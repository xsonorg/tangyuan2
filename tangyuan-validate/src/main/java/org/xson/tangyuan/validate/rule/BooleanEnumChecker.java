package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * boolean型固定值校验
 */
public class BooleanEnumChecker implements Checker {

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
		boolean[] result = new boolean[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Boolean.parseBoolean(array[i].trim());
		}
		return result;
	}

}
