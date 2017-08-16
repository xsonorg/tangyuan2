package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * byte型最大值校验
 */
public class ByteMaxChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		byte max = ((Byte) value).byteValue();
		byte val = xco.getByteValue(fieldName);
		return max >= val;
	}

	public static Object parseValue(String value) {
		return Byte.parseByte(value);
	}
}
