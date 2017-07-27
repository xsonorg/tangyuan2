package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * float型区间值校验
 */
public class FloatIntervalChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		float val = xco.getFloatValue(fieldName);
		float[] result = (float[]) value;
		return val >= result[0] && val <= result[1];
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		float[] result = { 0f, 0f };
		result[0] = Float.parseFloat(array[0].trim());
		result[1] = Float.parseFloat(array[1].trim());
		return result;
	}
}
