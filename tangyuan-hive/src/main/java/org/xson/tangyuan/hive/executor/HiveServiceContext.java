package org.xson.tangyuan.hive.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.IServiceContext;
import org.xson.tangyuan.executor.IServiceExceptionInfo;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.hive.HiveComponent;
import org.xson.tangyuan.hive.transaction.MultipleTransactionManager;
import org.xson.tangyuan.hive.transaction.TransactionException;
import org.xson.tangyuan.hive.transaction.XTransactionDefinition;
import org.xson.tangyuan.hive.transaction.XTransactionManager;
import org.xson.tangyuan.hive.transaction.XTransactionStatus;
import org.xson.tangyuan.hive.util.HiveJDBCUtil;
import org.xson.tangyuan.hive.xml.node.AbstractSqlNode;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.runtime.RuntimeContext;
import org.xson.tangyuan.runtime.trace.TrackingManager;

public class HiveServiceContext implements IServiceContext {

	private static Log						log					= LogFactory.getLog(HiveServiceContext.class);

	private StringBuilder					sqlBuilder			= null;

	/** 经过解析后的sql语句,日志专用 */
	private String							realSql				= null;

	/** SQL参数值列表 */
	private List<Object>					argList				= null;

	/** 真正的数据源dsKey */
	private String							realDsKey			= null;

	private SqlActuator						sqlActuator			= null;

	private XTransactionManager				transactionManager	= null;
	private XTransactionStatus				transaction			= null;

	private SqlLog							sqlLog				= null;

	private static XTransactionDefinition	nullDefinition		= new XTransactionDefinition(null);

	// 有入参决定的, 可以保证默认情况下同进同出, 用户组合SQL
	// protected Class<?> resultType = null;

	protected HiveServiceContext() {
		this.sqlActuator = new SqlActuator(HiveComponent.getInstance().getTypeHandlerRegistry());
		this.transactionManager = new MultipleTransactionManager(HiveComponent.getInstance().getDataSourceManager());
		if (log.isInfoEnabled()) {
			sqlLog = new SqlLog(HiveComponent.getInstance().getTypeHandlerRegistry());
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
		String execSql = HiveJDBCUtil.sqlTrim(getSql());
		List<Map<String, Object>> result = sqlActuator.selectAllMap(connection, execSql, argList, resultMap, fetchSize);
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
		String execSql = HiveJDBCUtil.sqlTrim(getSql());
		List<XCO> result = sqlActuator.selectAllXCO(connection, execSql, argList, resultMap, fetchSize);
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
		String execSql = HiveJDBCUtil.sqlTrim(getSql());
		Map<String, Object> result = sqlActuator.selectOneMap(connection, execSql, argList, resultMap, fetchSize);
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
		String execSql = HiveJDBCUtil.sqlTrim(getSql());
		XCO result = sqlActuator.selectOneXCO(connection, execSql, argList, resultMap, fetchSize);
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
		String execSql = HiveJDBCUtil.sqlTrim(getSql());
		Object result = sqlActuator.selectVar(connection, execSql, argList);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		appendTracking();
		return result;
	}

	public Object executeHql(AbstractSqlNode sqlNode, boolean async) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		String execSql = HiveJDBCUtil.sqlTrim(getSql());
		Object result = sqlActuator.executeHql(connection, execSql, argList, async);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		appendTracking();
		return result;
	}

	public void beforeExecute(AbstractSqlNode sqlNode) throws SQLException {
		// TODO 要根据上下文获取: 需要判断两个不同的数据源, 再一个查询中
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		// transaction = transactionManager.getTransaction(dsKey, sqlNode.getTxDef(), transaction);
		transaction = transactionManager.getTransaction(dsKey, nullDefinition, transaction);
	}

	public void beforeExecute(AbstractSqlNode sqlNode, boolean openConnection) throws SQLException {
		if (openConnection) {
			// TODO 要根据上下文获取: 需要判断两个不同的数据源, 再一个查询中
			String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
			// log.debug("子服务运行之前不需要打开事务,只需要打开连接");
			transaction = transactionManager.getTransaction(dsKey, null, transaction);
		} else {
			// log.debug("主服务运行之前只需要打开事务,不需要打开连接");
			// transaction = transactionManager.getTransaction(null, sqlNode.getTxDef(), transaction);
			transaction = transactionManager.getTransaction(null, nullDefinition, transaction);
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

	@Override
	public void rollback() {
		try {
			if (null != transaction) {
				transaction = transactionManager.rollback(transaction);
			}
		} catch (Throwable e) {
			log.error("rollback error", e);
		}
	}

	@Override
	public boolean onException(IServiceExceptionInfo exceptionInfo) throws ServiceException {
		return false;
	}

	private void appendTracking() {
		RuntimeContext.appendTracking(TrackingManager.SERVICE_TYPE_HIVE, this.realSql);
	}
}
