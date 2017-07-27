package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * long型固定值校验
 */
public class LongEnumChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		long val = xco.getLongValue(fieldName);
		long[] result = (long[]) value;
		for (int i = 0; i < result.length; i++) {
			if (val == result[i]) {
				return true;
			}
		}
		return false;
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		long[] result = new long[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Long.parseLong(array[i].trim());
		}
		return result;
	}
}
