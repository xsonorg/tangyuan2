package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * long型区间值校验
 */
public class LongIntervalChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		long val = xco.getLongValue(fieldName);
		long[] result = (long[]) value;
		return val >= result[0] && val <= result[1];
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		long[] result = { 0L, 0L };
		result[0] = Long.parseLong(array[0].trim());
		result[1] = Long.parseLong(array[1].trim());
		return result;
	}
}
