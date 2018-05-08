package org.xson.tangyuan.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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

	public static Map<String, String> keyToUpperCase(Map<String, String> properties) {
		if (null == properties || 0 == properties.size()) {
			return properties;
		}
		Map<String, String> newMap = new HashMap<String, String>();
		for (Entry<String, String> entry : properties.entrySet()) {
			newMap.put(entry.getKey().toUpperCase(), entry.getValue());
		}
		return newMap;
	}
}
