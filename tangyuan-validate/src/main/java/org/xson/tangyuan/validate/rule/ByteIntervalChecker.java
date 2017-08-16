package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * byte型区间值校验
 */
public class ByteIntervalChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		byte val = xco.getByteValue(fieldName);
		byte[] result = (byte[]) value;
		return val >= result[0] && val <= result[1];
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		byte[] result = { 0, 0 };
		result[0] = Byte.parseByte(array[0].trim());
		result[1] = Byte.parseByte(array[1].trim());
		return result;
	}
}
