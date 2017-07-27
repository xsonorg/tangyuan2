package org.xson.tangyuan.validate.rule;

import java.util.regex.Pattern;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * 字符串匹配校验校验
 */
public class StringMatchChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		Pattern pattern = (Pattern) value;
		String val = xco.getStringValue(fieldName);
		return pattern.matcher(val).matches();
	}

	public static Object parseValue(String value) {
		// return Pattern.compile(value.trim());
		return Pattern.compile(value.trim().replaceAll("\\\\\\\\", "\\\\"));
	}

}
