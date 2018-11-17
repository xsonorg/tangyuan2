package org.xson.tangyuan.log;

import java.lang.reflect.Constructor;

public final class LogFactory {

	public static final String					MARKER			= "TANGYUAN";

	private static Constructor<? extends Log>	logConstructor	= null;

	private static LogConfig					conf			= null;

	static {
		try {
			conf = new LogConfig();
			conf.init();
		} catch (Throwable e) {
		}

		tryImplementation(new Runnable() {
			public void run() {
				useLog4JLogging();
			}
		});

		tryImplementation(new Runnable() {
			public void run() {
				useSlf4jLogging();
			}
		});
	}

	private LogFactory() {
		// disable construction
	}

	public static Log getLog(Class<?> aClass) {
		return getLog(aClass.getName());
	}

	public static Log getLog(String logger) {
		try {
			Log proxy = logConstructor.newInstance(new Object[] { logger });
			((AbstractLog) proxy).setEnableContextLog(conf.isEnableContextLog());
			return new TangYuanLog(proxy, conf);
		} catch (Throwable t) {
			throw new RuntimeException("Error creating logger for logger " + logger + ".  Cause: " + t, t);
		}
	}

	public static synchronized void useSlf4jLogging() {
		setImplementation(org.xson.tangyuan.log.slf4j.Slf4jProxy.class);
	}

	public static synchronized void useLog4JLogging() {
		setImplementation(org.xson.tangyuan.log.log4j.Log4jProxy.class);
	}

	private static void tryImplementation(Runnable runnable) {
		if (logConstructor == null) {
			try {
				runnable.run();
			} catch (Throwable t) {
				// ignore
			}
		}
	}

	private static void setImplementation(Class<? extends Log> implClass) {
		try {
			Constructor<? extends Log> candidate = implClass.getConstructor(new Class[] { String.class });
			Log log = candidate.newInstance(new Object[] { LogFactory.class.getName() });
			log.debug("Logging initialized using '" + implClass + "' adapter.");
			logConstructor = candidate;
		} catch (Throwable t) {
			throw new RuntimeException("Error setting Log implementation.  Cause: " + t, t);
		}
	}
}
