package org.xson.tangyuan.log;

/**
 * Tangyuan日志接口
 */
public interface Log {

	boolean isDebugEnabled();

	boolean isTraceEnabled();

	boolean isInfoEnabled();

	void error(Throwable e);

	void error(Throwable e, String s, Object... args);

	void error(String s, Throwable e);

	void error(String s);

	void error(String s, Object... args);

	void info(String s);

	void info(String s, Object... args);

	void debug(String s);

	void debug(String s, Object... args);

	void trace(String s);

	void trace(String s, Object... args);

	void warn(String s);

	void warn(String s, Object... args);

}
