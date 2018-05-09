package org.xson.tangyuan.app;

import org.xson.common.object.XCO;

/**
 * Tangyuan 应用常量
 */
public class AppProperty {

	private static XCO appConst = null;

	public static void init(XCO data) {
		if (null == appConst) {
			appConst = data;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(String key) {
		if (null == appConst) {
			return null;
		}
		return (T) appConst.get(key);
	}

}
