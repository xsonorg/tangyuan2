package org.xson.tangyuan.share.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PropertyUtils {

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
