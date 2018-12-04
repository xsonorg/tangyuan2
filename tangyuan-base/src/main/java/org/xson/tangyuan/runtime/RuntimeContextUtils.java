package org.xson.tangyuan.runtime;

public class RuntimeContextUtils {

	public static String createTraceId() {
		return "T_" + System.currentTimeMillis();
	}

	public static String createSpanId() {
		// return UUID.randomUUID().toString();
		return "S_" + System.currentTimeMillis();
	}

}
