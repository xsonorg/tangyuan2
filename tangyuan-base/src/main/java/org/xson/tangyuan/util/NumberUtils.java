package org.xson.tangyuan.util;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.xson.tangyuan.ognl.OgnlException;

public class NumberUtils {

	public static boolean isInteger(String var) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(var).matches();
	}

	/**
	 * 是否是数值后缀
	 */
	public static boolean isNumberSuffix(char chr) {
		if (chr == 'l' || chr == 'L' || chr == 'f' || chr == 'F' || chr == 'd' || chr == 'D') {
			return true;
		}
		return false;
	}

	/**
	 * 是否是数值
	 */
	public static boolean isNumber(String text) {
		return text.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)[lLfFdD]?$");
	}

	public static Object parseNumber(String text) {
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

	public static boolean randomSuccess() {
		int x = (int) (Math.random() * 100);
		return x > 10;
	}

	public static boolean randomFailure() {
		return !randomSuccess();
	}

	public static long parseTimeWithUnit(String val) {
		return parseTimeWithUnit(val, null);
	}

	public static long parseTimeWithUnit(String text, TimeUnit standardUnit) {
		long val = -1;
		if (text.endsWith("ns")) {
			val = Long.parseLong(text.substring(0, text.length() - 2));
			if (null != standardUnit && TimeUnit.NANOSECONDS != standardUnit) {
				return standardUnit.convert(val, TimeUnit.NANOSECONDS);
			}
			return val;
		}
		if (text.endsWith("ms")) {//	ms:MILLISECONDS 
			val = Long.parseLong(text.substring(0, text.length() - 2));
			if (null != standardUnit && TimeUnit.MILLISECONDS != standardUnit) {
				return standardUnit.convert(val, TimeUnit.MILLISECONDS);
			}
			return val;
		}
		if (text.endsWith("s")) {//	    s:SECONDS 
			val = Long.parseLong(text.substring(0, text.length() - 1));
			if (null != standardUnit && TimeUnit.SECONDS != standardUnit) {
				return standardUnit.convert(val, TimeUnit.SECONDS);
			}
			return val;
		}
		if (text.endsWith("m")) {//	    m:MINUTES 
			val = Long.parseLong(text.substring(0, text.length() - 1));
			if (null != standardUnit && TimeUnit.MINUTES != standardUnit) {
				return standardUnit.convert(val, TimeUnit.MINUTES);
			}
			return val;
		}
		if (text.endsWith("h")) {//	    h:HOURS 
			val = Long.parseLong(text.substring(0, text.length() - 1));
			if (null != standardUnit && TimeUnit.HOURS != standardUnit) {
				return standardUnit.convert(val, TimeUnit.HOURS);
			}
			return val;
		}
		if (text.endsWith("d")) {//	    d:DAYS 
			val = Long.parseLong(text.substring(0, text.length() - 1));
			if (null != standardUnit && TimeUnit.DAYS != standardUnit) {
				return standardUnit.convert(val, TimeUnit.DAYS);
			}
			return val;
		}

		val = Long.parseLong(text);
		if (null != standardUnit && TimeUnit.SECONDS != standardUnit) {
			return standardUnit.convert(val, TimeUnit.SECONDS);
		}
		return val;
	}

}
