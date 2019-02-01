package org.xson.tangyuan.log;

import org.xson.tangyuan.runtime.RuntimeContext;

public abstract class AbstractLog implements Log {

	protected boolean	enableContextLog	= false;

	protected LogConfig	conf				= null;

	protected void setEnableContextLog(boolean enableContextLog) {
		this.enableContextLog = enableContextLog;
	}

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
				type = rc.getOrigin();
				component = rc.getComponent();
				if (0 == component.length()) { // optimize
					component = null;
				}
			}
			return conf.isExclude(type, component);
		}
		return false;
	}
}
