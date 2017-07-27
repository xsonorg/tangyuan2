package org.xson.tangyuan.mongo.executor.sql;

import org.xson.tangyuan.TangYuanException;

/**
 * SQL脚本解析异常
 */
public class SqlParseException extends TangYuanException {

	private static final long serialVersionUID = -1L;

	public SqlParseException(String msg) {
		super(msg);
	}

	public SqlParseException(Throwable cause) {
		super(cause);
	}

	public SqlParseException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
