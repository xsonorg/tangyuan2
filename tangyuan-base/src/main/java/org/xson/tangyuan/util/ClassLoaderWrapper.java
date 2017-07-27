package org.xson.tangyuan.util;

import java.io.InputStream;
import java.net.URL;

public class ClassLoaderWrapper {

	public static ClassLoaderWrapper	instance	= new ClassLoaderWrapper();

	ClassLoader							defaultClassLoader;

	ClassLoader							systemClassLoader;

	ClassLoaderWrapper() {
		try {
			systemClassLoader = ClassLoader.getSystemClassLoader();
		} catch (SecurityException ignored) {
			// AccessControlException on Google App Engine
		}
	}

	public URL getResourceAsURL(String resource) {
		return getResourceAsURL(resource, getClassLoaders(null));
	}

	public URL getResourceAsURL(String resource, ClassLoader classLoader) {
		return getResourceAsURL(resource, getClassLoaders(classLoader));
	}

	public InputStream getResourceAsStream(String resource) {
		return getResourceAsStream(resource, getClassLoaders(null));
	}

	public InputStream getResourceAsStream(String resource, ClassLoader classLoader) {
		return getResourceAsStream(resource, getClassLoaders(classLoader));
	}

	public Class<?> classForName(String name) throws ClassNotFoundException {
		return classForName(name, getClassLoaders(null));
	}

	public Class<?> classForName(String name, ClassLoader classLoader) throws ClassNotFoundException {
		return classForName(name, getClassLoaders(classLoader));
	}

	InputStream getResourceAsStream(String resource, ClassLoader[] classLoader) {
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
		return null;
	}

	URL getResourceAsURL(String resource, ClassLoader[] classLoader) {
		URL url;
		for (ClassLoader cl : classLoader) {
			if (null != cl) {
				// look for the resource as passed in...
				url = cl.getResource(resource);
				// ...but some class loaders want this leading "/", so we'll add it
				// and try again if we didn't find the resource
				if (null == url)
					url = cl.getResource("/" + resource);
				// "It's always in the last place I look for it!"
				// ... because only an idiot would keep looking for it after finding it, so stop looking already.
				if (null != url)
					return url;
			}
		}
		// didn't find it anywhere.
		return null;

	}

	Class<?> classForName(String name, ClassLoader[] classLoader) throws ClassNotFoundException {
		for (ClassLoader cl : classLoader) {
			if (null != cl) {
				try {
					Class<?> c = Class.forName(name, true, cl);
					if (null != c)
						return c;
				} catch (ClassNotFoundException e) {
					// we'll ignore this until all classloaders fail to locate the class
				}
			}
		}
		throw new ClassNotFoundException("Cannot find class: " + name);
	}

	ClassLoader[] getClassLoaders(ClassLoader classLoader) {
		return new ClassLoader[] { classLoader, defaultClassLoader, Thread.currentThread().getContextClassLoader(), getClass().getClassLoader(),
				systemClassLoader };
	}
}
