package org.xson.tangyuan.util;

import java.io.InputStream;
import java.util.Properties;

import org.xson.tangyuan.TangYuanContainer;

public class ResourceManager {

	public static Properties getProperties(String resource) throws Throwable {
		return getProperties(resource, false);
	}

	public static Properties getProperties(String resource, boolean placeholder) throws Throwable {
		InputStream in = getInputStream(resource, placeholder);
		Properties props = new Properties();
		props.load(in);
		in.close();
		return props;
	}

	public static InputStream getInputStream(String resource) throws Throwable {
		return getInputStream(resource, false);
	}

	public static InputStream getInputStream(String resource, boolean placeholder) throws Throwable {
		InputStream in = null;
		if (resource.toLowerCase().startsWith("http://") || resource.toLowerCase().startsWith("https://")) {
			in = Resources.getUrlAsStream(resource);// TODO 这个要考虑加密
		} else {
			in = Resources.getResourceAsStream(resource);
		}
		if (placeholder) {
			in = PlaceholderResourceSupport.processInputStream(in, TangYuanContainer.getInstance().getXmlGlobalContext().getPlaceholderMap());
		}
		return in;
	}

	//		Properties props = null;
	//		if (resource.toLowerCase().startsWith("http://") || resource.toLowerCase().startsWith("https://")) {
	//			// TODO 这个要考虑加密
	//			props = Resources.getUrlAsProperties(resource);
	//		} else {
	//			props = Resources.getResourceAsProperties(resource);
	//		}
	//		if (resource.toLowerCase().startsWith("http://") || resource.toLowerCase().startsWith("https://")) {
	//			props = Resources.getUrlAsProperties(resource);
	//		} else {
	//			props = Resources.getResourceAsProperties(resource);
	//		}

}
