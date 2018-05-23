package org.xson.tangyuan.timer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xson.tangyuan.ognl.OgnlException;

public class TimerUtil {

	public static Map<String, Object> parseProperties(Map<String, String> properties) {
		Map<String, Object> preProperties = new HashMap<String, Object>();
		for (Entry<String, String> entry : properties.entrySet()) {
			preProperties.put(entry.getKey(), parseValue(entry.getValue()));
		}
		return preProperties;
	}

	protected static Object parseValue(String val) {
		if (0 == val.length()) {
			throw new OgnlException("Illegal value: " + val);
		}
		if ("null".equalsIgnoreCase(val)) {
			return null;
		} else if (val.length() > 1 && val.startsWith("'") && val.endsWith("'")) {// fix bug
			return val.substring(1, val.length() - 1);
		} else if (isNumber(val)) {
			return getNumber(val);
		} else if ("now()".equalsIgnoreCase(val)) {
			return java.util.Date.class;
		} else if ("date()".equalsIgnoreCase(val)) {
			return java.sql.Date.class;
		} else if ("time()".equalsIgnoreCase(val)) {
			return java.sql.Time.class;
		} else if ("timestamp()".equalsIgnoreCase(val)) {
			return java.sql.Timestamp.class;
		}

		//throw new OgnlException("Illegal value type: " + val);
		return val; // 默认最为字符串处理
	}

	protected static boolean isNumberSuffix(char chr) {
		if (chr == 'l' || chr == 'L' || chr == 'f' || chr == 'F' || chr == 'd' || chr == 'D') {
			return true;
		}
		return false;
	}

	protected static boolean isNumber(String text) {
		return text.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)[lLfFdD]?$");
	}

	protected static Object getNumber(String text) {
		char chr = text.charAt(text.length() - 1);
		if (isNumberSuffix(chr)) {
			if (chr == 'l' || chr == 'L') {
				return Long.valueOf(text.substring(0, text.length() - 1));
			} else if (chr == 'f' || chr == 'F') {
				return Float.valueOf(text.substring(0, text.length() - 1));
			} else if (chr == 'd' || chr == 'D') {
				return Double.valueOf(text.substring(0, text.length() - 1));
			}
		} else if (text.indexOf(".") > -1) {
			Object number = null;
			try {
				number = Float.parseFloat(text);
			} catch (NumberFormatException e) {
				number = Double.parseDouble(text);
			}
			return number;
		} else {
			Object number = null;
			try {
				number = Integer.parseInt(text);
			} catch (NumberFormatException e) {
				number = Long.parseLong(text);
			}
			return number;
		}
		throw new OgnlException("Illegal numeric string: " + text);
	}
}
