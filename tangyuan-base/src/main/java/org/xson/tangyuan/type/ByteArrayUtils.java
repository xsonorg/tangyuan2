package org.xson.tangyuan.type;

class ByteArrayUtils {
	static byte[] convertToPrimitiveArray(Byte[] objects) {
		final byte[] bytes = new byte[objects.length];
		for (int i = 0; i < objects.length; i++) {
			Byte b = objects[i];
			bytes[i] = b;
		}
		return bytes;
	}

	static Byte[] convertToObjectArray(byte[] bytes) {
		final Byte[] objects = new Byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			objects[i] = b;
		}
		return objects;
	}
}
