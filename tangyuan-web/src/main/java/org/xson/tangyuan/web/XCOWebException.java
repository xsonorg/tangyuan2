package org.xson.tangyuan.web;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.TangYuanLang;

public class XCOWebException extends TangYuanException {

	private static final long serialVersionUID = -1L;

	public XCOWebException() {
		super();
	}

	public XCOWebException(String msg) {
		super(msg);
	}

	public XCOWebException(Throwable cause) {
		super(cause);
	}

	public XCOWebException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public static XCOWebException createLang(String message) {
		return new XCOWebException(TangYuanLang.get(message));
	}

	public static XCOWebException createLang(String message, Object... args) {
		return new XCOWebException(TangYuanLang.get(message, args));
	}
}
