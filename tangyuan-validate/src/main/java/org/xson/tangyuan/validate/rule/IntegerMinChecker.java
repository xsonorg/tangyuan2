package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * int型最小值固定值校验
 */
public class IntegerMinChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		int min = ((Integer) value).intValue();
		int val = xco.getIntegerValue(fieldName);
		return min <= val;
	}

	public static Object parseValue(String value) {
		return Integer.parseInt(value);
	}
}
