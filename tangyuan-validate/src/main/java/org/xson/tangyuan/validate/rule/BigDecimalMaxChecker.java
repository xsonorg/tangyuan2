package org.xson.tangyuan.validate.rule;

import java.math.BigDecimal;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * BigDecimal型最大值校验
 */
public class BigDecimalMaxChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		double max = ((BigDecimal) value).doubleValue();
		double val = xco.getBigDecimalValue(fieldName).doubleValue();
		return max >= val;
	}

	public static Object parseValue(String value) {
		return new BigDecimal(value);
	}
}
