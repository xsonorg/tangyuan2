package org.xson.tangyuan.es.xml.node;

import java.util.Collection;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.es.ognl.vars.vo.ESPPVariable;
import org.xson.tangyuan.es.ognl.vars.warper.ESPPParserWarper;
import org.xson.tangyuan.es.util.ESUtil;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.VariableConfig;
import org.xson.tangyuan.ognl.vars.warper.SRPParserWarper;
import org.xson.tangyuan.ognl.vars.warper.SqlTextParserWarper;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.EsServiceContext;
import org.xson.tangyuan.type.Null;
import org.xson.tangyuan.util.DateUtils;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class EsTextNode implements TangYuanNode {

	// 原始字符串
	protected String		originalText	= null;

	// 静态SQL
	protected String		staticSql		= null;

	// 二次处理后的解析集合
	protected List<Object>	dynamicVarList	= null;

	public EsTextNode(String text) {
		this.originalText = text;
		pretreatment();
	}

	protected void pretreatment() {

		VariableConfig[] configs = new VariableConfig[2];
		configs[0] = new VariableConfig("${", "}", true, new SRPParserWarper());
		configs[1] = new VariableConfig("#{", "}", true, new ESPPParserWarper());
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
	public boolean execute(ActuatorContext ac, Object arg1, Object acArg) throws Throwable {

		EsServiceContext context = (EsServiceContext) ac.getServiceContext(TangYuanServiceType.ES);

		boolean ignoreQuotes = context.getIgnoreQuotes();

		if (null == this.dynamicVarList) {
			context.addSql(this.staticSql);
		} else {
			// 每次解析
			String parsedText = null;
			StringBuilder builder = new StringBuilder();
			for (Object obj : this.dynamicVarList) {
				if (obj instanceof ESPPVariable) {
					Object val = ((ESPPVariable) obj).getValue(acArg);

					if (null == val) {
						throw new TangYuanException("Field does not exist: " + ((ESPPVariable) obj).getOriginal());
					}

					if (val instanceof Null) {
						builder.append("null");
						continue;
					}

					// fix bug. date process
					// if (val instanceof java.sql.Time) {
					// val = DateUtils.getTimeString((java.sql.Time) val);
					// }
					// if (val instanceof java.sql.Date) {
					// val = DateUtils.getDateString((java.sql.Date) val);
					// }
					// if (val instanceof java.sql.Timestamp) {
					// val = DateUtils.getTimestampString((java.sql.Timestamp) val);
					// }
					// if (val instanceof java.util.Date) {
					// val = DateUtils.getDateTimeString((java.util.Date) val);
					// }

					if (val instanceof java.sql.Time) {
						val = DateUtils.getTimeString((java.sql.Time) val);
					} else if (val instanceof java.sql.Date) {
						val = DateUtils.getDateString((java.sql.Date) val);
					} else if (val instanceof java.sql.Timestamp) {
						val = DateUtils.getTimestampString((java.sql.Timestamp) val);
					} else if (val instanceof java.util.Date) {
						val = DateUtils.getDateTimeString((java.util.Date) val);
					}

					// if (val instanceof String) {
					// // 专用于URL中
					// if (!ignoreQuotes) {
					// // val = "'" + (String) val + "'";
					// val = "\"" + (String) val + "\"";
					// }
					// }
					// // support array and collection
					// if (val instanceof Collection) {
					// val = ESUtil.collectionToString((Collection<?>) val);
					// }
					// if (val.getClass().isArray()) {
					// val = ESUtil.arrayToString(val);
					// }

					if (val instanceof String) {
						// val = "'" + (String) val + "'";
						if (!ignoreQuotes) {
							// val = "'" + (String) val + "'";
							val = "\"" + (String) val + "\"";
						}
					} else if (val instanceof XCO) { // xco->{}
						val = ((XCO) val).toJSON();
					} else if (val instanceof Collection) { // support array and collection
						val = ESUtil.collectionToString((Collection<?>) val);
					} else if (val.getClass().isArray()) {
						val = ESUtil.arrayToString(val);
					}

					builder.append(val);
				} else if (obj instanceof Variable) {
					// Object val = ((Variable) obj).getValue(temp);
					// if (null == val) {
					// // fix bug
					// throw new TangYuanException("Field does not exist: " + ((Variable) obj).getOriginal());
					// }
					// if (val instanceof Null) {
					// builder.append("null");
					// continue;
					// }
					// builder.append(val);

					Object val = ((Variable) obj).getValue(acArg);

					if (null == val) {
						throw new TangYuanException("Field does not exist: " + ((Variable) obj).getOriginal());
					}

					if (val instanceof Null) {
						builder.append("null");
						continue;
					}

					// fix bug. date process
					if (val instanceof java.sql.Time) {
						val = DateUtils.getTimeString((java.sql.Time) val);
					} else if (val instanceof java.sql.Date) {
						val = DateUtils.getDateString((java.sql.Date) val);
					} else if (val instanceof java.sql.Timestamp) {
						val = DateUtils.getTimestampString((java.sql.Timestamp) val);
					} else if (val instanceof java.util.Date) {
						val = DateUtils.getDateTimeString((java.util.Date) val);
					} else if (val instanceof XCO) { // xco->{}
						val = ((XCO) val).toJSON();
					} else if (val instanceof Collection) { // support array and collection
						val = ESUtil.collectionToString((Collection<?>) val);
					} else if (val.getClass().isArray()) {
						val = ESUtil.arrayToString(val);
					}

					builder.append(val);

				} else {
					builder.append(obj.toString());
				}
			}
			parsedText = builder.toString();
			context.addSql(parsedText);
		}
		return true;
	}

}
