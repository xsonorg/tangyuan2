package org.xson.tangyuan.service.mongo.sql;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.TangYuanLang;

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

	public static SqlParseException createLang(String message) {
		return new SqlParseException(TangYuanLang.get(message));
	}

	public static SqlParseException createLang(String message, Object... args) {
		return new SqlParseException(TangYuanLang.get(message, args));
	}
}
