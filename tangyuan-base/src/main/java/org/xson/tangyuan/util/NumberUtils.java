package org.xson.tangyuan.util;

import java.util.regex.Pattern;

import org.xson.tangyuan.ognl.OgnlException;

public class NumberUtils {

	// public static boolean isNumber(String var) {
	// return var.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	// }

	public static boolean isInteger(String var) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(var).matches();
	}

	// public static Object parseNumber(String var) {
	// Object value = null;
	// if (var.indexOf(".") == -1) {
	// try {
	// value = Integer.parseInt(var);
	// } catch (NumberFormatException e) {
	// value = Long.parseLong(var);
	// }
	// } else {
	// try {
	// value = Float.parseFloat(var);
	// } catch (NumberFormatException e) {
	// value = Double.parseDouble(var);
	// }
	// }
	// return value;
	// }

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

}
