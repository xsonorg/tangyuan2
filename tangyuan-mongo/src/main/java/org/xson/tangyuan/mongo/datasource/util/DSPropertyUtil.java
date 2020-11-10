package org.xson.tangyuan.mongo.datasource.util;

import java.util.Map;

import org.xson.tangyuan.mongo.datasource.DataSourceException;

public class DSPropertyUtil {

	/**
	 * 获取属性值
	 * 
	 * @param property
	 *            属性名
	 * @param properties
	 *            值容器
	 * @param decryptProperties
	 *            加密值容器
	 * @param defaultValue
	 *            默认值
	 * @param throwEx
	 *            无值是否抛异常
	 * @return
	 */
	public static String getPropertyStringValue(String property, Map<String, String> properties, Map<String, String> decryptProperties,
			String defaultValue, boolean throwEx) {
		String value = null;
		value = properties.get(property.toUpperCase());
		if (null == value) {
			value = (String) defaultValue;
		} else {
			String strValue = (String) value;
			if (null != decryptProperties && strValue.startsWith("${") && strValue.endsWith("}")) {
				value = decryptProperties.get(property.substring(2, property.length() - 1));
			}
		}
		if (throwEx && null == value) {
			throw new DataSourceException("missing attribute initialization data source: " + property);
		}
		return value;
	}

	public static int getPropertyIntegerValue(String property, Map<String, String> properties, Map<String, String> decryptProperties,
			Integer defaultValue, boolean throwEx) {
		Object value = null;
		value = properties.get(property.toUpperCase());
		if (null == value) {
			value = defaultValue;
		} else {
			String strValue = (String) value;
			if (null != decryptProperties && strValue.startsWith("${") && strValue.endsWith("}")) {
				value = decryptProperties.get(property.substring(2, property.length() - 1));
			}
			if (null != value) {
				value = Integer.parseInt((String) value);
			}
		}
		if (throwEx && null == value) {
			throw new DataSourceException("missing attribute initialization data source: " + property);
		}
		return (Integer) value;
	}

	public static boolean getPropertyBooleanValue(String property, Map<String, String> properties, Map<String, String> decryptProperties,
			Boolean defaultValue, boolean throwEx) {
		Object value = null;
		value = properties.get(property.toUpperCase());
		if (null == value) {
			value = defaultValue;
		} else {
			String strValue = (String) value;
			if (null != decryptProperties && strValue.startsWith("${") && strValue.endsWith("}")) {
				value = decryptProperties.get(property.substring(2, property.length() - 1));
			}
			if (null != value) {
				value = Boolean.parseBoolean((String) value);
			}
		}
		if (throwEx && null == value) {
			throw new DataSourceException("missing attribute initialization data source: " + property);
		}
		return (Boolean) value;
	}

	public static int getIntValue(Map<String, String> properties, String key, int defaultValue) {
		if (properties.containsKey(key)) {
			return Integer.parseInt(properties.get(key));
		}
		return defaultValue;
	}

	public static long getLongValue(Map<String, String> properties, String key, long defaultValue) {
		if (properties.containsKey(key)) {
			return Long.parseLong(properties.get(key));
		}
		return defaultValue;
	}

	public static boolean getBooleanValue(Map<String, String> properties, String key, boolean defaultValue) {
		if (properties.containsKey(key)) {
			return Boolean.parseBoolean(properties.get(key));
		}
		return defaultValue;
	}

	public static String getStringValue(Map<String, String> properties, String key, String defaultValue) {
		if (properties.containsKey(key)) {
			return properties.get(key);
		}
		return defaultValue;
	}

	public static String replace(String inString, String oldPattern, String newPattern) {
		if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
			return inString;
		}
		StringBuilder sb = new StringBuilder();
		int pos = 0; // our position in the old string
		int index = inString.indexOf(oldPattern);
		// the index of an occurrence we've found, or -1
		int patLen = oldPattern.length();
		while (index >= 0) {
			sb.append(inString.substring(pos, index));
			sb.append(newPattern);
			pos = index + patLen;
			index = inString.indexOf(oldPattern, pos);
		}
		sb.append(inString.substring(pos));
		// remember to append any characters to the right of a match
		return sb.toString();
	}

	public static boolean hasLength(CharSequence str) {
		return (str != null && str.length() > 0);
	}
}
