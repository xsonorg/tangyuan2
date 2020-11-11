package org.xson.tangyuan.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.xson.tangyuan.app.AppPlaceholder;
import org.xson.tangyuan.xml.XmlParseException;

public class ResourceManager {

	public static Properties getProperties(String resource) throws Throwable {
		return getProperties(resource, false);
	}

	public static Properties getProperties(String resource, boolean placeholder) throws Throwable {
		InputStream in = getInputStream(resource, placeholder);
		Properties props = new Properties();
		// props.load(in);
		// in.close();
		// fix: 中文乱码问题
		Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
		props.load(reader);
		reader.close();
		return props;
	}

	public static InputStream getInputStream(String resource) throws Throwable {
		return getInputStream(resource, false);
	}

	public static InputStream getInputStream(String resource, boolean placeholder) throws Throwable {
		try {
			InputStream in = null;
			if (resource.toLowerCase().startsWith("http://") || resource.toLowerCase().startsWith("https://")) {
				in = Resources.getUrlAsStream(resource);
			} else if (resource.toLowerCase().startsWith("file://")) {
				in = Resources.getResourceAsStreamFromFile(resource);
			} else {
				in = Resources.getResourceAsStream(resource);
			}
			if (placeholder) {
				in = PlaceholderResourceSupport.processInputStream(in, AppPlaceholder.getData());
			}
			return in;
		} catch (Throwable e) {
			throw new XmlParseException("get inputStream exception: " + resource, e);
		}
	}

}
