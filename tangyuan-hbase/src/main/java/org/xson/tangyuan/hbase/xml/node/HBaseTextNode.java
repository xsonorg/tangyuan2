package org.xson.tangyuan.hbase.xml.node;

import java.util.Collection;
import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.hbase.executor.HBaseServiceContext;
import org.xson.tangyuan.hbase.ognl.vars.vo.HBasePPVariable;
import org.xson.tangyuan.hbase.ognl.vars.warper.HBasePPParserWarper;
import org.xson.tangyuan.hbase.util.ESUtil;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.VariableConfig;
import org.xson.tangyuan.ognl.vars.warper.SRPParserWarper;
import org.xson.tangyuan.ognl.vars.warper.SqlTextParserWarper;
import org.xson.tangyuan.type.Null;
import org.xson.tangyuan.util.DateUtils;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class HBaseTextNode implements TangYuanNode {

	// 原始字符串
	protected String		originalText	= null;

	// 静态SQL
	protected String		staticSql		= null;

	// 二次处理后的解析集合
	protected List<Object>	dynamicVarList	= null;

	public HBaseTextNode(String text) {
		this.originalText = text;
		pretreatment();
	}

	protected void pretreatment() {

		VariableConfig[] configs = new VariableConfig[2];
		configs[0] = new VariableConfig("${", "}", true, new SRPParserWarper());
		configs[1] = new VariableConfig("#{", "}", true, new HBasePPParserWarper());
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
		HBaseServiceContext xContext = (HBaseServiceContext) context.getServiceContext(TangYuanServiceType.HBASE);

		if (null == this.dynamicVarList) {
			xContext.addSql(this.staticSql);
		} else {
			// 每次解析
			String parsedText = null;
			StringBuilder builder = new StringBuilder();
			for (Object obj : this.dynamicVarList) {
				if (obj instanceof HBasePPVariable) {
					Object val = ((HBasePPVariable) obj).getValue(arg);

					if (null == val) {
						throw new TangYuanException("Field does not exist: " + ((HBasePPVariable) obj).getOriginal());
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
						// val = "'" + (String) val + "'";
						val = "\"" + (String) val + "\"";
					}

					// support array and collection
					if (val instanceof Collection) {
						val = ESUtil.collectionToString((Collection<?>) val);
					}
					if (val.getClass().isArray()) {
						val = ESUtil.arrayToString(val);
					}

					builder.append(val);
				} else if (obj instanceof Variable) {
					Object val = ((Variable) obj).getValue(arg);
					if (null == val) {
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
			xContext.addSql(parsedText);
		}
		return true;
	}

}
