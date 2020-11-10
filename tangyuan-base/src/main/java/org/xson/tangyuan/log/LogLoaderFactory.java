package org.xson.tangyuan.log;

import java.lang.reflect.Method;

import org.xson.tangyuan.manager.conf.ResourceReloader;

/**
 * 日志加载管理器
 * 
 * 1. 用于动态检测使用的日志框架 2. 获取日志配置
 */
public class LogLoaderFactory implements ResourceReloader {

	private static LogLoader		logLoader	= null;

	private static LogLoaderFactory	instance	= new LogLoaderFactory();

	public static LogLoaderFactory getInstance() {
		return instance;
	}

	private LogLoaderFactory() {
		// disable construction
	}

	public static void load(String type, String resource) {
		if (null == type) {
			tryImplementation(new Runnable() {
				public void run() {
					testLog4JLogging(resource);
				}
			});
			tryImplementation(new Runnable() {
				public void run() {
					testSlf4jLogging(resource);
				}
			});
		} else {
			if ("Log4J".equalsIgnoreCase(type)) {
				tryImplementation(new Runnable() {
					public void run() {
						testLog4JLogging(resource);
					}
				});
			} else if ("Slf4j".equalsIgnoreCase(type)) {
				tryImplementation(new Runnable() {
					public void run() {
						testSlf4jLogging(resource);
					}
				});
			}
		}
	}

	public static synchronized void testSlf4jLogging(String resource) {
		testLoader(org.xson.tangyuan.log.log4j.Log4jLoader.class, "load", resource);
	}

	public static synchronized void testLog4JLogging(String resource) {
		testLoader(org.xson.tangyuan.log.log4j.Log4jLoader.class, "load", resource);
	}

	private static void tryImplementation(Runnable runnable) {
		if (logLoader == null) {
			try {
				runnable.run();
			} catch (Throwable t) {
				// ignore
			}
		}
	}

	@Override
	public void reload(String resource) throws Throwable {
		try {
			if (null != logLoader) {
				logLoader.reload(resource);
			}
		} catch (Throwable e) {
			throw e;
		}
	}

	private static void testLoader(Class<? extends LogLoader> loaderClass, String methodName, String resource) {
		try {
			LogLoader instance = loaderClass.getConstructor().newInstance();
			Method method = loaderClass.getMethod(methodName, String.class);
			method.invoke(instance, new Object[] { resource });
			logLoader = instance;
		} catch (Throwable t) {
		}
	}
}
