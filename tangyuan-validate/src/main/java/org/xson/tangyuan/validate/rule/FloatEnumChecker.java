package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * Float型固定值校验
 */
public class FloatEnumChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		float val = xco.getFloatValue(fieldName);
		float[] result = (float[]) value;
		for (int i = 0; i < result.length; i++) {
			if (val == result[i]) {
				return true;
			}
		}
		return false;
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		float[] result = new float[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Float.parseFloat(array[i].trim());
		}
		return result;
	}
}
