package org.xson.tangyuan.validate.rule;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.Checker;

/**
 * byte型固定值校验
 */
public class ByteEnumChecker implements Checker {

	@Override
	public boolean check(XCO xco, String fieldName, Object value) {
		byte val = xco.getByteValue(fieldName);
		byte[] result = (byte[]) value;
		for (int i = 0; i < result.length; i++) {
			if (val == result[i]) {
				return true;
			}
		}
		return false;
	}

	public static Object parseValue(String value) {
		String[] array = value.split(",");
		byte[] result = new byte[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Byte.parseByte(array[i].trim());
		}
		return result;
	}

}
