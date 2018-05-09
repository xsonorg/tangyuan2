package org.xson.tangyuan.share.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.xson.tangyuan.share.ShareComponent;

public class ResourceManager {

	public static Properties getProperties(String basePath, String resource) throws Throwable {
		return getProperties(basePath, resource, false);
	}

	public static Properties getProperties(String basePath, String resource, boolean placeholder) throws Throwable {
		InputStream in = getInputStream(basePath, resource, placeholder);
		Properties props = new Properties();
		props.load(in);
		in.close();
		return props;
	}

	public static InputStream getInputStream(String basePath, String resource) throws Throwable {
		return getInputStream(basePath, resource, false);
	}

	public static InputStream getInputStream(String basePath, String resource, boolean placeholder) throws Throwable {
		InputStream in = null;
		if (resource.toLowerCase().startsWith("http://") || resource.toLowerCase().startsWith("https://")) {
			in = getUrlAsStream(resource);// TODO 这个要考虑加密
		} else {
			//in = Resources.getResourceAsStream(resource);
			in = new FileInputStream(new File(basePath, resource));
		}
		if (placeholder) {
			in = PlaceholderResourceSupport.processInputStream(in, ShareComponent.getInstance().getPlaceholderMap());
		}
		return in;
	}

	public static InputStream getUrlAsStream(String urlString) throws IOException {
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		return conn.getInputStream();
	}
}
