package org.xson.tangyuan.runtime;

import java.util.UUID;

public class RuntimeContextUtils {

	public static String createTraceId() {
		// return "T_" + System.currentTimeMillis();
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static String createSpanId() {
		// return "S_" + System.currentTimeMillis();
		return createTraceId();
	}

}
