package org.xson.tangyuan.validate.rule;

import java.math.BigDecimal;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * BigDecimal型区间值校验
 */
public class BigDecimalIntervalChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		BigDecimal val = xco.getBigDecimalValue(fieldName);
		BigDecimal[] result = (BigDecimal[]) value;
		return val.doubleValue() >= result[0].doubleValue() && val.doubleValue() <= result[1].doubleValue();
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		BigDecimal[] result = { null, null };
		result[0] = new BigDecimal(array[0].trim());
		result[1] = new BigDecimal(array[1].trim());
		return result;
	}
}
