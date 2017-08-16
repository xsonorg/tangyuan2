package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * byte型最小值固定值校验
 */
public class ByteMinChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		byte min = ((Byte) value).byteValue();
		byte val = xco.getByteValue(fieldName);
		return min <= val;
	}

	public static Object parseValue(String value) {
		return Byte.parseByte(value);
	}
}
