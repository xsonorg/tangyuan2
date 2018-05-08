package org.xson.tangyuan.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.xson.tangyuan.TangYuanContainer;

public class PlaceholderResourceSupport {

	public static final String PLACEHOLDER_SYMBOL = "%"; // symbol

	public static void processMap(Map<String, String> properties) throws Throwable {
		processMap(properties, TangYuanContainer.getInstance().getXmlGlobalContext().getPlaceholderMap());
	}

	public static void processMap(Map<String, String> properties, Map<String, String> placeholderMap) throws Throwable {
		if (null == properties || 0 == properties.size()) {
			return;
		}
		if (null == placeholderMap || 0 == placeholderMap.size()) {
			return;
		}
		for (Entry<String, String> entry : properties.entrySet()) {
			String value = entry.getValue();
			value = process(value, placeholderMap);
			entry.setValue(value);
		}
	}

	public static void processProperties(Properties properties, Map<String, String> placeholderMap) throws Throwable {
		if (null == properties || 0 == properties.size()) {
			return;
		}
		if (null == placeholderMap || 0 == placeholderMap.size()) {
			return;
		}
		for (Entry<Object, Object> entry : properties.entrySet()) {
			String value = process(entry.getValue().toString(), placeholderMap);
			entry.setValue(value);
		}
	}

	public static String process(String property, Map<String, String> placeholderMap) throws Throwable {
		if (null == placeholderMap || 0 == placeholderMap.size()) {
			return property;
		}
		int placeholderLength = PLACEHOLDER_SYMBOL.length();
		StringBuilder result = new StringBuilder(property);
		int startIndex = property.indexOf(PLACEHOLDER_SYMBOL);
		while (startIndex != -1) {
			int endIndex = findPlaceholderEndIndex(result, startIndex);
			if (endIndex == -1) {
				startIndex = -1;
				continue;
			}
			String placeholder = result.substring(startIndex + placeholderLength, endIndex);
			// System.out.println("originalPlaceholder\t" + placeholder);
			String propVal = placeholderMap.get(placeholder);
			if (propVal != null) {
				result.replace(startIndex, endIndex + placeholderLength, propVal);
				startIndex = result.indexOf(PLACEHOLDER_SYMBOL, startIndex + propVal.length());
			} else {
				startIndex = endIndex + 1;
			}
		}
		return result.toString();
	}

	private static int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
		int index = startIndex + PLACEHOLDER_SYMBOL.length();
		char symbol = PLACEHOLDER_SYMBOL.charAt(0);
		while (index < buf.length()) {
			if (symbol == buf.charAt(index)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	public static InputStream processInputStream(InputStream src, Map<String, String> placeholderMap) throws Throwable {
		return processInputStream(src, "UTF-8", placeholderMap);
	}

	public static InputStream processInputStream(InputStream src, String charsetName, Map<String, String> placeholderMap) throws Throwable {
		if (null == placeholderMap || 0 == placeholderMap.size()) {
			return src;
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int n = 0;
		while (-1 != (n = src.read(buffer))) {
			output.write(buffer, 0, n);
		}
		buffer = output.toByteArray();
		src.close();
		String content = new String(buffer, charsetName);
		content = process(content, placeholderMap);
		return new ByteArrayInputStream(content.getBytes(charsetName));
	}

}
