package org.xson.tangyuan.ognl.vars.parser;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.vo.DefaultValueVariable;
import org.xson.tangyuan.type.Null;

public class DefaultValueParser extends AbstractParser {

	/**
	 * 默认值类型
	 */
	public enum DefaultValueEnum {
		NOW, DATE, TIME
	}

	/**
	 * 获取默认值标识的索引位置
	 */
	private int getDefaultMarkPos(String text) {
		int length = text.length();
		boolean isString = false;
		for (int i = 0; i < length; i++) {
			char key = text.charAt(i);
			switch (key) {
			case '\'':
				isString = !isString;
				break;// bug
			case '|':
				if (!isString) {
					return i;
				}
			}
		}
		return -1;
	}

	public boolean check(String text) {
		// if (text.indexOf("|") > -1) {
		// return true;
		// }
		// return false;
		int pos = getDefaultMarkPos(text);
		if (pos > -1) {
			return true;
		}
		return false;
	}

	/**
	 * 获取刨除默认值的属性值
	 */
	private String getPropertyWithOutDefault(String text) {
		int pos = getDefaultMarkPos(text);
		return text.substring(0, pos);
	}

	private Object getPropertyDefaultValue(String property) {
		int pos = getDefaultMarkPos(property);
		String defaultString = property.substring(pos + 1, property.length()).trim();
		if (0 == defaultString.length()) {
			throw new OgnlException("Illegal default value: " + property);
		}
		// 属性的默认值 #{abccc|0, null, 'xxx', now(), date(), time()}, 只有在变量[#{}|${}]里边才可以存在
		if ("null".equalsIgnoreCase(defaultString)) {
			// return null;
			return Null.OBJECT; // TODO 需要测试
		} else if (defaultString.length() > 1 && defaultString.startsWith("'") && defaultString.endsWith("'")) {// fix bug
			return defaultString.substring(1, defaultString.length() - 1);
		} else if (isNumber(defaultString)) {
			return getNumber(defaultString);
		}

		// fixBug
		else if ("now()".equalsIgnoreCase(defaultString)) {
			return DefaultValueEnum.NOW;
		} else if ("date()".equalsIgnoreCase(defaultString)) {
			return DefaultValueEnum.DATE;
		} else if ("time()".equalsIgnoreCase(defaultString)) {
			return DefaultValueEnum.TIME;
		}

		else if ("byte.null".equalsIgnoreCase(defaultString)) {
			return Null.BYTE;
		} else if ("short.null".equalsIgnoreCase(defaultString)) {
			return Null.SHORT;
		} else if ("int.null".equalsIgnoreCase(defaultString)) {
			return Null.INTEGER;
		} else if ("long.null".equalsIgnoreCase(defaultString)) {
			return Null.LONG;
		} else if ("float.null".equalsIgnoreCase(defaultString)) {
			return Null.FLOAT;
		} else if ("bigDecimal.null".equalsIgnoreCase(defaultString)) {
			return Null.DOUBLE;
		} else if ("double.null".equalsIgnoreCase(defaultString)) {
			return Null.BIGDECIMAL;
		} else if ("string.null".equalsIgnoreCase(defaultString)) {
			return Null.STRING;
		} else if ("datetime.null".equalsIgnoreCase(defaultString)) {
			return Null.TIMESTAMP;
		} else if ("date.null".equalsIgnoreCase(defaultString)) {
			return Null.SQLDATE;
		} else if ("time.null".equalsIgnoreCase(defaultString)) {
			return Null.SQLTIME;
		} else if ("timestamp.null".equalsIgnoreCase(defaultString)) {
			return Null.SQLTIMESTAMP;
		}

		throw new OgnlException("Illegal default value: " + property);
	}

	public Variable parse(String text) {

		String original = text;
		int defaultValueType = 0;

		Object defaultValue = getPropertyDefaultValue(text);
		String property = getPropertyWithOutDefault(text);

		if (DefaultValueEnum.NOW == defaultValue) {
			defaultValueType = 1;
			defaultValue = null;
		} else if (DefaultValueEnum.DATE == defaultValue) {
			defaultValueType = 2;
			defaultValue = null;
		} else if (DefaultValueEnum.TIME == defaultValue) {
			defaultValueType = 3;
			defaultValue = null;
		}
		return new DefaultValueVariable(original, new NormalParser().parse(property), defaultValue, defaultValueType);
	}
}
