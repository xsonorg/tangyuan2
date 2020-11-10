package org.xson.tangyuan.log.jdk;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.xson.tangyuan.log.AbstractLog;

/**
 * JDK LOG日志代理
 */
public class JdkLogProxy extends AbstractLog {

	private Logger log = null;

	public JdkLogProxy(String clazz) {
		this.log = Logger.getLogger(clazz);
	}

	private void setContextLog() {
	}

	@Override
	public boolean isDebugEnabled() {
		return true;
	}

	@Override
	public boolean isTraceEnabled() {
		return true;
	}

	@Override
	public boolean isInfoEnabled() {
		return true;
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
		//		setContextLog();
		log.log(Level.SEVERE, s, e);
	}

	@Override
	public void error(String s, Object... args) {
		error(format(s, args));
	}

	@Override
	public void error(String s) {
		//		setContextLog();
		log.log(Level.SEVERE, s);
	}

	// ########################################## INFO

	@Override
	public void info(String s, Object... args) {
		info(format(s, args));
	}

	@Override
	public void info(String s) {
		//		setContextLog();
		log.info(s);
	}

	// ########################################## DEBUG

	@Override
	public void debug(String s, Object... args) {
		debug(format(s, args));
	}

	@Override
	public void debug(String s) {
		//		setContextLog();
		log.log(Level.FINE, s);
	}

	// ########################################## TRACE

	@Override
	public void trace(String s, Object... args) {
		trace(format(s, args));
	}

	@Override
	public void trace(String s) {
		setContextLog();
		//		log.trace(s);
		log.log(Level.FINEST, s);
	}

	// ########################################## WARN

	@Override
	public void warn(String s, Object... args) {
		warn(format(s, args));
	}

	@Override
	public void warn(String s) {
		//		setContextLog();
		log.log(Level.WARNING, s);
	}

}
