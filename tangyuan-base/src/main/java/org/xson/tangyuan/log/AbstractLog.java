package org.xson.tangyuan.log;

import org.xson.tangyuan.runtime.RuntimeContext;

public abstract class AbstractLog implements Log {

	protected boolean	enableContextLog	= false;

	protected LogConfig	conf				= null;

	protected void setEnableContextLog(boolean enableContextLog) {
		this.enableContextLog = enableContextLog;
	}

	// protected void setContextLog() {
	// if (enableContextLog) {
	// MDC.clear();
	// RuntimeContext rc = RuntimeContext.get();
	// if (null != rc) {
	// MDC.put(RuntimeContext.HEADER_KEY_TRACE_ID, rc.getTraceId());
	// MDC.put(RuntimeContext.HEADER_KEY_LOG_TYPE, rc.getLogType());
	// MDC.put(RuntimeContext.HEADER_KEY_COMPONENT, rc.getComponent());
	// }
	// }
	// }

	protected String format(String str, Object... args) {
		if (null == args || 0 == args.length) {
			return str;
		}
		for (int i = 0; i < args.length; i++) {
			str = str.replaceFirst("\\{\\}", String.valueOf(args[i]));
		}
		return str;
	}

	protected boolean isExclude() {
		if (null != conf) {
			String type = null;
			String component = null;
			RuntimeContext rc = RuntimeContext.get();
			if (null != rc) {
				type = rc.getLogType();
				component = rc.getComponent();
			}
			return conf.isExclude(type, component);
		}
		return false;
	}
}
