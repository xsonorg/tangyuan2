package org.xson.tangyuan.sql.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.sql.datasource.DataSourceManager;
import org.xson.tangyuan.sql.transaction.XConnection.ConnectionState;

public class MultipleTransactionManager implements XTransactionManager {

	private static Log			log	= LogFactory.getLog(MultipleTransactionManager.class);

	protected DataSourceManager	dataSources;

	public MultipleTransactionManager(DataSourceManager dataSources) {
		this.dataSources = dataSources;
	}

	@Override
	public XTransactionStatus getTransaction(String dsKey, XTransactionDefinition definition, XTransactionStatus currentStatus) {
		if (null == definition && null == currentStatus) {
			throw new TransactionException("definition and currentStatus is null");// 没有匹配的事务定义
		}
		try {
			XTransactionStatus status = null;
			if (null == dsKey) { // 1. 只开启事务
				if (null == currentStatus) {
					status = createTransactionOnly(definition);
				} else {
					status = handleExistingTransactionOnly(definition, currentStatus);
				}
			} else if (null == definition) { // 2. 只是打开连接
				// 这里一定存在事务
				openConnectionOnly(dsKey, currentStatus);
				status = currentStatus;
			} else { // 3.开启事务，并处理连接
				if (null == currentStatus) {
					status = createTransactionStatus(dsKey, definition);
				} else {
					status = handleExistingTransaction(dsKey, definition, currentStatus);
				}
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
	private XTransactionStatus createTransactionOnly(XTransactionDefinition definition) {
		XTransactionStatus status = new XTransactionStatus();
		status.hasTransaction = !definition.isAutoCommit();
		status.newTransaction = definition.isNewTranscation();
		status.name = definition.getName();
		status.definition = definition;
		status.initialization = false; // 未初始化
		return status;
	}

	/**
	 * 创建一个新事务： 只有存在确定的数据源的情况下再调用
	 */
	private XTransactionStatus createTransactionStatus(String dsKey, XTransactionDefinition definition) throws SQLException {
		XConnection xConnection = createXConnection(dsKey, definition);

		XTransactionStatus status = new XTransactionStatus();
		status.xConnection = xConnection;
		status.hasTransaction = !definition.isAutoCommit();
		status.newTransaction = definition.isNewTranscation();
		status.name = definition.getName();
		status.definition = definition;

		status.dsKey = dsKey;
		status.initialization = true;
		return status;
	}

	/**
	 * 处理存在的事务, 在当前存在事务的情况下
	 */
	private XTransactionStatus handleExistingTransactionOnly(XTransactionDefinition newDefinition, XTransactionStatus currentStatus)
			throws SQLException {
		if (XTransactionDefinition.PROPAGATION_REQUIRED == newDefinition.getBehavior()) {
			newTransaction(currentStatus);
			return currentStatus;
		} else if (XTransactionDefinition.PROPAGATION_SUPPORTS == newDefinition.getBehavior()) {
			return currentStatus;
		} else if (XTransactionDefinition.PROPAGATION_MANDATORY == newDefinition.getBehavior()) {
			if (!currentStatus.hasTransaction) {
				throw new TangYuanException("No existing transaction found for transaction marked with propagation 'mandatory'");
			}
			return currentStatus;
		} else if (XTransactionDefinition.PROPAGATION_REQUIRES_NEW == newDefinition.getBehavior()) {
			if (currentStatus.hasTransaction) {
				// 之前存在事务，挂起之前的事务,创建独立的新事物
				XTransactionStatus newStatus = createTransactionOnly(newDefinition);
				newStatus.suspendedResources = currentStatus;
				return newStatus;
			} else {
				// 之前不存在事务,沿用连接并开启新事物
				newTransaction(currentStatus);
				currentStatus.newTransaction = true;
				return currentStatus;
			}
		} else if (XTransactionDefinition.PROPAGATION_NOT_SUPPORTED == newDefinition.getBehavior()) {
			if (currentStatus.hasTransaction) {
				// 之前存在事务，挂起之前的事务,创建独立的非事物连接
				XTransactionStatus newStatus = createTransactionOnly(newDefinition);
				newStatus.suspendedResources = currentStatus;
				return newStatus;
			} else {
				currentStatus.newTransaction = true;// TODO
				return currentStatus;
			}
		} else if (XTransactionDefinition.PROPAGATION_NEVER == newDefinition.getBehavior()) {
			if (currentStatus.hasTransaction) {
				throw new TangYuanException("Existing transaction found for transaction marked with propagation 'never'");
			}
			return currentStatus;
		} else if (XTransactionDefinition.PROPAGATION_NESTED == newDefinition.getBehavior()) {
			if (currentStatus.hasTransaction) {
				currentStatus.hasSavepoint = true;
			} else {
				// 之前不存在事务,沿用连接并开启新事物
				newTransaction(currentStatus);
			}
			return currentStatus;
		}
		return null;
	}

	/**
	 * 之前不存在事务,沿用连接并开启新事物
	 */
	private void newTransaction(XTransactionStatus currentStatus) throws SQLException {
		if (!currentStatus.hasTransaction) {
			currentStatus.hasTransaction = true;
			if (currentStatus.initialization) {
				if (currentStatus.singleDs) {
					// 单数据源的情况:之前不存在事务
					currentStatus.xConnection.beginTransaction(currentStatus.definition);
				} else {
					// 多数据源的情况:之前不存在事务
					for (Map.Entry<String, XConnection> entry : currentStatus.connMap.entrySet()) {
						entry.getValue().checkSetTransaction(currentStatus.definition);
					}
				}
			}
		}
	}

	/**
	 * 只是打开连接
	 */
	private void openConnectionOnly(String dsKey, XTransactionStatus currentStatus) throws SQLException {
		if (currentStatus.initialization) {
			// 这里所有的事务设置都是一致的, 都是最初设置的, 所以不存在事务的变化和传播
			if (currentStatus.singleDs && dsKey.equalsIgnoreCase(currentStatus.dsKey)) {
				if (currentStatus.hasSavepoint) {// TODO
					currentStatus.xConnection.setSavepoint();
				}
				return;
			}
			// 多数据源的情况
			XConnection xConn = currentStatus.getXConnectionFromMap(dsKey);
			if (null == xConn) { // 如果之前不存在事务, 则之后的Connection都是开启事务的
				xConn = createXConnection(dsKey, currentStatus.definition);
				currentStatus.addXConnection(dsKey, xConn);
			}
			if (currentStatus.hasSavepoint) {
				xConn.setSavepoint();
			}
		} else {
			// 之前事务未初始化, 在这里初始化并打开首个连接
			currentStatus.xConnection = createXConnection(dsKey, currentStatus.definition);
			currentStatus.dsKey = dsKey;
			currentStatus.initialization = true;
		}
	}

	private XConnection createXConnection(String dsKey, XTransactionDefinition definition) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSources.getConnection(dsKey);
			log.info("open new connection. dsKey[" + dsKey + "], hashCode[" + conn.hashCode() + "]");
			XConnection xConnection = new XConnection();
			xConnection.conn = conn;
			xConnection.begin(definition);
			return xConnection;
		} catch (SQLException e) {
			if (null != conn) {
				dataSources.recycleConnection(dsKey, conn);
			}
			throw e;
		}
	}

	/**
	 * 处理存在的事务,并处理连接
	 */
	private XTransactionStatus handleExistingTransaction(String dsKey, XTransactionDefinition newDefinition, XTransactionStatus currentStatus)
			throws SQLException {
		// 存在事务定义,则根据事务的传播级别处理
		if (XTransactionDefinition.PROPAGATION_REQUIRED == newDefinition.getBehavior()) {
			return handleExistingTransactionRequired(dsKey, newDefinition, currentStatus);
		} else if (XTransactionDefinition.PROPAGATION_SUPPORTS == newDefinition.getBehavior()) {
			return handleExistingTransactionSupports(dsKey, newDefinition, currentStatus);
		} else if (XTransactionDefinition.PROPAGATION_MANDATORY == newDefinition.getBehavior()) {
			if (!currentStatus.hasTransaction) {
				throw new TangYuanException("No existing transaction found for transaction marked with propagation 'mandatory'");
			}
			return currentStatus;
		} else if (XTransactionDefinition.PROPAGATION_REQUIRES_NEW == newDefinition.getBehavior()) {
			return handleExistingTransactionRequiredNew(dsKey, newDefinition, currentStatus);
		} else if (XTransactionDefinition.PROPAGATION_NOT_SUPPORTED == newDefinition.getBehavior()) {
			return handleExistingTransactionNotSupport(dsKey, newDefinition, currentStatus);
		} else if (XTransactionDefinition.PROPAGATION_NEVER == newDefinition.getBehavior()) {
			if (currentStatus.hasTransaction) {
				throw new TangYuanException("Existing transaction found for transaction marked with propagation 'never'");
			}
			return currentStatus;
		} else if (XTransactionDefinition.PROPAGATION_NESTED == newDefinition.getBehavior()) {
			return handleExistingTransactionNested(dsKey, newDefinition, currentStatus);
		}
		return null;
	}

	/**
	 * 处理事务传播:REQUIRED
	 */
	private XTransactionStatus handleExistingTransactionRequired(String dsKey, XTransactionDefinition newDefinition, XTransactionStatus currentStatus)
			throws SQLException {
		// 单数据源的情况
		if (currentStatus.singleDs && dsKey.equalsIgnoreCase(currentStatus.dsKey)) {
			if (currentStatus.hasTransaction) { // 存在事务
				return currentStatus;
			} else { // 之前不存在事务
				currentStatus.xConnection.beginTransaction(newDefinition);
				currentStatus.hasTransaction = true;
				return currentStatus;
			}
		}
		// 多数据源的情况
		XConnection xConn = currentStatus.getXConnectionFromMap(dsKey);
		if (null == xConn) { // 如果之前不存在事务, 则之后的Connection都是开启事务的
			xConn = createXConnection(dsKey, newDefinition);
			currentStatus.addXConnection(dsKey, xConn);
		}

		if (currentStatus.hasTransaction) {
			return currentStatus;
		} else {
			for (Map.Entry<String, XConnection> entry : currentStatus.connMap.entrySet()) {
				entry.getValue().checkSetTransaction(newDefinition);
			}
			currentStatus.hasTransaction = true;
			return currentStatus;
		}
	}

	/**
	 * 处理事务传播:REQUIRES_NEW
	 */
	private XTransactionStatus handleExistingTransactionRequiredNew(String dsKey, XTransactionDefinition newDefinition,
			XTransactionStatus currentStatus) throws SQLException {
		if (currentStatus.hasTransaction) { // 之前存在事务，挂起之前的事务,创建独立的新事物
			XTransactionStatus newStatus = createTransactionStatus(dsKey, newDefinition);
			newStatus.suspendedResources = currentStatus;
			return newStatus;
		} else { // 之前不存在事务,沿用连接并开启新事物
			currentStatus.newTransaction = true;
			return handleExistingTransactionRequired(dsKey, newDefinition, currentStatus);
		}
	}

	/**
	 * 处理事务传播:SUPPORTS
	 */
	private XTransactionStatus handleExistingTransactionSupports(String dsKey, XTransactionDefinition newDefinition, XTransactionStatus currentStatus)
			throws SQLException {
		// 单数据源的情况
		if (currentStatus.singleDs && dsKey.equalsIgnoreCase(currentStatus.dsKey)) {
			return currentStatus;
		}
		// 多数据源的情况
		XConnection xConn = currentStatus.getXConnectionFromMap(dsKey);
		if (null == xConn) {
			// *这里要和当前事务保持一致: 不能使用newDefinition
			xConn = createXConnection(dsKey, currentStatus.definition);
			currentStatus.addXConnection(dsKey, xConn);
		}
		return currentStatus;
	}

	/**
	 * 处理事务传播:NOT_SUPPORTED
	 */
	private XTransactionStatus handleExistingTransactionNotSupport(String dsKey, XTransactionDefinition newDefinition,
			XTransactionStatus currentStatus) throws SQLException {
		if (currentStatus.hasTransaction) { // 之前存在事务，挂起之前的事务,创建独立的非事物连接
			XTransactionStatus newStatus = createTransactionStatus(dsKey, newDefinition);
			newStatus.suspendedResources = currentStatus;
			return newStatus;
		} else {
			currentStatus.newTransaction = true;
			return currentStatus;
		}
	}

	/**
	 * 处理事务传播:NESTED
	 */
	private XTransactionStatus handleExistingTransactionNested(String dsKey, XTransactionDefinition newDefinition, XTransactionStatus currentStatus)
			throws SQLException {
		if (currentStatus.hasTransaction) {
			currentStatus.hasSavepoint = true;
			// 单数据源的情况
			if (currentStatus.singleDs && dsKey.equalsIgnoreCase(currentStatus.dsKey)) {
				currentStatus.xConnection.setSavepoint();
				return currentStatus;
			}
			// 多数据源的情况
			XConnection xConn = currentStatus.getXConnectionFromMap(dsKey);
			if (null == xConn) {
				xConn = createXConnection(dsKey, newDefinition);
				currentStatus.addXConnection(dsKey, xConn);
			}
			xConn.setSavepoint();
			return currentStatus;
		} else { // 之前不存在事务,沿用连接并开启新事物
			return handleExistingTransactionRequired(dsKey, newDefinition, currentStatus);
		}
	}

	/**
	 * 提交事务 confirm:是否确认提交 false:自动判断是否需要提交(只提交单独的事务), true:立即提交
	 * 
	 * @throws SQLException
	 *             提交过程中发生异常, 上抛
	 */
	public XTransactionStatus commit(XTransactionStatus status, boolean confirm) throws SQLException {
		if (!confirm && !status.newTransaction) {
			// 不确定提交 && 不是一个非独立的事务, 暂时不提交
			log.debug("commit ignore.");
			return status;
		}

		if (!status.initialization) {
			if (null != status.suspendedResources) {
				return status.suspendedResources;
			}
			return null;
		}

		// 确定提交 OR 独立的事务

		if (status.hasTransaction) {
			// 存在事务
			if (null != status.xConnection) {
				Connection conn = status.xConnection.getConnection();
				conn.commit();
				dataSources.recycleConnection(status.dsKey, conn);// 这里不能抛出异常, 内部处理
				status.xConnection.connState = ConnectionState.COMMIT;
			} else if (null != status.connMap) {
				for (Map.Entry<String, XConnection> entry : status.connMap.entrySet()) {
					Connection conn = entry.getValue().getConnection();
					conn.commit();
					dataSources.recycleConnection(entry.getKey(), conn);// 这里不能抛出异常, 内部处理
					entry.getValue().connState = ConnectionState.COMMIT;
				}
			}
		} else {
			// 不存在事务
			if (null != status.xConnection) {
				Connection conn = status.xConnection.getConnection();
				dataSources.recycleConnection(status.dsKey, conn); // 这里不能抛出异常, 内部处理
			} else if (null != status.connMap) {
				for (Map.Entry<String, XConnection> entry : status.connMap.entrySet()) {
					Connection conn = entry.getValue().getConnection();
					dataSources.recycleConnection(entry.getKey(), conn);// 这里不能抛出异常, 内部处理
				}
			}
		}
		log.info("commit success");
		// 回复挂起的服务
		if (null != status.suspendedResources) {
			return status.suspendedResources;
		}
		return null;
	}

	@Override
	public XTransactionStatus rollback(XTransactionStatus status) throws SQLException {
		if (!status.initialization) {
			if (null != status.suspendedResources) {
				return status.suspendedResources;
			}
			return null;
		}

		if (status.hasTransaction) {
			// 存在事务
			if (null != status.xConnection && status.xConnection.connState != ConnectionState.COMMIT) {
				Connection conn = status.xConnection.getConnection();
				try {
					if (null == status.xConnection.getSavepoint()) {
						conn.rollback();
					} else {
						conn.rollback(status.xConnection.getSavepoint());
						conn.commit();
					}
				} catch (SQLException e) {
					log.error("rollback error", e);
				}
				dataSources.recycleConnection(status.dsKey, conn);// 这里不能抛出异常, 内部处理
			} else if (null != status.connMap) {
				for (Map.Entry<String, XConnection> entry : status.connMap.entrySet()) {
					if (entry.getValue().connState != ConnectionState.COMMIT) {
						Connection conn = entry.getValue().getConnection();
						try {
							if (null == entry.getValue().getSavepoint()) {
								conn.rollback();
							} else {
								conn.rollback(entry.getValue().getSavepoint());
								conn.commit();
							}
						} catch (SQLException e) {
							log.error("rollback error", e);
						}
						dataSources.recycleConnection(entry.getKey(), conn);// 这里不能抛出异常, 内部处理
					}
				}
			}
		} else {
			// 不存在事务
			if (null != status.xConnection) {
				Connection conn = status.xConnection.getConnection();
				dataSources.recycleConnection(status.dsKey, conn);// 这里不能抛出异常, 内部处理
			} else if (null != status.connMap) {
				for (Map.Entry<String, XConnection> entry : status.connMap.entrySet()) {
					Connection conn = entry.getValue().getConnection();
					dataSources.recycleConnection(entry.getKey(), conn);// 这里不能抛出异常, 内部处理
				}
			}
		}

		log.info("rollback");
		if (null != status.suspendedResources) {
			return status.suspendedResources;
		}
		return null;
	}

}
