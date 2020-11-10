package org.xson.tangyuan;

import org.xson.tangyuan.log.TangYuanLang;

public class TangYuanException extends RuntimeException {

	private static final long serialVersionUID = -1L;

	public TangYuanException() {
		super();
	}

	public TangYuanException(String msg) {
		super(msg);
	}

	public TangYuanException(Throwable cause) {
		super(cause);
	}

	public TangYuanException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public static TangYuanException createLang(String message) {
		return new TangYuanException(TangYuanLang.get(message));
	}

	public static TangYuanException createLang(String message, Object... args) {
		return new TangYuanException(TangYuanLang.get(message, args));
	}
}
