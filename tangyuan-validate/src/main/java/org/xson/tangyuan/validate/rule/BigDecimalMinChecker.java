package org.xson.tangyuan.validate.rule;

import java.math.BigDecimal;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * BigDecimal型最小值固定值校验
 */
public class BigDecimalMinChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		double min = ((BigDecimal) value).doubleValue();
		double val = xco.getBigDecimalValue(fieldName).doubleValue();
		return min <= val;
	}

	public static Object parseValue(String value) {
		return new BigDecimal(value);
	}
}
