package org.xson.tangyuan.hive.transaction;

import java.sql.Connection;
import java.sql.SQLException;

public class XConnection {

	protected static enum ConnectionState {
		ROLLBACK, COMMIT
	}

	protected Connection		conn		= null;

	/** 事务进展[0: 初始化, 1:rollback, 2:commit] */
	protected ConnectionState	connState	= null;

	/** 首次开启 */
	protected void begin(XTransactionDefinition definition) throws SQLException {
	}

	/**
	 * 之前不存在事务
	 */
	public void beginTransaction(XTransactionDefinition definition) throws SQLException {
	}

	/**
	 * 检测之前，如果不存在事务，则设置事务
	 */
	public void checkSetTransaction(XTransactionDefinition definition) throws SQLException {
	}

	public Connection getConnection() {
		return conn;
	}

}
