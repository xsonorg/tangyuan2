package org.xson.tangyuan;

public class TangYuanException extends RuntimeException {

	private static final long	serialVersionUID	= -1L;

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

}
