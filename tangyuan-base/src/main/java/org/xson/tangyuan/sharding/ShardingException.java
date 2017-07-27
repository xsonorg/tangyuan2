package org.xson.tangyuan.sharding;

import org.xson.tangyuan.TangYuanException;

public class ShardingException extends TangYuanException {

	private static final long	serialVersionUID	= 9133285208115034698L;

	public ShardingException() {
		super();
	}

	public ShardingException(String message) {
		super(message);
	}

	public ShardingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShardingException(Throwable cause) {
		super(cause);
	}

}
