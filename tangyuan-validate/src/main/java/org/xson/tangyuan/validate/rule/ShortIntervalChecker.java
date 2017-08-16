package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * short型区间值校验
 */
public class ShortIntervalChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		short val = xco.getShortValue(fieldName);
		short[] result = (short[]) value;
		return val >= result[0] && val <= result[1];
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		short[] result = { 0, 0 };
		result[0] = Short.parseShort(array[0].trim());
		result[1] = Short.parseShort(array[1].trim());
		return result;
	}
}
