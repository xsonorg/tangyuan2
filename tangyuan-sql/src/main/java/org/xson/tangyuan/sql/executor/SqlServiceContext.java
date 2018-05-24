package org.xson.tangyuan.sql.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.IServiceContext;
import org.xson.tangyuan.executor.IServiceExceptionInfo;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.sql.SqlComponent;
import org.xson.tangyuan.sql.transaction.MultipleTransactionManager;
import org.xson.tangyuan.sql.transaction.TransactionException;
import org.xson.tangyuan.sql.transaction.XTransactionManager;
import org.xson.tangyuan.sql.transaction.XTransactionStatus;
import org.xson.tangyuan.sql.xml.node.AbstractSqlNode;
import org.xson.tangyuan.type.InsertReturn;

public class SqlServiceContext implements IServiceContext {

	private static Log			log					= LogFactory.getLog(SqlServiceContext.class);

	private StringBuilder		sqlBuilder			= null;

	/** 经过解析后的sql语句,日志专用 */
	private String				realSql				= null;

	/** SQL参数值列表 */
	private List<Object>		argList				= null;

	/** 真正的数据源dsKey */
	private String				realDsKey			= null;

	private SqlActuator			sqlActuator			= null;

	private XTransactionManager	transactionManager	= null;

	private XTransactionStatus	transaction			= null;

	private SqlLog				sqlLog				= null;

	// 有入参决定的, 可以保证默认情况下同进同出, 用户组合SQL
	// protected Class<?> resultType = null;

	protected SqlServiceContext() {
		this.sqlActuator = new SqlActuator(SqlComponent.getInstance().getTypeHandlerRegistry());
		this.transactionManager = new MultipleTransactionManager(SqlComponent.getInstance().getDataSourceManager());
		if (log.isInfoEnabled()) {
			sqlLog = new SqlLog(SqlComponent.getInstance().getTypeHandlerRegistry());
		}
	}

	/** 重设执行环境 */
	public void resetExecEnv() {
		this.realDsKey = null; // 重设dskey
		this.argList = null; // 重新设置变量
		this.sqlBuilder = new StringBuilder(); // 重新设置sqlBuilder
		this.realSql = null;
	}

	public void addSql(String sql) {
		sqlBuilder.append(sql);
	}

	private String getSql() {
		return sqlBuilder.toString();
	}

	public void setDsKey(String realDsKey) {
		if (null == realDsKey || null == this.realDsKey) {
			this.realDsKey = realDsKey;
		} else if (!this.realDsKey.equals(realDsKey)) {
			throw new TransactionException("暂不支持多dsKey:" + realDsKey);// 只有在分库分表的情况才会出现
		}
	}

	public void parseSqlLog() {
		this.realSql = sqlLog.getSqlLog(getSql(), argList);
	}

	public void addStaticVarList(List<Variable> varList) {
		addStaticVarList(varList, null);
	}

	// TODO 以后考虑 arg...
	public void addStaticVarList(List<Variable> varList, Object arg) {
		if (null == varList) {
			this.argList = null;
			return;
		}

		if (null == this.argList) {
			this.argList = new ArrayList<Object>();
		}

		// for (VariableVo var : varList) {
		// argList.add(var.getValue(arg));
		// }

		for (Variable var : varList) {
			Object value = var.getValue(arg);
			if (null == value) {
				// log.error("Field does not exist: " + var.getOriginal());
				throw new TangYuanException("Field does not exist: " + var.getOriginal());
			}
			argList.add(value);
		}
	}

	public List<Map<String, Object>> executeSelectSetListMap(AbstractSqlNode sqlNode, MappingVo resultMap, Integer fetchSize) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		List<Map<String, Object>> result = sqlActuator.selectAllMap(connection, getSql(), argList, resultMap, fetchSize);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public List<XCO> executeSelectSetListXCO(AbstractSqlNode sqlNode, MappingVo resultMap, Integer fetchSize) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		List<XCO> result = sqlActuator.selectAllXCO(connection, getSql(), argList, resultMap, fetchSize);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public Map<String, Object> executeSelectOneMap(AbstractSqlNode sqlNode, MappingVo resultMap, Integer fetchSize) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		Map<String, Object> result = sqlActuator.selectOneMap(connection, getSql(), argList, resultMap, fetchSize);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public XCO executeSelectOneXCO(AbstractSqlNode sqlNode, MappingVo resultMap, Integer fetchSize) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		XCO result = sqlActuator.selectOneXCO(connection, getSql(), argList, resultMap, fetchSize);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public Object executeSelectVar(AbstractSqlNode sqlNode) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		Object result = sqlActuator.selectVar(connection, getSql(), argList);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public int executeDelete(AbstractSqlNode sqlNode) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		int result = sqlActuator.delete(connection, getSql(), argList);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public int executeUpdate(AbstractSqlNode sqlNode) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		int result = sqlActuator.update(connection, getSql(), argList);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public int executeInsert(AbstractSqlNode sqlNode) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		int result = sqlActuator.insert(connection, getSql(), argList);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public InsertReturn executeInsertReturn(AbstractSqlNode sqlNode) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		InsertReturn result = sqlActuator.insertReturn(connection, getSql(), argList);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public void beforeExecute(AbstractSqlNode sqlNode) throws SQLException {
		// TODO 要根据上下文获取: 需要判断两个不同的数据源, 再一个查询中
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		transaction = transactionManager.getTransaction(dsKey, sqlNode.getTxDef(), transaction);
	}

	public void beforeExecute(AbstractSqlNode sqlNode, boolean openConnection) throws SQLException {
		if (openConnection) {
			// TODO 要根据上下文获取: 需要判断两个不同的数据源, 再一个查询中
			String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
			// log.debug("子服务运行之前不需要打开事务,只需要打开连接");
			transaction = transactionManager.getTransaction(dsKey, null, transaction);
		} else {
			// log.debug("主服务运行之前只需要打开事务,不需要打开连接");
			transaction = transactionManager.getTransaction(null, sqlNode.getTxDef(), transaction);
		}
	}

	public void afterExecute(AbstractSqlNode sqlNode) throws SQLException {
		// TODO 1. count time
	}

	public void commit(boolean confirm) throws Throwable {
		if (null != transaction) {
			transaction = transactionManager.commit(transaction, confirm);
		}
	}

	public void rollbackCurrentTransaction() throws SQLException {
		if (null != transaction) {
			transaction = transactionManager.rollback(transaction);
		}
	}

	@Override
	public void rollback() {
		while (null != transaction) {
			try {
				rollbackCurrentTransaction();
			} catch (Throwable e) {
				log.error("rollback error", e);
			}
		}
	}

	@Override
	public boolean onException(IServiceExceptionInfo exceptionInfo) throws ServiceException {
		//		if (null == exceptionInfo) {
		//			throw new ServiceException("exceptionInfo is null");
		//		}
		if (null == exceptionInfo) {	//  fix:info为空, sql上下文将不能准确的处理异常, 认为是系统未考虑到的特殊异常
			log.error("exceptionInfo is null.");
			return false;
		}

		SqlServiceExceptionInfo info = (SqlServiceExceptionInfo) exceptionInfo;
		if (info.isNewTranscation()) {
			if (info.isCreatedTranscation()) {
				try {
					rollbackCurrentTransaction();
				} catch (Throwable e) {
					log.error("rollback error", e);
				}
			}
			return true;
		}
		return false;
	}

}
