package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * double型最小值固定值校验
 */
public class DoubleMinChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		double min = ((Double) value).doubleValue();
		double val = xco.getDoubleValue(fieldName);
		return min <= val;
	}

	public static Object parseValue(String value) {
		return Double.parseDouble(value);
	}
}
