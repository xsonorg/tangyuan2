package org.xson.tangyuan.validate.rule;

import java.util.Collection;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * List size 最大值
 */
public class CollectionLengthMaxChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		Collection<?> list = (Collection<?>) xco.getObjectValue(fieldName);
		int val = list.size();
		int max = ((Integer) value).intValue();
		return max >= val;
	}

	public static Object parseValue(String value) {
		return Integer.parseInt(value);
	}
}
