package org.xson.tangyuan.mongo.xml.node;

import java.util.Collection;
import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.mongo.MongoComponent;
import org.xson.tangyuan.mongo.executor.MongoServiceContext;
import org.xson.tangyuan.mongo.ognl.vars.vo.MPPVariable;
import org.xson.tangyuan.mongo.ognl.vars.warper.MPPParserWarper;
import org.xson.tangyuan.mongo.util.MongoUtil;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.VariableConfig;
import org.xson.tangyuan.ognl.vars.vo.ShardingVariable;
import org.xson.tangyuan.ognl.vars.warper.SRPParserWarper;
import org.xson.tangyuan.ognl.vars.warper.SqlShardingParserWarper;
import org.xson.tangyuan.ognl.vars.warper.SqlTextParserWarper;
import org.xson.tangyuan.sharding.ShardingArgVo.ShardingTemplate;
import org.xson.tangyuan.sharding.ShardingDefManager;
import org.xson.tangyuan.sharding.ShardingResult;
import org.xson.tangyuan.type.Null;
import org.xson.tangyuan.util.DateUtils;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoTextNode implements TangYuanNode {

	// 原始字符串
	protected String		originalText	= null;

	// 静态SQL
	protected String		staticSql		= null;

	// 二次处理后的解析集合
	protected List<Object>	dynamicVarList	= null;

	public MongoTextNode(String text) {
		this.originalText = text;
		pretreatment();
	}

	protected void pretreatment() {

		ShardingDefManager shardingManager = MongoComponent.getInstance().getShardingDefManager();

		// 1. 对字符串进行预解析
		VariableConfig[] configs = new VariableConfig[7];
		configs[0] = new VariableConfig("{DT:", "}", false, new SqlShardingParserWarper(ShardingTemplate.DT, shardingManager));
		configs[1] = new VariableConfig("{T:", "}", false, new SqlShardingParserWarper(ShardingTemplate.T, shardingManager));
		configs[2] = new VariableConfig("{DI:", "}", false, new SqlShardingParserWarper(ShardingTemplate.DI, shardingManager));
		configs[3] = new VariableConfig("{I:", "}", false, new SqlShardingParserWarper(ShardingTemplate.I, shardingManager));
		configs[4] = new VariableConfig("{D:", "}", false, new SqlShardingParserWarper(ShardingTemplate.D, shardingManager));
		configs[5] = new VariableConfig("${", "}", true, new SRPParserWarper());
		configs[6] = new VariableConfig("#{", "}", true, new MPPParserWarper());
		List<Object> list = new SqlTextParserWarper().parse(this.originalText, configs);

		// 2.对初步的解析结果进行二次分析
		StringBuilder builder = new StringBuilder();
		boolean hasDynamicVar = false;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) instanceof Variable) {
				hasDynamicVar = true;
				continue;
			}
			builder.append(list.get(i).toString());
		}

		if (hasDynamicVar) {
			this.dynamicVarList = list;
		} else {
			this.staticSql = builder.toString();
		}
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);

		if (null == this.dynamicVarList) {
			mongoContext.addSql(this.staticSql);
		} else {
			// 每次解析
			String parsedText = null;
			StringBuilder builder = new StringBuilder();
			for (Object obj : this.dynamicVarList) {
				if (obj instanceof ShardingVariable) {
					ShardingResult result = (ShardingResult) ((ShardingVariable) obj).getValue(arg);
					mongoContext.setDsKey(result.getDataSource());
					builder.append(result.getTable());
				} else if (obj instanceof MPPVariable) {
					Object val = ((MPPVariable) obj).getValue(arg);

					if (null == val) {
						throw new TangYuanException("Field does not exist: " + ((MPPVariable) obj).getOriginal());
					}

					if (val instanceof Null) {
						builder.append("null");
						continue;
					}

					// fix bug. date process
					if (val instanceof java.sql.Time) {
						val = DateUtils.getTimeString((java.sql.Time) val);
					}
					if (val instanceof java.sql.Date) {
						val = DateUtils.getDateString((java.sql.Date) val);
					}
					if (val instanceof java.sql.Timestamp) {
						val = DateUtils.getTimestampString((java.sql.Timestamp) val);
					}
					if (val instanceof java.util.Date) {
						val = DateUtils.getDateTimeString((java.util.Date) val);
					}

					if (val instanceof String) {
						val = "'" + (String) val + "'";
					}

					// support array and collection
					if (val instanceof Collection) {
						val = MongoUtil.collectionToString((Collection<?>) val);
					}
					if (val.getClass().isArray()) {
						val = MongoUtil.arrayToString(val);
					}

					builder.append(val);
				} else if (obj instanceof Variable) {
					Object val = ((Variable) obj).getValue(arg);
					if (null == val) {
						// throw new TangYuanException("Field does not exist: " + ((MPPVariable) obj).getOriginal());
						// fix bug
						throw new TangYuanException("Field does not exist: " + ((Variable) obj).getOriginal());
					}
					if (val instanceof Null) {
						builder.append("null");
						continue;
					}
					builder.append(val);
				} else {
					builder.append(obj.toString());
				}
			}
			parsedText = builder.toString();
			mongoContext.addSql(parsedText);
		}
		return true;
	}

}
