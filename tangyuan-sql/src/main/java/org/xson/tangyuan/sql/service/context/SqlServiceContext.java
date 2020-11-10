package org.xson.tangyuan.sql.service.context;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.manager.TangYuanManager;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.service.context.ServiceContext;
import org.xson.tangyuan.sql.SqlComponent;
import org.xson.tangyuan.sql.service.SqlActuator;
import org.xson.tangyuan.sql.transaction.TransactionException;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.sql.transaction.XTransactionManager;
import org.xson.tangyuan.sql.transaction.XTransactionStatus;
import org.xson.tangyuan.sql.util.SqlLog;
import org.xson.tangyuan.sql.xml.node.AbstractSqlNode;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class SqlServiceContext implements ServiceContext {

	private static Log			log					= LogFactory.getLog(SqlServiceContext.class);
	private StringBuilder		sqlBuilder			= null;
	/** 经过解析后的sql语句,日志专用 */
	private String				realSql				= null;
	/** SQL参数值列表 */
	private List<Object>		argList				= null;
	/** 真正的数据源dsKey */
	private String				realDsKey			= null;
	private XTransactionStatus	transaction			= null;
	/**
	 * 发送异常时的计数，用作于command模式
	 */
	private int					exceptionCount		= 0;

	private XTransactionManager	transactionManager	= null;
	private SqlActuator			sqlActuator			= null;
	private SqlLog				sqlLog				= null;

	private TangYuanManager		tangYuanManager		= null;

	protected SqlServiceContext() {
		this.tangYuanManager = TangYuanManager.getInstance();
		this.transactionManager = SqlComponent.getInstance().getTransactionManager();
		this.sqlActuator = SqlComponent.getInstance().getSqlActuator();
		this.sqlLog = SqlComponent.getInstance().getSqlLog();
	}

	/** 重设执行环境 */
	public void resetExecEnv() {
		// 重设dskey
		this.realDsKey = null;
		// 重新设置变量
		this.argList = null;
		// 重新设置sqlBuilder
		this.sqlBuilder = new StringBuilder();
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

	public void addStaticVarList(List<Variable> varList, Object arg) {
		// 以后考虑 arg...
		if (null == varList) {
			this.argList = null;
			return;
		}

		if (null == this.argList) {
			this.argList = new ArrayList<Object>();
		}

		for (Variable var : varList) {
			Object value = var.getValue(arg);
			if (null == value) {
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
		// RuntimeContext.appendTracking(TrackingManager.SERVICE_TYPE_SQL, this.realSql);
		appendTracking();
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
		appendTracking();
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
		appendTracking();
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
		appendTracking();
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
		appendTracking();
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
		appendTracking();
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
		appendTracking();
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
		appendTracking();
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
		appendTracking();
		return result;
	}

	public void beforeExecute(AbstractSqlNode sqlNode) throws SQLException {
		// 要根据上下文获取: 需要判断两个不同的数据源, 再一个查询中
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		transaction = transactionManager.getTransaction(dsKey, sqlNode.getTxDef(), transaction);
	}

	public void beforeExecute(AbstractSqlNode sqlNode, boolean openConnection) throws SQLException {
		if (openConnection) {
			// 要根据上下文获取: 需要判断两个不同的数据源, 再一个查询中
			String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
			// log.debug("子服务运行之前不需要打开事务,只需要打开连接");
			transaction = transactionManager.getTransaction(dsKey, null, transaction);
		} else {
			// log.debug("主服务运行之前只需要打开事务,不需要打开连接");
			transaction = transactionManager.getTransaction(null, sqlNode.getTxDef(), transaction);
		}
	}

	/**
	 * 开启事物，供SQL命令调用，仅仅开启事物，不打开连接
	 */
	public void beforeExecuteForCommandBegin(XTransactionDefinition txDef) throws SQLException {
		transaction = transactionManager.getTransaction(null, txDef, transaction);
	}

	public void afterExecute(AbstractSqlNode sqlNode) throws SQLException {
		// 1. count time
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

	/**
	 * 提交或者回滚
	 * 
	 * @param ex
	 */
	public boolean commitAndRollBack(Throwable ex) throws Throwable {
		if (null != ex || this.exceptionCount > 0) {
			rollback();
			return false;
		}
		while (null != transaction) {
			try {
				commit(true);
			} catch (Throwable e) {
				log.error("commit error", e);
				rollback();
				throw e;// 新的异常
			}
		}
		return true;
	}

	@Override
	public void onException(Object info) {
		this.exceptionCount++;
	}

	private void appendTracking() {
		if (isTraceCommand()) {
			this.tangYuanManager.appendTrackingCommand(this.realSql);
		}
	}

	public boolean isTraceCommand() {
		if (null != this.tangYuanManager) {
			this.tangYuanManager.isTrackingCommand(TangYuanServiceType.SQL.getVal());
		}
		return false;
	}

}
