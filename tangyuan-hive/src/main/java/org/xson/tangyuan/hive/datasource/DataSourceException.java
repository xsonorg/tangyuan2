package org.xson.tangyuan.hive.datasource;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.TangYuanLang;

public class DataSourceException extends TangYuanException {

	private static final long serialVersionUID = 2299470366572557098L;

	public DataSourceException() {
		super();
	}

	public DataSourceException(String message) {
		super(message);
	}

	public DataSourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataSourceException(Throwable cause) {
		super(cause);
	}

	public static DataSourceException createLang(String message) {
		return new DataSourceException(TangYuanLang.get(message));
	}

	public static DataSourceException createLang(String message, Object... args) {
		return new DataSourceException(TangYuanLang.get(message, args));
	}
}
