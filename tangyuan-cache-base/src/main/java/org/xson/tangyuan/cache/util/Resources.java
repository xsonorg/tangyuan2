package org.xson.tangyuan.cache.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

public class Resources {

	public static InputStream getResourceAsStream(String resource) {
		ClassLoader[] classLoader = new ClassLoader[] { Thread.currentThread().getContextClassLoader(), Resources.class.getClassLoader() };
		return getResourceAsStream(resource, classLoader);
	}

	private static InputStream getResourceAsStream(String resource, ClassLoader[] classLoader) {
		for (ClassLoader cl : classLoader) {
			if (null != cl) {
				// try to find the resource as passed
				InputStream returnValue = cl.getResourceAsStream(resource);
				// now, some class loaders want this leading "/", so we'll add it and try again if we didn't find the resource
				if (null == returnValue)
					returnValue = cl.getResourceAsStream("/" + resource);

				if (null != returnValue)
					return returnValue;
			}
		}
		// return null;
		return getFileResource(resource);
	}

	private static InputStream getFileResource(String resource) {
		try {
			File file = new File(resource);
			if (file.exists()) {
				return new FileInputStream(file);
			}

		} catch (FileNotFoundException e) {
		}
		return null;
	}
	
	public static Properties getResourceAsProperties(String resource) throws IOException {
		Properties props = new Properties();
		InputStream in = getResourceAsStream(resource);
		props.load(in);
		in.close();
		return props;
	}

	public static Properties getUrlAsProperties(String urlString) throws IOException {
		Properties props = new Properties();
		InputStream in = getUrlAsStream(urlString);
		props.load(in);
		in.close();
		return props;
	}
	
	public static InputStream getUrlAsStream(String urlString) throws IOException {
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		return conn.getInputStream();
	}
}
