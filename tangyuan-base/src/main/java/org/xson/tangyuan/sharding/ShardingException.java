package org.xson.tangyuan.sharding;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.TangYuanLang;

public class ShardingException extends TangYuanException {

	private static final long serialVersionUID = 9133285208115034698L;

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

	public static ShardingException createLang(String message) {
		return new ShardingException(TangYuanLang.get(message));
	}

	public static ShardingException createLang(String message, Object... args) {
		return new ShardingException(TangYuanLang.get(message, args));
	}

}
