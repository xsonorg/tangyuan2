package org.xson.tangyuan.hive.service.context;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.hive.HiveComponent;
import org.xson.tangyuan.hive.service.SqlActuator;
import org.xson.tangyuan.hive.transaction.TransactionException;
import org.xson.tangyuan.hive.transaction.XTransactionDefinition;
import org.xson.tangyuan.hive.transaction.XTransactionManager;
import org.xson.tangyuan.hive.transaction.XTransactionStatus;
import org.xson.tangyuan.hive.util.HiveJDBCUtil;
import org.xson.tangyuan.hive.util.SqlLog;
import org.xson.tangyuan.hive.xml.node.AbstractHiveNode;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.manager.TangYuanManager;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.service.context.ServiceContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class HiveServiceContext implements ServiceContext {

	private static Log                    log                = LogFactory.getLog(HiveServiceContext.class);

	private StringBuilder                 sqlBuilder         = null;

	/** 经过解析后的sql语句,日志专用 */
	private String                        realSql            = null;

	/** SQL参数值列表 */
	private List<Object>                  argList            = null;

	/** 真正的数据源dsKey */
	private String                        realDsKey          = null;

	//	private SqlActuator                   sqlActuator        = null;
	//	private XTransactionManager           transactionManager = null;
	//	private SqlLog                        sqlLog             = null;
	// 有入参决定的, 可以保证默认情况下同进同出, 用户组合SQL
	// protected Class<?> resultType = null;

	private XTransactionStatus            transaction        = null;

	private static XTransactionDefinition nullDefinition     = new XTransactionDefinition(null);

	/**
	 * 发送异常时的计数，用作于command模式
	 */
	private int                           exceptionCount     = 0;

	private XTransactionManager           transactionManager = null;
	private SqlActuator                   sqlActuator        = null;
	private SqlLog                        sqlLog             = null;
	private TangYuanManager               tangYuanManager    = null;

	protected HiveServiceContext() {
		//		this.sqlActuator = new SqlActuator(HiveComponent.getInstance().getTypeHandlerRegistry());
		//		this.transactionManager = new MultipleTransactionManager(HiveComponent.getInstance().getDataSourceManager());
		//		if (log.isInfoEnabled()) {
		//			sqlLog = new SqlLog(HiveComponent.getInstance().getTypeHandlerRegistry());
		//		}

		this.tangYuanManager = TangYuanManager.getInstance();
		this.transactionManager = HiveComponent.getInstance().getTransactionManager();
		this.sqlActuator = HiveComponent.getInstance().getSqlActuator();
		this.sqlLog = HiveComponent.getInstance().getSqlLog();
	}

	/** 重设执行环境 */
	public void resetExecEnv() {
		this.realDsKey = null; 						// 重设dskey
		this.argList = null;					 	// 重新设置变量
		this.sqlBuilder = new StringBuilder(); 		// 重新设置sqlBuilder
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

	public List<Map<String, Object>> executeSelectSetListMap(AbstractHiveNode sqlNode, MappingVo resultMap, Integer fetchSize) throws SQLException {
		String     dsKey      = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		String                    execSql = HiveJDBCUtil.sqlTrim(getSql());
		List<Map<String, Object>> result  = sqlActuator.selectAllMap(connection, execSql, argList, resultMap, fetchSize);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		// RuntimeContext.appendTracking(TrackingManager.SERVICE_TYPE_SQL, this.realSql);
		appendTracking();
		return result;
	}

	public List<XCO> executeSelectSetListXCO(AbstractHiveNode sqlNode, MappingVo resultMap, Integer fetchSize) throws SQLException {
		String     dsKey      = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		String    execSql = HiveJDBCUtil.sqlTrim(getSql());
		List<XCO> result  = sqlActuator.selectAllXCO(connection, execSql, argList, resultMap, fetchSize);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		appendTracking();
		return result;
	}

	public Map<String, Object> executeSelectOneMap(AbstractHiveNode sqlNode, MappingVo resultMap, Integer fetchSize) throws SQLException {
		String     dsKey      = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		String              execSql = HiveJDBCUtil.sqlTrim(getSql());
		Map<String, Object> result  = sqlActuator.selectOneMap(connection, execSql, argList, resultMap, fetchSize);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		appendTracking();
		return result;
	}

	public XCO executeSelectOneXCO(AbstractHiveNode sqlNode, MappingVo resultMap, Integer fetchSize) throws SQLException {
		String     dsKey      = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		String execSql = HiveJDBCUtil.sqlTrim(getSql());
		XCO    result  = sqlActuator.selectOneXCO(connection, execSql, argList, resultMap, fetchSize);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		appendTracking();
		return result;
	}

	public Object executeSelectVar(AbstractHiveNode sqlNode, MappingVo resultMap) throws SQLException {
		String     dsKey      = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		String execSql = HiveJDBCUtil.sqlTrim(getSql());
		Object result  = sqlActuator.selectVar(connection, execSql, argList, resultMap);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		appendTracking();
		return result;
	}

	public Object executeHql(AbstractHiveNode sqlNode, boolean async) throws SQLException {
		String     dsKey      = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		String execSql = HiveJDBCUtil.sqlTrim(getSql());
		Object result  = sqlActuator.executeHql(connection, execSql, argList, async);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		appendTracking();
		return result;
	}

	public void beforeExecute(AbstractHiveNode sqlNode) throws SQLException {
		// TODO 要根据上下文获取: 需要判断两个不同的数据源, 再一个查询中
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		// transaction = transactionManager.getTransaction(dsKey, sqlNode.getTxDef(), transaction);
		transaction = transactionManager.getTransaction(dsKey, nullDefinition, transaction);
	}

	public void beforeExecute(AbstractHiveNode sqlNode, boolean openConnection) throws SQLException {
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

	/**
	 * 开启事物，供SQL命令调用，仅仅开启事物，不打开连接
	 */
	public void beforeExecuteForCommandBegin(XTransactionDefinition txDef) throws SQLException {
		transaction = transactionManager.getTransaction(null, txDef, transaction);
	}

	public void afterExecute(AbstractHiveNode sqlNode) throws SQLException {
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

	/////////////////////////////////////////////////////////////////////////////

	//	@Override
	//	public boolean onException(IServiceExceptionInfo exceptionInfo) throws ServiceException {
	//		return false;
	//	}
	//	private void appendTracking() {
	//		RuntimeContext.appendTracking(TrackingManager.SERVICE_TYPE_HIVE, this.realSql);
	//	}

}
