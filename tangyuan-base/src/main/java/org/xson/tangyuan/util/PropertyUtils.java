package org.xson.tangyuan.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.xml.XmlParseException;

public class PropertyUtils {

	// int

	public static int getIntValue(Map<String, String> p, String key, int defaultValue) {
		return getIntValue(p, key, defaultValue, false);
	}

	public static int getIntValue(Map<String, String> p, String key, int defaultValue, boolean upperCase) {
		if (upperCase) {
			key = key.toUpperCase();
		}
		String val = StringUtils.trimEmpty(p.get(key));
		if (null != val) {
			return Integer.parseInt(val);
		}
		return defaultValue;
	}

	public static int getIntValue(Map<String, String> p, String key, String errorMsg) {
		return getIntValue(p, key, errorMsg, false);
	}

	public static int getIntValue(Map<String, String> p, String key, String errorMsg, boolean upperCase) {
		if (upperCase) {
			key = key.toUpperCase();
		}
		String val = StringUtils.trimEmpty(p.get(key));
		if (null != val) {
			return Integer.parseInt(val);
		}
		throw new TangYuanException(errorMsg);
	}

	// integer

	public static Integer getIntegerValue(Map<String, String> p, String key) {
		return getIntegerValue(p, key, null, null, false);
	}

	public static Integer getIntegerValue(Map<String, String> p, String key, Integer def) {
		return getIntegerValue(p, key, def, null, false);
	}

	public static Integer getIntegerValue(Map<String, String> p, String key, String errorMsg) {
		return getIntegerValue(p, key, null, errorMsg, false);
	}

	public static Integer getIntegerValue(Map<String, String> p, String key, Integer def, String errorMsg, boolean upperCase) {
		if (upperCase) {
			key = key.toUpperCase();
		}
		String val = StringUtils.trimEmpty(p.get(key));
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

	// long
	public static long getLongValue(Map<String, String> p, String key, long defaultValue) {
		return getLongValue(p, key, defaultValue, false);
	}

	public static long getLongValue(Map<String, String> p, String key, long defaultValue, boolean upperCase) {
		if (upperCase) {
			key = key.toUpperCase();
		}
		String val = StringUtils.trimEmpty(p.get(key));
		if (null != val) {
			return Long.parseLong(val);
		}
		return defaultValue;
	}

	public static long getLongValue(Map<String, String> p, String key, String errorMsg) {
		return getLongValue(p, key, errorMsg, false);
	}

	public static long getLongValue(Map<String, String> p, String key, String errorMsg, boolean upperCase) {
		if (upperCase) {
			key = key.toUpperCase();
		}
		String val = StringUtils.trimEmpty(p.get(key));
		if (null != val) {
			return Long.parseLong(val);
		}
		throw new TangYuanException(errorMsg);
	}

	// longWrapper

	public static Long getLongWrapperValue(Map<String, String> p, String key) {
		return getLongWrapperValue(p, key, null, null, false);
	}

	public static Long getLongWrapperValue(Map<String, String> p, String key, Long def) {
		return getLongWrapperValue(p, key, def, null, false);
	}

	public static Long getLongWrapperValue(Map<String, String> p, String key, String errorMsg) {
		return getLongWrapperValue(p, key, null, errorMsg, false);
	}

	public static Long getLongWrapperValue(Map<String, String> p, String key, Long def, String errorMsg, boolean upperCase) {
		if (upperCase) {
			key = key.toUpperCase();
		}
		String val = StringUtils.trimEmpty(p.get(key));
		if (null == val) {
			if (null != def) {
				return def;
			}
			if (null != errorMsg) {
				throw new XmlParseException(errorMsg);
			}
			return null;
		}
		return Long.parseLong(val);
	}

	// longTimeString

	// bool
	public static boolean getBoolValue(Map<String, String> p, String key, boolean defaultValue) {
		return getBoolValue(p, key, defaultValue, false);
	}

	public static boolean getBoolValue(Map<String, String> p, String key, boolean defaultValue, boolean upperCase) {
		if (upperCase) {
			key = key.toUpperCase();
		}
		String val = StringUtils.trimEmpty(p.get(key));
		if (null != val) {
			return Boolean.parseBoolean(val);
		}
		return defaultValue;
	}

	public static boolean getBoolValue(Map<String, String> p, String key, String errorMsg) {
		return getBoolValue(p, key, errorMsg, false);
	}

	public static boolean getBoolValue(Map<String, String> p, String key, String errorMsg, boolean upperCase) {
		if (upperCase) {
			key = key.toUpperCase();
		}
		String val = StringUtils.trimEmpty(p.get(key));
		if (null != val) {
			return Boolean.parseBoolean(val);
		}
		throw new TangYuanException(errorMsg);
	}

	// Boolean

	public static Boolean getBooleanValue(Map<String, String> p, String key) {
		return getBooleanValue(p, key, null, null, false);
	}

	public static Boolean getBooleanValue(Map<String, String> p, String key, Boolean def) {
		return getBooleanValue(p, key, def, null, false);
	}

	public static Boolean getBooleanValue(Map<String, String> p, String key, String errorMsg) {
		return getBooleanValue(p, key, null, errorMsg, false);
	}

	public static Boolean getBooleanValue(Map<String, String> p, String key, Boolean def, String errorMsg, boolean upperCase) {
		if (upperCase) {
			key = key.toUpperCase();
		}
		String val = StringUtils.trimEmpty(p.get(key));
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

	// string

	public static String getStringValue(Map<String, String> p, String key) {
		return getStringValue(p, key, null, null, false);
	}

	public static String getStringValue(Map<String, String> p, String key, String def) {
		return getStringValue(p, key, def, null, false);
	}

	//	public static String getStringValue(Map<String, String> p, String key, String errorMsg) {
	//		return getStringValue(p, key, null, errorMsg, false);
	//	}

	public static String getStringValue(Map<String, String> p, String key, String def, String errorMsg, boolean upperCase) {
		if (upperCase) {
			key = key.toUpperCase();
		}
		String val = StringUtils.trimEmpty(p.get(key));
		if (null == val) {
			if (null != def) {
				return def;
			}
			if (null != errorMsg) {
				throw new XmlParseException(errorMsg);
			}
			return null;
		}
		return val;
	}

	// array
	public static String[] getStringArrayValue(Map<String, String> p, String key, String separator) {
		String val = StringUtils.trimEmpty(p.get(key));
		if (null == val) {
			return null;
		}
		String[] temp = val.split(separator);
		for (int i = 0; i < temp.length; i++) {
			temp[i] = StringUtils.trimEmpty(temp[i]);
		}
		return temp;
	}

	public static Integer[] getIntegerArrayValue(Map<String, String> p, String key, String separator) {
		String val = StringUtils.trimEmpty(p.get(key));
		if (null == val) {
			return null;
		}
		String[]  temp   = val.split(separator);
		Integer[] result = new Integer[temp.length];
		try {
			for (int i = 0; i < temp.length; i++) {
				result[i] = Integer.valueOf(StringUtils.trimEmpty(temp[i]));
			}
		} catch (Throwable e) {
			//			if (null != errorMsg) {
			//				throw new XmlParseException(errorMsg);
			//			}
			throw new XmlParseException(TangYuanLang.get("property.invalid", key, val));
		}
		return result;
	}

	//	public static int[] getIntArrayValue(Map<String, String> p, String key, String separator, String errorMsg) {
	//		return null;
	//	}

	public static Map<String, String> keyToUpperCase(Map<String, String> properties) {
		if (null == properties || 0 == properties.size()) {
			return properties;
		}
		Map<String, String> newMap = new HashMap<String, String>();
		for (Entry<String, String> entry : properties.entrySet()) {
			newMap.put(entry.getKey().toUpperCase(), entry.getValue());
		}
		return newMap;
	}

	public static String replace(String inString, String oldPattern, String newPattern) {
		if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
			return inString;
		}
		StringBuilder sb     = new StringBuilder();
		int           pos    = 0; // our position in the old string
		int           index  = inString.indexOf(oldPattern);
		// the index of an occurrence we've found, or -1
		int           patLen = oldPattern.length();
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
