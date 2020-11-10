package org.xson.tangyuan.log;

import org.xson.tangyuan.log.ext.LogExt;

/**
 * TangYuan日志, 顶层的控制(过滤的控制)
 */
public class TangYuanLog extends AbstractLog {

	private Log proxy = null;

	protected TangYuanLog(Log proxy, LogExt ext) {
		this.proxy = proxy;
		((AbstractLog) this.proxy).setLogExt(ext);
		this.logExt = ext;
	}

	@Override
	public boolean isDebugEnabled() {
		return proxy.isDebugEnabled();
	}

	@Override
	public boolean isTraceEnabled() {
		return proxy.isTraceEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return proxy.isInfoEnabled();
	}

	@Override
	public void error(Throwable e) {
		if (isExclude()) {
			return;
		}
		proxy.error(e);
	}

	@Override
	public void error(Throwable e, String s, Object... args) {
		if (isExclude()) {
			return;
		}
		proxy.error(e, s, args);
	}

	@Override
	public void error(String s, Throwable e) {
		if (isExclude()) {
			return;
		}
		proxy.error(s, e);
	}

	@Override
	public void error(String s) {
		if (isExclude()) {
			return;
		}
		proxy.error(s);
	}

	@Override
	public void error(String s, Object... args) {
		if (isExclude()) {
			return;
		}
		proxy.error(s, args);
	}

	@Override
	public void info(String s) {
		if (isExclude()) {
			return;
		}
		proxy.info(s);
	}

	@Override
	public void info(String s, Object... args) {
		if (isExclude()) {
			return;
		}
		proxy.info(s, args);
	}

	@Override
	public void debug(String s) {
		if (isExclude()) {
			return;
		}
		proxy.debug(s);
	}

	@Override
	public void debug(String s, Object... args) {
		if (isExclude()) {
			return;
		}
		proxy.debug(s, args);
	}

	@Override
	public void trace(String s) {
		if (isExclude()) {
			return;
		}
		proxy.trace(s);
	}

	@Override
	public void trace(String s, Object... args) {
		if (isExclude()) {
			return;
		}
		proxy.trace(s, args);
	}

	@Override
	public void warn(String s) {
		if (isExclude()) {
			return;
		}
		proxy.warn(s);
	}

	@Override
	public void warn(String s, Object... args) {
		if (isExclude()) {
			return;
		}
		proxy.warn(s, args);
	}

}
