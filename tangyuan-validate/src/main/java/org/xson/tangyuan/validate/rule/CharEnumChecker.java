package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * char型固定值校验
 */
public class CharEnumChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		char val = xco.getCharValue(fieldName);
		char[] result = (char[]) value;
		for (int i = 0; i < result.length; i++) {
			if (val == result[i]) {
				return true;
			}
		}
		return false;
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		char[] result = new char[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = array[i].trim().charAt(0);
		}
		return result;
	}

}
