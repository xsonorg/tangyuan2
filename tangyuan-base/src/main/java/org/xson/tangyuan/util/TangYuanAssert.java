package org.xson.tangyuan.util;

import java.util.Collection;

public class TangYuanAssert {

	public static void stringEmpty(String str, String message) {
		if (null == str || 0 == str.length()) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void objectEmpty(Object obj, String message) {
		if (null == obj) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void collectionEmpty(Collection<?> obj, String message) {
		if (null == obj || 0 == obj.size()) {
			throw new IllegalArgumentException(message);
		}
	}
}
