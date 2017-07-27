package org.xson.tangyuan.validate.rule;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * List size 最小值
 */
public class CollectionLengthMinChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		List<?> list = (List<?>) xco.getObjectValue(fieldName);
		int val = list.size();
		int min = ((Integer) value).intValue();
		return min <= val;
	}

	public static Object parseValue(String value) {
		return Integer.parseInt(value);
	}
}
