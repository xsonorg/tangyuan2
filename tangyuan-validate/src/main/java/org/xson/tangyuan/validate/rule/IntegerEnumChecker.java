package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * int型固定值校验
 */
public class IntegerEnumChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		int val = xco.getIntegerValue(fieldName);
		int[] result = (int[]) value;
		for (int i = 0; i < result.length; i++) {
			if (val == result[i]) {
				return true;
			}
		}
		return false;
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		int[] result = new int[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.parseInt(array[i].trim());
		}
		return result;
	}

}
