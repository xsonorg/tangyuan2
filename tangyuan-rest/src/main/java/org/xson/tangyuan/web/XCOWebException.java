package org.xson.tangyuan.web;

public class XCOWebException extends RuntimeException {
	
	private static final long	serialVersionUID	= -1L;

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
}
