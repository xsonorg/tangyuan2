package org.xson.tangyuan.log.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.xson.tangyuan.log.AbstractLog;
import org.xson.tangyuan.service.runtime.RuntimeContext;

public class Slf4jProxy extends AbstractLog {

	private Logger log = null;

	// public Slf4jProxy(String clazz, boolean enableContextLog) {
	// this.log = LoggerFactory.getLogger(clazz);
	// this.enableContextLog = enableContextLog;
	// }
	// public Slf4jProxy(Logger logger, boolean enableContextLog) {
	// this.log = logger;
	// this.enableContextLog = enableContextLog;
	// }

	public Slf4jProxy(String clazz) {
		this.log = LoggerFactory.getLogger(clazz);
	}

	public Slf4jProxy(Logger logger) {
		this.log = logger;
	}

	private void setContextLog() {
		if (null != logExt && logExt.isEnableContextLog()) {
			try {
				MDC.clear();
				RuntimeContext rc = RuntimeContext.get();
				if (null != rc) {
					MDC.put(RuntimeContext.HEADER_KEY_TRACE_ID, rc.getTraceId());
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	//	private void setContextLog() {
	//		if (enableContextLog) {
	//			MDC.clear();
	//			RuntimeContext rc = RuntimeContext.get();
	//			if (null != rc) {
	//				MDC.put(RuntimeContext.HEADER_KEY_TRACE_ID, rc.getTraceId());
	//				MDC.put(RuntimeContext.HEADER_KEY_ORIGIN, rc.getOrigin());
	//				MDC.put(RuntimeContext.HEADER_KEY_COMPONENT, rc.getComponent());
	//			}
	//		}
	//	}

	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}

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
