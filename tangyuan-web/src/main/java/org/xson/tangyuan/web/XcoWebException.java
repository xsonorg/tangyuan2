package org.xson.tangyuan.web;

public class XcoWebException extends RuntimeException {
	
	private static final long	serialVersionUID	= -1L;

	public XcoWebException() {
		super();
	}

	public XcoWebException(String msg) {
		super(msg);
	}

	public XcoWebException(Throwable cause) {
		super(cause);
	}

	public XcoWebException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
