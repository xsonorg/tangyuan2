package org.xson.tangyuan.validate.rule;

import java.math.BigInteger;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * BigInteger型最小值固定值校验
 */
public class BigIntegerMinChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		long min = ((BigInteger) value).longValue();
		long val = xco.getBigIntegerValue(fieldName).longValue();
		return min <= val;
	}

	public static Object parseValue(String value) {
		return new BigInteger(value);
	}
}
