package org.xson.tangyuan.sql.transaction;

import org.xson.tangyuan.TangYuanException;

public class TransactionException extends TangYuanException {

	private static final long serialVersionUID = 1L;

	public TransactionException() {
		super();
	}

	public TransactionException(String message) {
		super(message);
	}

	public TransactionException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransactionException(Throwable cause) {
		super(cause);
	}

	// 是否对当前事务有影响,
	// private boolean affect = false;
	// public TransactionException(String message, boolean affect) {
	// super(message);
	// this.affect = affect;
	// }
	// public TransactionException(boolean affect, Throwable cause) {
	// super(cause);
	// this.affect = affect;
	// }
	// public boolean isAffect() {
	// return affect;
	// }

}
