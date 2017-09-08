package org.xson.tangyuan.share.util;

import java.util.HashMap;
import java.util.Map;

public class ClassUtils {

	private static final Map<String, Class<?>>	primitiveTypeNameMap	= new HashMap<String, Class<?>>(32);

	private static final Map<String, Class<?>>	commonClassCache		= new HashMap<String, Class<?>>(32);

	public static Class<?> resolvePrimitiveClassName(String name) {
		Class<?> result = null;
		if (name != null && name.length() <= 8) {
			result = primitiveTypeNameMap.get(name);
		}
		return result;
	}

	public static Class<?> forName(String name) {
		// Assert.notNull(name, "Name must not be null");

		Class<?> clazz = resolvePrimitiveClassName(name);
		if (clazz == null) {
			clazz = commonClassCache.get(name);
		}
		if (clazz != null) {
			return clazz;
		}
		ClassLoader clToUse = getDefaultClassLoader();
		try {
			return (clToUse != null ? clToUse.loadClass(name) : Class.forName(name));
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = ClassUtils.class.getClassLoader();
			if (cl == null) {
				// getClassLoader() returning null indicates the bootstrap ClassLoader
				try {
					cl = ClassLoader.getSystemClassLoader();
				} catch (Throwable ex) {
					// Cannot access system ClassLoader - oh well, maybe the caller can live with null...
				}
			}
		}
		return cl;
	}

}
