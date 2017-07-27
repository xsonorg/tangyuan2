package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * float型最小值固定值校验
 */
public class FloatMinChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		float min = ((Float) value).floatValue();
		float val = xco.getFloatValue(fieldName);
		return min <= val;
	}

	public static Object parseValue(String value) {
		return Float.parseFloat(value);
	}
}
