package org.xson.tangyuan.validate.rule;

import java.util.Collection;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * List size 区间值校验
 */
public class CollectionLengthIntervalChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		Collection<?> list = (Collection<?>) xco.getObjectValue(fieldName);
		int val = list.size();
		int[] result = (int[]) value;
		return val >= result[0] && val <= result[1];
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		int[] result = { 0, 0 };
		result[0] = Integer.parseInt(array[0].trim());
		result[1] = Integer.parseInt(array[1].trim());
		return result;
	}
}
