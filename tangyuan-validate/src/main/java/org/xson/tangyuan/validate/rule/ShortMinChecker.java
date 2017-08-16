package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * short型最小值固定值校验
 */
public class ShortMinChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		short min = ((Short) value).shortValue();
		short val = xco.getShortValue(fieldName);
		return min <= val;
	}

	public static Object parseValue(String value) {
		return Short.parseShort(value);
	}
}
