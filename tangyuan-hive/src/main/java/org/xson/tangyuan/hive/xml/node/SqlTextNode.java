package org.xson.tangyuan.hive.xml.node;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.hive.HiveComponent;
import org.xson.tangyuan.hive.executor.HiveServiceContext;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.VariableConfig;
import org.xson.tangyuan.ognl.vars.vo.SPPVariable;
import org.xson.tangyuan.ognl.vars.vo.ShardingVariable;
import org.xson.tangyuan.ognl.vars.warper.SPPParserWarper;
import org.xson.tangyuan.ognl.vars.warper.SRPParserWarper;
import org.xson.tangyuan.ognl.vars.warper.SqlShardingParserWarper;
import org.xson.tangyuan.ognl.vars.warper.SqlTextParserWarper;
import org.xson.tangyuan.sharding.ShardingArgVo.ShardingTemplate;
import org.xson.tangyuan.sharding.ShardingDefManager;
import org.xson.tangyuan.sharding.ShardingResult;
import org.xson.tangyuan.xml.node.TangYuanNode;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class SqlTextNode implements TangYuanNode {

	// 原始字符串
	protected String			originalText	= null;

	// 这里存放的是#变量的内容
	protected List<Variable>	staticVarList	= new ArrayList<Variable>();

	// 静态SQL
	protected String			staticSql		= null;

	// 二次处理后的解析集合
	protected List<Object>		dynamicVarList	= null;

	public SqlTextNode(String text) {
		this.originalText = text;
		pretreatment();
	}

	protected void pretreatment() {

		ShardingDefManager shardingManager = HiveComponent.getInstance().getShardingDefManager();

		// 1. 对字符串进行预解析
		VariableConfig[] configs = new VariableConfig[7];
		configs[0] = new VariableConfig("{DT:", "}", false, new SqlShardingParserWarper(ShardingTemplate.DT, shardingManager));
		configs[1] = new VariableConfig("{T:", "}", false, new SqlShardingParserWarper(ShardingTemplate.T, shardingManager));
		configs[2] = new VariableConfig("{DI:", "}", false, new SqlShardingParserWarper(ShardingTemplate.DI, shardingManager));
		configs[3] = new VariableConfig("{I:", "}", false, new SqlShardingParserWarper(ShardingTemplate.I, shardingManager));
		configs[4] = new VariableConfig("{D:", "}", false, new SqlShardingParserWarper(ShardingTemplate.D, shardingManager));
		configs[5] = new VariableConfig("${", "}", true, new SRPParserWarper());
		configs[6] = new VariableConfig("#{", "}", true, new SPPParserWarper());
		List<Object> list = new SqlTextParserWarper().parse(this.originalText, configs);

		// 2.对初步的解析结果进行二次分析
		StringBuilder builder = new StringBuilder();
		boolean hasStaticVar = false;
		boolean hasDynamicVar = false;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) instanceof SPPVariable) {
				// 放入静态变量列表中
				this.staticVarList.add((Variable) list.get(i));
				// 替换"?", SQL占位变量专用
				list.set(i, "?");
				builder.append("?");
				hasStaticVar = true;
				continue;
			}
			if (list.get(i) instanceof Variable) {
				hasDynamicVar = true;
				continue;
			}
			builder.append(list.get(i).toString());
		}

		if (hasStaticVar && !hasDynamicVar) {
			// 有静态变量, 没有动态变量
			this.staticSql = builder.toString();
		} else if (!hasStaticVar && hasDynamicVar) {
			// 有动态变量, 没有静态变量
			this.dynamicVarList = list;
			this.staticVarList = null;
		} else if (hasStaticVar && hasDynamicVar) {
			// 都有
			this.dynamicVarList = list;
		} else {
			// 无变量
			this.staticSql = builder.toString();
			this.staticVarList = null;
		}
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		// SqlServiceContext sqlContext = (SqlServiceContext) context.getSqlServiceContext();
		HiveServiceContext sqlContext = (HiveServiceContext) context.getServiceContext(TangYuanServiceType.HIVE);
		if (null == this.dynamicVarList) {
			// 不存在动态解析列表
			if (null == this.staticVarList) {
				sqlContext.addSql(this.staticSql);
			} else {
				sqlContext.addSql(this.staticSql);
				sqlContext.addStaticVarList(this.staticVarList, arg);
			}
		} else {
			// 每次解析
			String parsedText = null;
			StringBuilder builder = new StringBuilder();
			for (Object obj : this.dynamicVarList) {
				if (obj instanceof ShardingVariable) {
					// ShardingArgVo shardingArg = ((ShardingVariable) obj).getShardingArg();
					// ShardingResult result = shardingArg.getShardingResult(arg);
					ShardingResult result = (ShardingResult) ((ShardingVariable) obj).getValue(arg);
					// 设置数据源
					sqlContext.setDsKey(result.getDataSource());
					// 设置表名, 有可能为空字符串*
					builder.append(result.getTable());
				} else if (obj instanceof Variable) {
					builder.append(((Variable) obj).getValue(arg));
				} else {
					builder.append(obj.toString());
				}
			}
			parsedText = builder.toString();
			if (null == this.staticVarList) {
				sqlContext.addSql(parsedText);
			} else {
				sqlContext.addSql(parsedText);
				sqlContext.addStaticVarList(this.staticVarList, arg);
			}
		}
		return true;
	}

	// TODO var is empty: #{}, 以后将支持单个单数, 数组, list, arg...
	// staticVarList.add(null);

}
