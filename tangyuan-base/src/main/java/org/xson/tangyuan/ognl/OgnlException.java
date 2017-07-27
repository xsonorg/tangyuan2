package org.xson.tangyuan.ognl;

import org.xson.tangyuan.TangYuanException;

public class OgnlException extends TangYuanException {

	private static final long	serialVersionUID	= -2645663856216676753L;

	public OgnlException() {
		super();
	}

	public OgnlException(String message) {
		super(message);
	}

	public OgnlException(String message, Throwable cause) {
		super(message, cause);
	}

	public OgnlException(Throwable cause) {
		super(cause);
	}

}
