package org.xson.tangyuan.runtime;

import java.util.UUID;

public class RuntimeContextUtils {

	public static String createTraceId() {
		return "T:" + System.currentTimeMillis();
	}

	public static String createSpanId() {
		return UUID.randomUUID().toString();
	}

}
