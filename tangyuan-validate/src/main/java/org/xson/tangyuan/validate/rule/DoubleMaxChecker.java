package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * double型最大值校验
 */
public class DoubleMaxChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		double max = ((Double) value).doubleValue();
		double val = xco.getDoubleValue(fieldName);
		return max >= val;
	}

	public static Object parseValue(String value) {
		return Double.parseDouble(value);
	}
}
