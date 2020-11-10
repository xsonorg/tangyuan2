package org.xson.tangyuan.log.log4j;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.xson.tangyuan.log.AbstractLog;
import org.xson.tangyuan.service.runtime.RuntimeContext;

/**
 * Log4j日志代理
 */
public class Log4jProxy extends AbstractLog {

	private Logger log = null;

	public Log4jProxy(String clazz) {
		this.log = Logger.getLogger(clazz);
	}

	private void setContextLog() {
		if (null != logExt && logExt.isEnableContextLog()) {
			try {
				MDC.clear();
				RuntimeContext rc = RuntimeContext.get();
				if (null != rc) {
					MDC.put(RuntimeContext.HEADER_KEY_TRACE_ID, rc.getTraceId());
					//					MDC.put(RuntimeContext.HEADER_KEY_ORIGIN, rc.getOrigin());
					//					MDC.put(RuntimeContext.HEADER_KEY_COMPONENT, rc.getComponent());
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	//	private void setContextLog() {
	//		if (enableContextLog) {
	//			try {
	//				MDC.clear();
	//				RuntimeContext rc = RuntimeContext.get();
	//				if (null != rc) {
	//					MDC.put(RuntimeContext.HEADER_KEY_TRACE_ID, rc.getTraceId());
	//					MDC.put(RuntimeContext.HEADER_KEY_ORIGIN, rc.getOrigin());
	//					MDC.put(RuntimeContext.HEADER_KEY_COMPONENT, rc.getComponent());
	//				}
	//			} catch (Throwable e) {
	//				e.printStackTrace();
	//			}
	//		}
	//	}

	@Override
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	@Override
	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	// ########################################## ERROR

	@Override
	public void error(Throwable e) {
		error(null, e);
	}

	@Override
	public void error(Throwable e, String s, Object... args) {
		error(format(s, args), e);
	}

	@Override
	public void error(String s, Throwable e) {
		setContextLog();
		log.error(s, e);
	}

	@Override
	public void error(String s, Object... args) {
		error(format(s, args));
	}

	@Override
	public void error(String s) {
		setContextLog();
		log.error(s);
	}

	// ########################################## INFO

	@Override
	public void info(String s, Object... args) {
		info(format(s, args));
	}

	@Override
	public void info(String s) {
		setContextLog();
		log.info(s);
	}

	// ########################################## DEBUG

	@Override
	public void debug(String s, Object... args) {
		debug(format(s, args));
	}

	@Override
	public void debug(String s) {
		setContextLog();
		log.debug(s);
	}

	// ########################################## TRACE

	@Override
	public void trace(String s, Object... args) {
		trace(format(s, args));
	}

	@Override
	public void trace(String s) {
		setContextLog();
		log.trace(s);
	}

	// ########################################## WARN

	@Override
	public void warn(String s, Object... args) {
		warn(format(s, args));
	}

	@Override
	public void warn(String s) {
		setContextLog();
		log.warn(s);
	}

}
