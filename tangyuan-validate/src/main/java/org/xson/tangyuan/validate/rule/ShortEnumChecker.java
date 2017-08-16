package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * short型固定值校验
 */
public class ShortEnumChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		short val = xco.getShortValue(fieldName);
		short[] result = (short[]) value;
		for (int i = 0; i < result.length; i++) {
			if (val == result[i]) {
				return true;
			}
		}
		return false;
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		short[] result = new short[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Short.parseShort(array[i].trim());
		}
		return result;
	}

}
