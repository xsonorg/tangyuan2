package org.xson.tangyuan.hive.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.xson.tangyuan.hive.transaction.XConnection.ConnectionState;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.sql.datasource.DataSourceManager;

public class MultipleTransactionManager implements XTransactionManager {

	private static Log			log	= LogFactory.getLog(MultipleTransactionManager.class);

	protected DataSourceManager	dataSources;

	public MultipleTransactionManager(DataSourceManager dataSources) {
		this.dataSources = dataSources;
	}

	@Override
	public XTransactionStatus getTransaction(String dsKey, XTransactionDefinition definition, XTransactionStatus currentStatus) {
		try {
			XTransactionStatus status = null;
			if (null == dsKey) {
				// 1. 只开启事务
				status = createTransactionOnly(definition, currentStatus);
			} else if (null == definition) {
				// 2. 只是打开连接
				status = createTransaction(dsKey, definition, currentStatus);
			} else {
				// 3.开启事务，并处理连接
				status = createTransaction(dsKey, definition, currentStatus);
			}
			return status;
		} catch (Throwable e) {
			// 这里发生异常代表创建事务或延续事务失败, 连接已经回收, 所有上层只需要简单处理即可
			throw new TransactionException(e);
		}

	}

	/**
	 * 创建一个新事务(ONLY), 并不打开数据库链接
	 */
	private XTransactionStatus createTransactionOnly(XTransactionDefinition definition, XTransactionStatus currentStatus) {
		if (null == currentStatus) {
			XTransactionStatus status = new XTransactionStatus();
			// status.hasTransaction = !definition.isAutoCommit();
			// status.newTransaction = definition.isNewTranscation();
			// status.name = definition.getName();
			// status.definition = definition;
			// status.initialization = false; // 未初始化
			return status;
		}
		return currentStatus;
	}

	private XTransactionStatus createTransaction(String dsKey, XTransactionDefinition definition, XTransactionStatus currentStatus) throws Throwable {
		if (null == currentStatus) {
			currentStatus = createTransactionOnly(definition, currentStatus);
		}
		if (!currentStatus.existXConnection(dsKey)) {
			XConnection newXConnection = createXConnection(dsKey, definition);
			currentStatus.addXConnection(dsKey, newXConnection);
		}
		return currentStatus;
	}

	private XConnection createXConnection(String dsKey, XTransactionDefinition definition) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSources.getConnection(dsKey);
			log.info("open new connection. dsKey[" + dsKey + "], hashCode[" + conn.hashCode() + "]");
			XConnection xConnection = new XConnection();
			xConnection.conn = conn;
			// xConnection.begin(definition);
			return xConnection;
		} catch (Throwable e) {
			if (null != conn) {
				dataSources.recycleConnection(dsKey, conn);
			}
			throw e;
		}
	}

	/**
	 * 提交事务 confirm:是否确认提交 false:自动判断是否需要提交(只提交单独的事务), true:立即提交
	 * 
	 * @throws SQLException
	 *             提交过程中发生异常, 上抛
	 */
	public XTransactionStatus commit(XTransactionStatus status, boolean confirm) throws SQLException {

		if (!confirm) {
			log.info("commit ignore");
			return status;
		}

		// 存在事务
		if (null != status.xConnection) {
			Connection conn = status.xConnection.getConnection();
			// conn.commit();
			dataSources.recycleConnection(status.dsKey, conn);// 这里不能抛出异常, 内部处理
			status.xConnection.connState = ConnectionState.COMMIT;
		} else if (null != status.connMap) {
			for (Map.Entry<String, XConnection> entry : status.connMap.entrySet()) {
				Connection conn = entry.getValue().getConnection();
				// conn.commit();
				dataSources.recycleConnection(entry.getKey(), conn);// 这里不能抛出异常, 内部处理
				entry.getValue().connState = ConnectionState.COMMIT;
			}
		}

		log.info("commit success");
		// 回复挂起的服务

		return null;
	}

	@Override
	public XTransactionStatus rollback(XTransactionStatus status) throws SQLException {
		// 存在事务
		if (null != status.xConnection) {
			Connection conn = status.xConnection.getConnection();
			// conn.commit();
			dataSources.recycleConnection(status.dsKey, conn);// 这里不能抛出异常, 内部处理
			status.xConnection.connState = ConnectionState.ROLLBACK;
		} else if (null != status.connMap) {
			for (Map.Entry<String, XConnection> entry : status.connMap.entrySet()) {
				Connection conn = entry.getValue().getConnection();
				// conn.commit();
				dataSources.recycleConnection(entry.getKey(), conn);// 这里不能抛出异常, 内部处理
				entry.getValue().connState = ConnectionState.ROLLBACK;
			}
		}

		log.info("rollback");
		return null;
	}

}
