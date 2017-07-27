package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * 字符串过滤校验校验
 */
public class StringFilterChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		String val = xco.getStringValue(fieldName);
		String[] result = (String[]) value;
		for (int i = 0; i < result.length; i++) {
			if (val.contains(result[i])) {
				return false;
			}
		}
		return true;
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		String[] result = new String[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = array[i].trim();
		}
		return result;
	}

}
