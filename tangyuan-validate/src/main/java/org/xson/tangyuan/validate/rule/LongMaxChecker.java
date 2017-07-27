package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * long型最大值校验
 */
public class LongMaxChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		long max = ((Long) value).longValue();
		long val = xco.getLongValue(fieldName);
		return max >= val;
	}

	public static Object parseValue(String value) {
		return Long.parseLong(value);
	}
}
