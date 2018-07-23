package org.xson.tangyuan.app;

import org.xson.common.object.XCO;

/**
 * Tangyuan 应用常量
 */
public class AppProperty {

	public final static String	HOST_IP		= "$$HOST_IP";
	public final static String	HOST_NAME	= "$$HOST_NAME";
	public final static String	APP_NAME	= "$$APP_NAME";
	public final static String	NODE_NAME	= "$$NODE_NAME";

	private static XCO			appConst	= null;

	public static void init(XCO data) {
		if (null == appConst) {
			appConst = data;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(String key) {
		//		if (null == appConst) {
		//			return null;
		//		}
		return (T) appConst.get(key);
	}

}
