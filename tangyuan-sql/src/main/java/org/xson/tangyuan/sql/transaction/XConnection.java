package org.xson.tangyuan.sql.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

public class XConnection {

	protected static enum ConnectionState {
		ROLLBACK, COMMIT
	}

	// private static Log log = LogFactory.getLog(XConnection.class);

	private static final String	SAVEPOINT_NAME_PREFIX	= "SAVEPOINT_";

	protected Connection		conn					= null;

	private Savepoint			savepoint				= null;

	private int					savepointCounter		= 0;

	/**
	 * 事务进展[0: 初始化, 1:rollback, 2:commit]
	 */
	protected ConnectionState	connState				= null;

	// TODO ISO设置顺序

	// public void begin(String dsKey, DataSourceManager dataSources, XTransactionDefinition definition, boolean autoCommit) throws SQLException {
	// this.conn = dataSources.getConnection(dsKey);
	// int isolation = this.conn.getTransactionIsolation();
	// if (XTransactionDefinition.ISOLATION_DEFAULT != definition.getIsolation() && isolation != definition.getIsolation()) {
	// this.conn.setTransactionIsolation(definition.getIsolation());
	// }
	// if (autoCommit != this.conn.getAutoCommit()) {
	// this.conn.setAutoCommit(autoCommit);
	// }
	// // TODO
	// // if (definition.isReadOnly() && !this.conn.isReadOnly()) {
	// // this.conn.setReadOnly(definition.isReadOnly());
	// // }
	// log.info("open new connection. dsKey[" + dsKey + "], hashCode[" + this.conn.hashCode() + "]");
	// }

	// protected void begin(XTransactionDefinition definition, boolean autoCommit) throws SQLException {
	// // this.conn = conn;
	//
	// // TODO
	//
	// int isolation = this.conn.getTransactionIsolation();
	// if (XTransactionDefinition.ISOLATION_DEFAULT != definition.getIsolation() && isolation != definition.getIsolation()) {
	// this.conn.setTransactionIsolation(definition.getIsolation());
	// }
	// if (autoCommit != this.conn.getAutoCommit()) {
	// this.conn.setAutoCommit(autoCommit);
	// }
	// if (definition.isReadOnly() != this.conn.isReadOnly()) {
	// this.conn.setReadOnly(definition.isReadOnly());
	// }
	// }

	/**
	 * 首次开启
	 */
	protected void begin(XTransactionDefinition definition) throws SQLException {

		boolean autoCommit = definition.isAutoCommit();

		// 1. 只读设置
		if (definition.isReadOnly() != this.conn.isReadOnly()) {
			this.conn.setReadOnly(definition.isReadOnly());
		}

		// 2. 隔离级别设置
		if (autoCommit) {
			// TODO, 非事务下的隔离级别, 需要测试
			if (XTransactionDefinition.ISOLATION_DEFAULT != definition.getIsolation()) {
				this.conn.setTransactionIsolation(XTransactionDefinition.ISOLATION_DEFAULT);// 非事务
			}
		} else {
			int connIsolation = this.conn.getTransactionIsolation();
			if (XTransactionDefinition.ISOLATION_DEFAULT != definition.getIsolation() && connIsolation != definition.getIsolation()) {
				this.conn.setTransactionIsolation(definition.getIsolation());
			}
		}

		// 3. 事务设置
		if (autoCommit != this.conn.getAutoCommit()) {
			this.conn.setAutoCommit(autoCommit);
		}
	}

	// /**
	// * 之前不存在事务
	// */
	// public void beginTransaction(XTransactionDefinition definition) throws SQLException {
	// int isolation = this.conn.getTransactionIsolation();
	// if (XTransactionDefinition.ISOLATION_DEFAULT != definition.getIsolation() && isolation != definition.getIsolation()) {
	// this.conn.setTransactionIsolation(definition.getIsolation());
	// }
	// if (this.conn.getAutoCommit()) {
	// this.conn.setAutoCommit(false);
	// }
	// // TODO 暂不考虑只读
	// }

	/**
	 * 之前不存在事务
	 */
	public void beginTransaction(XTransactionDefinition definition) throws SQLException {
		// TODO 暂不考虑只读
		int isolation = this.conn.getTransactionIsolation();
		if (XTransactionDefinition.ISOLATION_DEFAULT != definition.getIsolation() && isolation != definition.getIsolation()) {
			this.conn.setTransactionIsolation(definition.getIsolation());
		}
		this.conn.setAutoCommit(false);
	}

	/**
	 * 检测之前，如果不存在事务，则设置事务
	 */
	public void checkSetTransaction(XTransactionDefinition definition) throws SQLException {
		if (this.conn.getAutoCommit()) {
			beginTransaction(definition);
		}
	}

	public void setSavepoint() throws SQLException {
		// TODO
		// if (null != this.savepoint) {
		// throw new SQLException("TransactionStatus has Savepoint");
		// }
		if (!conn.getMetaData().supportsSavepoints()) {
			throw new SQLException("SavepointManager does not support Savepoint.");
		}
		this.savepoint = conn.setSavepoint(SAVEPOINT_NAME_PREFIX + this.savepointCounter++);
	}

	public Connection getConnection() {
		return conn;
	}

	public Savepoint getSavepoint() {
		return savepoint;
	}

	public int getSavepointCounter() {
		return savepointCounter;
	}

}
