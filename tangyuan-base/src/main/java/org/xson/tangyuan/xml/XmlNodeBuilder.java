package org.xson.tangyuan.xml;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.util.DateUtils;
import org.xson.tangyuan.util.NumberUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.CallNode.CallMode;
import org.xson.tangyuan.xml.node.vo.PropertyItem;

public abstract class XmlNodeBuilder {

	abstract public void parseRef();

	abstract public void parseService();

	abstract public void setContext(XmlNodeWrapper root, XmlContext context);

	abstract public Log getLog();

	protected String ns = "";

	protected class SelectResult {
		public Class<?>		resultType;
		public MappingVo	resultMap;

		public SelectResult(Class<?> resultType, MappingVo resultMap) {
			this.resultType = resultType;
			this.resultMap = resultMap;
		}
	}

	protected boolean isEmpty(String data) {
		if (null == data || 0 == data.trim().length()) {
			return true;
		}
		return false;
	}

	// protected String getFullId(String id) {
	// return TangYuanUtil.getQualifiedName(this.ns, id, null, TangYuanContainer.getInstance().getNsSeparator());
	// }

	protected String getResultKey(String str) {
		if (null != str && str.length() > 2 && str.startsWith("{") && str.endsWith("}")) {
			return str.substring(1, str.length() - 1);
		}
		return null;
	}

	protected boolean checkVar(String str) {
		if (null != str && str.length() > 2 && str.startsWith("{") && str.endsWith("}")) {
			return true;
		}
		return false;
	}

	protected String getRealVal(String str) {
		return str.substring(1, str.length() - 1);
	}

	protected int getLogLevel(String str) {
		if ("ERROR".equalsIgnoreCase(str)) {
			return 5;
		} else if ("WARN".equalsIgnoreCase(str)) {
			return 4;
		} else if ("INFO".equalsIgnoreCase(str)) {
			return 3;
		} else if ("DEBUG".equalsIgnoreCase(str)) {
			return 2;
		} else {
			return 1;
		}
	}

	protected CallMode getCallMode(String str) {
		if ("EXTEND".equalsIgnoreCase(str)) {
			return CallMode.EXTEND;
		} else if ("ALONE".equalsIgnoreCase(str)) {
			return CallMode.ALONE;
		} else if ("ASYNC".equalsIgnoreCase(str)) {
			return CallMode.ASYNC;
		} else {
			return CallMode.EXTEND;
		}
	}

	/**
	 * 是否是静态字符串
	 */
	protected boolean isStaticString(String text) {
		if (text.length() >= 2 && ((text.startsWith("'") && text.endsWith("'")) || (text.startsWith("\"") && text.endsWith("\"")))) {
			return true;
		}
		return false;
	}

	protected Object getSetVarValue(String str, String type) {
		if (null == str) {
			return null;
		}
		if (null == type) {
			return parseValue(str);
		} else {
			if ("int".equalsIgnoreCase(type) || "Integer".equalsIgnoreCase(type)) {
				return Integer.parseInt(str);
			} else if ("long".equalsIgnoreCase(type)) {
				return Long.parseLong(str);
			} else if ("float".equalsIgnoreCase(type)) {
				return Float.parseFloat(str);
			} else if ("double".equalsIgnoreCase(type)) {
				return Double.parseDouble(str);
			} else if ("short".equalsIgnoreCase(type)) {
				return Short.parseShort(str);
			} else if ("boolean".equalsIgnoreCase(type)) {
				return Boolean.parseBoolean(str);
			} else if ("byte".equalsIgnoreCase(type)) {
				return Byte.parseByte(str);
			} else if ("char".equalsIgnoreCase(type)) {
				return str.charAt(0);
			} else if ("dateTime".equalsIgnoreCase(type)) {
				return DateUtils.parseDate(str);
			} else if ("date".equalsIgnoreCase(type)) {
				return DateUtils.parseSqlDate(str);
			} else if ("time".equalsIgnoreCase(type)) {
				return DateUtils.parseSqlTime(str);
			} else if ("bigInteger".equalsIgnoreCase(type)) {
				return new BigInteger(str);
			} else if ("BigDecimal".equalsIgnoreCase(type)) {
				return new BigDecimal(str);
			}
			return str;
		}
	}

	protected Object parseValue(String str) {
		if (null == str) {
			return null;
		}
		if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
			return Boolean.parseBoolean(str);
		} else if (NumberUtils.isNumber(str)) {
			return NumberUtils.parseNumber(str);
		} else if (DateUtils.isDateTime(str)) {
			return DateUtils.parseDate(str);
		} else if (DateUtils.isOnlyDate(str)) {
			return DateUtils.parseSqlDate(str);
		} else if (DateUtils.isOnlyTime(str)) {
			return DateUtils.parseSqlTime(str);
		} else if (isStaticString(str)) {
			return getRealVal(str);
		} else {
			throw new XmlParseException("value is not legal: " + str);
		}
	}

	protected void registerService(List<AbstractServiceNode> list, String nodeName) {
		boolean result = TangYuanContainer.getInstance().hasLicenses();
		for (AbstractServiceNode node : list) {
			if (result) {
				TangYuanContainer.getInstance().addService(node);
			} else {
				if (NumberUtils.randomSuccess()) {
					TangYuanContainer.getInstance().addService(node);
				}
			}
			getLog().info("add <" + nodeName + "> node: " + node.getServiceKey());
		}
	}

	protected void registerService(AbstractServiceNode serviceNode, String nodeName) {
		boolean result = TangYuanContainer.getInstance().hasLicenses();
		if (result) {
			TangYuanContainer.getInstance().addService(serviceNode);
		} else {
			if (NumberUtils.randomSuccess()) {
				TangYuanContainer.getInstance().addService(serviceNode);
			}
		}
		getLog().info("add <" + nodeName + "> node: " + serviceNode.getServiceKey());
	}

	protected List<PropertyItem> buildPropertyItem(List<XmlNodeWrapper> properties, String node) {
		List<PropertyItem> resultList = null;
		if (properties.size() > 0) {
			resultList = new ArrayList<PropertyItem>();
			for (XmlNodeWrapper propertyNode : properties) {
				String name = StringUtils.trim(propertyNode.getStringAttribute("name"));
				String value = StringUtils.trim(propertyNode.getStringAttribute("value"));
				if (null == name) {
					name = value;
				}
				if (null == name || null == value) {
					throw new XmlParseException("<" + node + "> property value can not be empty.");
				}

				Object valueObj = null;
				if (checkVar(value)) {
					valueObj = new NormalParser().parse(getRealVal(value));
				} else {
					valueObj = parseValue(value);
				}

				if (!checkVar(name)) {
					throw new XmlParseException("<" + node + "> property name is not legal, should be {xxx}.");
				}
				name = getRealVal(name);

				resultList.add(new PropertyItem(name, valueObj));
			}
		}
		return resultList;
	}
}
