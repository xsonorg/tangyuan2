package org.xson.tangyuan.util;

import java.util.Map;

public class PropertyUtils {

	public static int getIntValue(Map<String, String> properties, String key, int defaultValue) {
		if (properties.containsKey(key)) {
			return Integer.parseInt(properties.get(key));
		}
		return defaultValue;
	}

	public static long getLongValue(Map<String, String> properties, String key, long defaultValue) {
		if (properties.containsKey(key)) {
			return Long.parseLong(properties.get(key));
		}
		return defaultValue;
	}

	public static boolean getBooleanValue(Map<String, String> properties, String key, boolean defaultValue) {
		if (properties.containsKey(key)) {
			return Boolean.parseBoolean(properties.get(key));
		}
		return defaultValue;
	}

	public static String getStringValue(Map<String, String> properties, String key, String defaultValue) {
		if (properties.containsKey(key)) {
			return properties.get(key);
		}
		return defaultValue;
	}
}
