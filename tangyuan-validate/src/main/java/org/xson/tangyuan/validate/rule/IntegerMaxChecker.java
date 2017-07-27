package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * int型最大值校验
 */
public class IntegerMaxChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		int max = ((Integer) value).intValue();
		int val = xco.getIntegerValue(fieldName);
		return max >= val;
	}

	public static Object parseValue(String value) {
		return Integer.parseInt(value);
	}
}
