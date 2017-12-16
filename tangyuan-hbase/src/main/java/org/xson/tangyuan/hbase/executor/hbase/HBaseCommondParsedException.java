package org.xson.tangyuan.hbase.executor.hbase;

import org.xson.tangyuan.TangYuanException;

public class HBaseCommondParsedException extends TangYuanException {

	private static final long serialVersionUID = 1L;

	public HBaseCommondParsedException(String msg) {
		super(msg);
	}

	public HBaseCommondParsedException(Throwable cause) {
		super(cause);
	}

	public HBaseCommondParsedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
