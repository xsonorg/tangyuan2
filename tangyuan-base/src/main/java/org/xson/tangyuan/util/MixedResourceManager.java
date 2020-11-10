package org.xson.tangyuan.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.xson.tangyuan.app.AppPlaceholder;
import org.xson.tangyuan.manager.LocalStorage;
import org.xson.tangyuan.manager.ManagerLauncherContext;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlParseException;

/**
 * 混合的资源管理器，支持classpath, localStorage, remote
 * 
 * @since 1.3.0
 */
public class MixedResourceManager {

	private static LocalStorage localStorage = null;

	static {
		ManagerLauncherContext mlc = XmlGlobalContext.getMlc();
		if (null != mlc) {
			localStorage = mlc.getLocalStorage();
		}
	}

	public static Properties getProperties(String resource) throws Throwable {
		return getProperties(resource, false);
	}

	public static Properties getProperties(String resource, boolean placeholder) throws Throwable {
		return getProperties(resource, placeholder, true);
	}

	public static Properties getProperties(String resource, boolean placeholder, boolean useLocalStorage) throws Throwable {
		InputStream in = getInputStream(resource, placeholder, useLocalStorage);
		Properties props = new Properties();
		props.load(in);
		in.close();
		return props;
	}

	public static InputStream getInputStream(String resource) throws Throwable {
		return getInputStream(resource, false);
	}

	public static InputStream getInputStream(String resource, boolean placeholder) throws Throwable {
		return getInputStream(resource, placeholder, true);
	}

	public static InputStream getInputStream(String resource, boolean placeholder, boolean useLocalStorage) throws Throwable {
		try {
			InputStream in = null;
			if (useLocalStorage) {
				in = getInputStreamFromLocalStorage(resource);
				if (null != in) {
					return in;
				}
			}
			String resourceLowerCase = resource.toLowerCase();
			if (resourceLowerCase.startsWith("http://") || resourceLowerCase.startsWith("https://")) {
				in = Resources.getUrlAsStream(resource);
			} else if (resourceLowerCase.startsWith("file://")) {
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

	private static InputStream getInputStreamFromLocalStorage(String resource) {
		if (null == localStorage) {
			return null;
		}
		InputStream in = null;
		try {
			String context = localStorage.getResourceContext(resource);
			if (null != context) {
				in = new ByteArrayInputStream(context.getBytes(StandardCharsets.UTF_8));
			}
		} catch (Throwable e) {
		}
		return in;
	}

}
