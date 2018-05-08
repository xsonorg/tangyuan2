package org.xson.tangyuan.cache.util;

import java.io.InputStream;
import java.util.Map;

public class ResourceManager {

	//	public static Properties getProperties(String resource) throws Throwable {
	//		return getProperties(resource, false);
	//	}
	//
	//	public static Properties getProperties(String resource, boolean placeholder) throws Throwable {
	//		InputStream in = getInputStream(resource, placeholder);
	//		Properties props = new Properties();
	//		props.load(in);
	//		in.close();
	//		return props;
	//	}

	//	public static InputStream getInputStream(String resource) throws Throwable {
	//		return getInputStream(resource, false);
	//	}

	public static InputStream getInputStream(String resource, Map<String, String> placeholderMap) throws Throwable {
		InputStream in = null;
		if (resource.toLowerCase().startsWith("http://") || resource.toLowerCase().startsWith("https://")) {
			in = Resources.getUrlAsStream(resource);// TODO 这个要考虑加密
		} else {
			in = Resources.getResourceAsStream(resource);
		}
		if (null == placeholderMap || 0 == placeholderMap.size()) {
			in = PlaceholderResourceSupport.processInputStream(in, placeholderMap);
		}
		return in;
	}

}
