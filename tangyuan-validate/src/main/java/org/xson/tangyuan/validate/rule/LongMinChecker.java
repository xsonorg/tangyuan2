package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * long型最小值固定值校验
 */
public class LongMinChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		long min = ((Long) value).longValue();
		long val = xco.getLongValue(fieldName);
		return min <= val;
	}

	public static Object parseValue(String value) {
		return Long.parseLong(value);
	}
}
