package org.xson.tangyuan.validate.rule;

import java.lang.reflect.Array;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * Array Length 最大值
 */
public class ArrayLengthMaxChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		Object arrayObject = xco.getObjectValue(fieldName);
		int val = Array.getLength(arrayObject);
		int max = ((Integer) value).intValue();
		return max >= val;
	}

	public static Object parseValue(String value) {
		return Integer.parseInt(value);
	}
}
