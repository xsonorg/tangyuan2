package org.xson.tangyuan.sql.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.cmd.SQLCommandContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class InsertNode extends AbstractSqlNode {

	private static Log log = LogFactory.getLog(InsertNode.class);

	// 插入后的返回类型, 这里使用resultType, long, array, list, 空认为返回行数

	// 返回影响行数的key
	private String     resultKey;
	// 返回的自增key, 有可能是多个
	private String     incrementKey;

	public InsertNode(String id, String ns, String serviceKey, String resultKey, String incrementKey, String dsKey, XTransactionDefinition txDef, TangYuanNode sqlNode,
			CacheCleanVo cacheClean, String desc, String[] groups) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.resultKey = resultKey;
		this.incrementKey = incrementKey;

		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.txDef = txDef;

		this.cacheClean = cacheClean;

		this.desc = desc;
		this.groups = groups;

		this.simple = true;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, SqlServiceContext sqlContext, Object temp) throws Throwable {
		if (ac instanceof SQLCommandContext) {
			sqlContext.beforeExecute(this, true);
		} else {
			sqlContext.beforeExecute(this);
		}

		if (null == this.resultKey && null == this.incrementKey) {
			Object result = sqlContext.executeInsert(this);
			sqlContext.afterExecute(this);
			ac.setResult(result);
			return null;
		}
		XCO result = new XCO();
		if (null != this.resultKey && null == this.incrementKey) {
			int rowCount = sqlContext.executeInsert(this);
			sqlContext.afterExecute(this);
			result.setIntegerValue(this.resultKey, rowCount);
		} else {
			InsertReturn insertReturn = sqlContext.executeInsertReturn(this);
			sqlContext.afterExecute(this);
			if (null != this.resultKey) {
				result.setIntegerValue(this.resultKey, insertReturn.getRowCount());
			}
			result.setObjectValue(this.incrementKey, insertReturn.getColumns());
		}
		// 7. 设置结果
		ac.setResult(result);
		return null;
	}

	//////////////////////////////////////////////////////////////////////////////////

	//	public InsertNode(String id, String ns, String serviceKey, Class<?> resultType, String dsKey, XTransactionDefinition txDef, TangYuanNode sqlNode, CacheCleanVo cacheClean,
	//			String desc, String[] groups) {
	//
	//		this.id = id;
	//		this.ns = ns;
	//		this.serviceKey = serviceKey;
	//
	//		this.resultType = resultType;
	//
	//		this.dsKey = dsKey;
	//		this.sqlNode = sqlNode;
	//		this.txDef = txDef;
	//
	//		this.cacheClean = cacheClean;
	//
	//		this.desc = desc;
	//		this.groups = groups;
	//
	//		this.simple = true;
	//	}

	//	protected Object executeSql(SqlServiceContext sqlContext, Object temp) throws Throwable {
	//		sqlContext.beforeExecute(this);
	//		if (null == this.resultKey && null == this.incrementKey) {
	//			return sqlContext.executeInsert(this);
	//		}
	//		XCO result = new XCO();
	//		if (null != this.resultKey && null == this.incrementKey) {
	//			int rowCount = sqlContext.executeInsert(this);
	//			result.setIntegerValue(this.resultKey, rowCount);
	//		} else {
	//			InsertReturn insertReturn = sqlContext.executeInsertReturn(this);
	//			result.setIntegerValue(this.resultKey, insertReturn.getRowCount());
	//			result.setObjectValue(this.incrementKey, insertReturn.getColumns());
	//		}
	//		return result;
	//	}

	//	@Override
	//	public boolean execute(ServiceContext sc, Object arg, Object temp) throws Throwable {
	//		SqlServiceContext sqlContext = (SqlServiceContext) sc.getSqlServiceContext();
	//
	//		String            cacheKey   = null;
	//
	//		// 1. cache使用
	//		if (null != cacheClean) {
	//			cacheKey = this.cacheClean.buildKey(arg);
	//		}
	//
	//		// 1. 清理和重置执行环境
	//		sqlContext.resetExecEnv();
	//
	//		boolean createdTranscation = false;
	//		long    startTime          = 0L;
	//		try {
	//			// 2. 解析SQL
	//			sqlNode.execute(sc, arg, arg); // 获取sql
	//			// if (log.isInfoEnabled()) {
	//			// sqlContext.parseSqlLog();
	//			// }
	//
	//			if (log.isInfoEnabled() || sqlContext.isTraceCommand()) {
	//				sqlContext.parseSqlLog();
	//			}
	//
	//			// 3. 开启事务
	//			startTime = System.currentTimeMillis();
	//			sqlContext.beforeExecute(this); // 开启事务异常, 可认为是事务之前的异常
	//			createdTranscation = true; // *
	//			// 4. 执行SQL
	//			Object result = null;
	//			if (null == resultType) {
	//				result = sqlContext.executeInsert(this);
	//			} else {
	//				InsertReturn insertReturn = sqlContext.executeInsertReturn(this);
	//				result = insertReturn.getColumns();
	//			}
	//			// 5. 提交:这里做不确定的提交
	//			sqlContext.commit(false);
	//			if (txDef.isNewTranscation()) {
	//				createdTranscation = false; // *
	//			}
	//			sqlContext.afterExecute(this);
	//
	//			// 6. 设置结果
	//			sc.setResult(result);
	//			if (log.isInfoEnabled()) {
	//				log.info("sql execution time: " + getSlowServiceLog(startTime));
	//			}
	//		} catch (Throwable e) {
	//			// 考虑此处设置当前事务的特征, 上层统一处理(无论如何)
	//			sc.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), createdTranscation));
	//			throw e;
	//		}
	//
	//		if (null != cacheClean) {
	//			removeCache(sc, cacheKey);
	//		}
	//
	//		return true;
	//	}

	//	private void removeCache(ServiceContext sc, String cacheKey) {
	//		sc.addPostTask(new Runnable() {
	//			@Override
	//			public void run() {
	//				cacheClean.removeObject(cacheKey);
	//			}
	//		});
	//	}

	//	@Override
	//	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
	//		SqlServiceContext sqlContext = (SqlServiceContext) serviceContext.getSqlServiceContext();
	//
	//		// 1. 清理和重置执行环境
	//		sqlContext.resetExecEnv();
	//
	//		boolean createdTranscation = false;
	//		long    startTime          = 0L;
	//		try {
	//			// 2. 解析SQL
	//			sqlNode.execute(serviceContext, arg); // 获取sql
	//			// if (log.isInfoEnabled()) {
	//			// sqlContext.parseSqlLog();
	//			// }
	//
	//			if (log.isInfoEnabled() || isTraceCommand()) {
	//				sqlContext.parseSqlLog();
	//			}
	//
	//			// 3. 开启事务
	//			startTime = System.currentTimeMillis();
	//			sqlContext.beforeExecute(this); // 开启事务异常, 可认为是事务之前的异常
	//			createdTranscation = true; // *
	//			// 4. 执行SQL
	//			Object result = null;
	//			if (null == resultType) {
	//				result = sqlContext.executeInsert(this);
	//			} else {
	//				InsertReturn insertReturn = sqlContext.executeInsertReturn(this);
	//				result = insertReturn.getColumns();
	//			}
	//			// 5. 提交:这里做不确定的提交
	//			sqlContext.commit(false);
	//			if (txDef.isNewTranscation()) {
	//				createdTranscation = false; // *
	//			}
	//			sqlContext.afterExecute(this);
	//
	//			// 6. 设置结果
	//			serviceContext.setResult(result);
	//			if (log.isInfoEnabled()) {
	//				log.info("sql execution time: " + getSlowServiceLog(startTime));
	//			}
	//		} catch (Throwable e) {
	//			// 考虑此处设置当前事务的特征, 上层统一处理(无论如何)
	//			serviceContext.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), createdTranscation));
	//			throw e;
	//		}
	//
	//		if (null != cacheClean) {
	//			cacheClean.removeObject(arg);
	//		}
	//
	//		return true;
	//	}
}
