package org.xson.tangyuan.mongo.datasource;

public class DataSourceException extends RuntimeException {

	private static final long	serialVersionUID	= 1L;

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
