package org.xson.tangyuan.mq.executor;

public interface MqTransactionObject {

	public enum State {
		COMMIT, ROLLBACK, INITIAL
	}

	public void commit() throws Throwable;

	public void rollback();

	public void close();

	public State getState();
}
