package org.xson.tangyuan.xml;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.warper.GAParserWarper;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.DateUtils;
import org.xson.tangyuan.util.MixedResourceManager;
import org.xson.tangyuan.util.NumberUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;

public class DefaultXmlBuilder {

	protected Log				log				= LogFactory.getLog(getClass());

	protected XmlGlobalContext	globalContext	= null;

	protected XPathParser		xPathParser		= null;
	protected XmlNodeWrapper	root			= null;
	protected String			resource		= null;

	protected void clean() {
		this.xPathParser = null;
		this.root = null;
		this.resource = null;
		this.globalContext = null;
	}

	protected void init(String resource, String rootName, boolean placeholder) throws Throwable {
		init(resource, rootName, placeholder, false);
	}

	protected void init(String resource, String rootName, boolean placeholder, boolean useLocalStorage) throws Throwable {
		this.resource = resource;
		InputStream inputStream = getInputStream(resource, placeholder, useLocalStorage);
		this.xPathParser = new XPathParser(inputStream);
		this.root = this.xPathParser.evalNode("/" + rootName);
		inputStream.close();
	}

	protected InputStream getInputStream(String resource, boolean placeholder, boolean useLocalStorage) throws Throwable {
		return MixedResourceManager.getInputStream(resource, placeholder, useLocalStorage);
	}

	protected Properties getProperties(String resource, boolean placeholder, boolean useLocalStorage) throws Throwable {
		return MixedResourceManager.getProperties(resource, placeholder, useLocalStorage);
	}

	protected String lang(String template) {
		return lang(template, (Object[]) null);
	}

	protected String lang(String template, Object... args) {
		String content = TangYuanLang.get(template);
		// if (null == content) {
		// content = template;
		// }
		content = TangYuanUtil.format(content, args);
		return content;
	}

	// 从属性中获取值/////////////////////////////////////////////////////////////////////////////////////////

	// string

	protected String getStringFromAttr(XmlNodeWrapper node, String attributeName) {
		return getStringFromAttr(node, attributeName, null, null);
	}

	protected String getStringFromAttr(XmlNodeWrapper node, String attributeName, String errorMsg) {
		return getStringFromAttr(node, attributeName, null, errorMsg);
	}

	protected String getStringFromAttr(XmlNodeWrapper node, String attributeName, String def, String errorMsg) {
		String val = StringUtils.trimEmpty(node.getStringAttribute(attributeName));
		if (null == val) {
			if (null != def) {
				return def;
			}
			if (null != errorMsg) {
				throw new XmlParseException(errorMsg);
			}
		}
		return val;
	}

	// String[]

	protected String[] getStringArrayFromAttr(XmlNodeWrapper xNode, String attributeName) {
		String group = getStringFromAttr(xNode, attributeName);
		if (null == group) {
			return null;
		}
		String[] groups = group.split(",");
		for (int i = 0; i < groups.length; i++) {
			groups[i] = StringUtils.trim(groups[i]);
		}
		return groups;
	}

	// boolean

	protected Boolean getBoolanFromAttr(XmlNodeWrapper node, String attributeName) {
		return getBoolanFromAttr(node, attributeName, null, null);
	}

	protected Boolean getBoolanFromAttr(XmlNodeWrapper node, String attributeName, String errorMsg) {
		return getBoolanFromAttr(node, attributeName, null, errorMsg);
	}

	protected Boolean getBoolanFromAttr(XmlNodeWrapper node, String attributeName, Boolean def, String errorMsg) {
		String val = StringUtils.trimEmpty(node.getStringAttribute(attributeName));
		if (null == val) {
			if (null != def) {
				return def;
			}
			if (null != errorMsg) {
				throw new XmlParseException(errorMsg);
			}

			return null;
		}
		return Boolean.parseBoolean(val);
	}

	protected boolean getBoolFromAttr(XmlNodeWrapper node, String attributeName, String errorMsg) {
		String val = StringUtils.trimEmpty(node.getStringAttribute(attributeName));
		if (null == val) {
			throw new XmlParseException(errorMsg);
		}
		return Boolean.parseBoolean(val);
	}

	protected boolean getBoolFromAttr(XmlNodeWrapper node, String attributeName, boolean def) {
		String val = StringUtils.trimEmpty(node.getStringAttribute(attributeName));
		if (null == val) {
			return def;
		}
		return Boolean.parseBoolean(val);
	}

	// Integer

	protected Integer getIntegerFromAttr(XmlNodeWrapper node, String attributeName) {
		return getIntegerFromAttr(node, attributeName, null, null);
	}

	protected Integer getIntegerFromAttr(XmlNodeWrapper node, String attributeName, String errorMsg) {
		return getIntegerFromAttr(node, attributeName, null, errorMsg);
	}

	protected Integer getIntegerFromAttr(XmlNodeWrapper node, String attributeName, Integer def, String errorMsg) {
		String val = StringUtils.trimEmpty(node.getStringAttribute(attributeName));
		if (null == val) {
			if (null != def) {
				return def;
			}
			if (null != errorMsg) {
				throw new XmlParseException(errorMsg);
			}

			return null;
		}
		return Integer.parseInt(val);
	}

	protected int getIntFromAttr(XmlNodeWrapper node, String attributeName, String errorMsg) {
		String val = StringUtils.trimEmpty(node.getStringAttribute(attributeName));
		if (null == val) {
			throw new XmlParseException(errorMsg);
		}
		return Integer.parseInt(val);
	}

	protected int getIntFromAttr(XmlNodeWrapper node, String attributeName, int def) {
		String val = StringUtils.trimEmpty(node.getStringAttribute(attributeName));
		if (null == val) {
			return def;
		}
		return Integer.parseInt(val);
	}

	// Long getLongWrapperValue

	protected Long getLongWrapperFromAttr(XmlNodeWrapper node, String attributeName) {
		return getLongWrapperFromAttr(node, attributeName, null, null);
	}

	protected Long getLongWrapperFromAttr(XmlNodeWrapper node, String attributeName, String errorMsg) {
		return getLongWrapperFromAttr(node, attributeName, null, errorMsg);
	}

	protected Long getLongWrapperFromAttr(XmlNodeWrapper node, String attributeName, Long def, String errorMsg) {
		String val = StringUtils.trimEmpty(node.getStringAttribute(attributeName));
		if (null == val) {
			if (null != def) {
				return def;
			}
			if (null != errorMsg) {
				throw new XmlParseException(errorMsg);
			}

			return null;
		}
		return Long.valueOf(val);
	}

	protected long getLongFromAttr(XmlNodeWrapper node, String attributeName, String errorMsg) {
		String val = StringUtils.trimEmpty(node.getStringAttribute(attributeName));
		if (null == val) {
			throw new XmlParseException(errorMsg);
		}
		return Long.parseLong(val);
	}

	protected long getLongFromAttr(XmlNodeWrapper node, String attributeName, long def) {
		String val = StringUtils.trimEmpty(node.getStringAttribute(attributeName));
		if (null == val) {
			return def;
		}
		return Long.parseLong(val);
	}

	//////////////

	@SuppressWarnings("unchecked")
	protected <T> Class<T> getClassForName(String className, Class<T> father, String errorMsg) throws Throwable {
		Class<?> clazz = ClassUtils.forName(className);
		if (null != father && null != errorMsg) {
			if (!father.isAssignableFrom(clazz)) {
				// throw new XmlParseException(lang("xml.class.impl.interface", className, father.getName()));
				throw new XmlParseException(errorMsg);
			}
		}
		return (Class<T>) clazz;
	}

	protected <T> T getInstanceForName(String className, Class<T> father, String errorMsg) throws Throwable {
		Class<T> clazz = getClassForName(className, father, errorMsg);
		return TangYuanUtil.newInstance(clazz);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////

	protected XmlNodeWrapper getMostOneNode(XmlNodeWrapper root, String expression, String errorMsg) {
		if (null == root) {
			root = this.root;
		}
		List<XmlNodeWrapper> nodes = root.evalNodes(expression);
		int size = nodes.size();
		if (0 == size) {
			return null;
		}
		if (size > 1 && null != errorMsg) {
			throw new XmlParseException(errorMsg);
		}
		return nodes.get(0);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////

	protected Variable parseVariableUseGA(String val) {
		return new GAParserWarper().parse(getRealVal(val));
	}

	protected String getResultKey(String str) {
		if (checkVar(str)) {
			return getRealVal(str);
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
		return str.substring(1, str.length() - 1).trim();
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
			} else if ("BigInteger".equalsIgnoreCase(type)) {
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

	protected String parseVariableKey(XmlNodeWrapper xNode, String attributeName, String tagName) {
		String resultKey = getStringFromAttr(xNode, attributeName);
		if (null != resultKey) {
			if (!checkVar(resultKey)) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid.should", resultKey, "{xxx}", tagName, this.resource));
			}
			resultKey = getRealVal(resultKey);
		}
		return resultKey;
	}
}
