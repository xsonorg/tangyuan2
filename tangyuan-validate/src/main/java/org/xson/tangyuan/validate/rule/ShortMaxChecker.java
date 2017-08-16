package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * short型最大值校验
 */
public class ShortMaxChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		short max = ((Short) value).shortValue();
		short val = xco.getShortValue(fieldName);
		return max >= val;
	}

	public static Object parseValue(String value) {
		return Short.parseShort(value);
	}
}
