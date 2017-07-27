package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * 字符串长度校验
 */
public class StringLengthMinChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		String strVal = xco.getStringValue(fieldName);
		int val = strVal.length();
		int min = ((Integer) value).intValue();
		return min <= val;
	}

	public static Object parseValue(String value) {
		return Integer.parseInt(value);
	}
}
