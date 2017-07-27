package org.xson.tangyuan.ognl.vars.parser;

import java.util.regex.Pattern;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.Variable;

public abstract class AbstractParser {

	abstract public Variable parse(String text);

	protected boolean isInteger(String var) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(var).matches();
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

	/**
	 * 是否是数值后缀
	 */
	protected boolean isNumberSuffix(char chr) {
		if (chr == 'l' || chr == 'L' || chr == 'f' || chr == 'F' || chr == 'd' || chr == 'D') {
			return true;
		}
		return false;
	}

	/**
	 * 是否是数值
	 */
	protected boolean isNumber(String text) {
		return text.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)[lLfFdD]?$");
	}

	protected Object getNumber(String text) {
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
