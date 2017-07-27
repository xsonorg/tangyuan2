package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * float型最大值校验
 */
public class FloatMaxChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		float max = ((Float) value).floatValue();
		float val = xco.getFloatValue(fieldName);
		return max >= val;
	}

	public static Object parseValue(String value) {
		return Float.parseFloat(value);
	}
}
