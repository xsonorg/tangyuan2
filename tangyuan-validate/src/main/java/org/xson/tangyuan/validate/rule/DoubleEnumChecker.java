package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * Double型固定值校验
 */
public class DoubleEnumChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		double val = xco.getDoubleValue(fieldName);
		double[] result = (double[]) value;
		for (int i = 0; i < result.length; i++) {
			if (val == result[i]) {
				return true;
			}
		}
		return false;
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		double[] result = new double[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Double.parseDouble(array[i].trim());
		}
		return result;
	}
}
