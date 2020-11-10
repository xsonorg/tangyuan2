package org.xson.tangyuan.sql.datasource;

import org.xson.tangyuan.TangYuanException;

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
}
