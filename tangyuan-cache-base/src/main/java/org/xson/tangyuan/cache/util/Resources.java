package org.xson.tangyuan.cache.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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

}
