
package org.xson.tangyuan.type;

import org.xson.tangyuan.TangYuanException;

public class TypeException extends TangYuanException {

	private static final long serialVersionUID = 8614420898975117130L;

	public TypeException() {
		super();
	}

	public TypeException(String message) {
		super(message);
	}

	public TypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public TypeException(Throwable cause) {
		super(cause);
	}

}
