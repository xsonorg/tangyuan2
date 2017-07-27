package org.xson.tangyuan.validate.rule;

import java.math.BigInteger;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * BigInteger型区间值校验
 */
public class BigIntegerIntervalChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		BigInteger val = xco.getBigIntegerValue(fieldName);
		BigInteger[] result = (BigInteger[]) value;
		return val.longValue() >= result[0].longValue() && val.longValue() <= result[1].longValue();
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		BigInteger[] result = { null, null };
		result[0] = new BigInteger(array[0].trim());
		result[1] = new BigInteger(array[1].trim());
		return result;
	}
}
