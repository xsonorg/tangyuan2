package org.xson.tangyuan.log;

import org.xson.tangyuan.log.ext.LogExt;
import org.xson.tangyuan.service.runtime.RuntimeContext;
import org.xson.tangyuan.util.MessageFormatter;

public abstract class AbstractLog implements Log {

	protected LogExt logExt = null;

	protected void setLogExt(LogExt logExt) {
		this.logExt = logExt;
	}

	protected String format(String str, Object... args) {
		//		if (null == args || 0 == args.length) {
		//			return str;
		//		}
		//		for (int i = 0; i < args.length; i++) {
		//			str = str.replaceFirst("\\{\\}", String.valueOf(args[i]));
		//		}
		//		return str;
		return MessageFormatter.formatArgs(str, args);
	}

	protected boolean isExclude() {
		if (null != logExt) {
			RuntimeContext rc = RuntimeContext.get();
			if (null != rc) {
				String origin = rc.getOrigin();
				if (null != origin) {
					return logExt.isExclude(origin);
				}
			}
		}
		return false;
	}

	////////////////////////////////////////////////////////

	@Override
	public void errorLang(Throwable e, String s, Object... args) {
		error(e, TangYuanLang.get(s, args));
	}

	@Override
	public void errorLang(String s, Throwable e) {
		error(TangYuanLang.get(s), e);
	}

	@Override
	public void errorLang(String s) {
		error(TangYuanLang.get(s));
	}

	@Override
	public void errorLang(String s, Object... args) {
		error(TangYuanLang.get(s, args));
	}

	@Override
	public void infoLang(String s) {
		info(TangYuanLang.get(s));
	}

	@Override
	public void infoLang(String s, Object... args) {
		info(TangYuanLang.get(s, args));
	}

	@Override
	public void debugLang(String s) {
		debug(TangYuanLang.get(s));
	}

	@Override
	public void debugLang(String s, Object... args) {
		debug(TangYuanLang.get(s, args));
	}

	@Override
	public void warnLang(String s) {
		warn(TangYuanLang.get(s));
	}

	@Override
	public void warnLang(String s, Object... args) {
		warn(TangYuanLang.get(s, args));
	}

	//	protected boolean isExclude() {
	//		final LogExpandVo conf = expandVo;
	//		if (null != conf) {
	//			String         type      = null;
	//			String         component = null;
	//			RuntimeContext rc        = RuntimeContext.get();
	//			if (null != rc) {
	//				type = rc.getOrigin();
	//				component = rc.getComponent();
	//				if (0 == component.length()) { // optimize
	//					component = null;
	//				}
	//			}
	//			return conf.isExclude(type, component);
	//		}
	//		return false;
	//	}
	//	protected static boolean   enableContextLog = false;
	//	private static LogExpandVo expandVo         = null;
	//	public static void setExpandVo(LogExpandVo newExpandVo) {
	//		expandVo = newExpandVo;
	//		enableContextLog = newExpandVo.isEnableContextLog();
	//	}
	//	protected LogConfig	conf				= null;
	//	protected void setEnableContextLog(boolean enableContextLog) {
	//		this.enableContextLog = enableContextLog;
	//	}
	//	protected LogExpandVo getExpandVo() {
	//	return expandVo;
	//}
}
