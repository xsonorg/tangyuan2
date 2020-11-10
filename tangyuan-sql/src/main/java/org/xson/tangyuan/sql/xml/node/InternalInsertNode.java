package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的InsertNode
 */
public class InternalInsertNode extends AbstractSqlNode {

	private static Log log = LogFactory.getLog(InternalInsertNode.class);

	// 返回影响行数的key
	private String     resultKey;
	// 返回的自增key, 有可能是多个
	private String     incrementKey;

	public InternalInsertNode(String dsKey, String rowCount, String incrementKey, TangYuanNode sqlNode, CacheCleanVo cacheClean) {
		this.dsKey = dsKey;
		this.resultKey = rowCount;
		this.incrementKey = incrementKey;
		this.sqlNode = sqlNode;
		this.simple = false;
		this.cacheClean = cacheClean;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, SqlServiceContext sqlContext, Object temp) throws Throwable {
		// 只是打开连接
		sqlContext.beforeExecute(this, true);

		if (null == this.resultKey && null == this.incrementKey) {
			sqlContext.executeInsert(this);
			sqlContext.afterExecute(this);
		} else if (null != this.resultKey && null == this.incrementKey) {
			int rowCount = sqlContext.executeInsert(this);
			sqlContext.afterExecute(this);
			Ognl.setValue(temp, this.resultKey, rowCount);
		} else {
			InsertReturn insertReturn = sqlContext.executeInsertReturn(this);
			sqlContext.afterExecute(this);
			if (null != this.resultKey) {
				Ognl.setValue(temp, this.resultKey, insertReturn.getRowCount());
			}
			Ognl.setValue(temp, this.incrementKey, insertReturn.getColumns());
		}
		return null;
	}

	//	protected Object executeSql(SqlServiceContext sqlContext, Object temp) throws Throwable {
	//		// 只是打开连接
	//		sqlContext.beforeExecute(this, true);
	//
	//		if (null == this.resultKey && null == this.incrementKey) {
	//			sqlContext.executeInsert(this);
	//		} else if (null != this.resultKey && null == this.incrementKey) {
	//			int rowCount = sqlContext.executeInsert(this);
	//			Ognl.setValue(temp, this.resultKey, rowCount);
	//		} else {
	//			InsertReturn insertReturn = sqlContext.executeInsertReturn(this);
	//			Ognl.setValue(temp, this.resultKey, insertReturn.getRowCount());
	//			Ognl.setValue(temp, this.incrementKey, insertReturn.getColumns());
	//		}
	//
	//		sqlContext.afterExecute(this);
	//		return null;
	//	}
	//
	//	@Override
	//	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
	//		return executeInternal(ac, arg, temp);
	//	}

	//	@Override
	//	public boolean execute(ServiceContext sc, Object arg, Object temp) throws Throwable {
	//		SqlServiceContext context  = (SqlServiceContext) sc.getSqlServiceContext();
	//		String            cacheKey = null;
	//		// 1. cache使用
	//		if (null != cacheClean) {
	//			cacheKey = this.cacheClean.buildKey(arg);
	//		}
	//
	//		context.resetExecEnv();
	//
	//		//		sqlNode.execute(serviceContext, arg); // 获取sql
	//		sqlNode.execute(sc, arg, temp); // 获取sql
	//		// if (log.isInfoEnabled()) {
	//		// context.parseSqlLog();
	//		// }
	//		if (log.isInfoEnabled() || context.isTraceCommand()) {
	//			context.parseSqlLog();
	//		}
	//
	//		long startTime = System.currentTimeMillis();
	//		context.beforeExecute(this, true);// 只是打开连接
	//
	//		if (null == this.incrementKey) {
	//			int count = context.executeInsert(this);
	//			if (null != this.resultKey) {
	//				//				Ognl.setValue(arg, this.resultKey, count);
	//				Ognl.setValue(temp, this.resultKey, count);
	//			}
	//		} else {
	//			InsertReturn insertReturn = context.executeInsertReturn(this);
	//			if (null != this.resultKey) {
	//				//				Ognl.setValue(arg, this.resultKey, insertReturn.getRowCount());
	//				Ognl.setValue(temp, this.resultKey, insertReturn.getRowCount());
	//			}
	//			Object columns = insertReturn.getColumns();
	//			//			Ognl.setValue(arg, this.incrementKey, columns);
	//			Ognl.setValue(temp, this.incrementKey, columns);
	//		}
	//
	//		context.afterExecute(this);
	//		if (log.isInfoEnabled()) {
	//			log.info("sql execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		//		if (null != cacheClean) {
	//		//			cacheClean.removeObject(arg);
	//		//		}
	//		if (null != cacheClean) {
	//			removeCache(sc, cacheKey);
	//		}
	//
	//		return true;
	//	}

	//	@Override
	//	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
	//		SqlServiceContext context = (SqlServiceContext) serviceContext.getSqlServiceContext();
	//
	//		context.resetExecEnv();
	//
	//		sqlNode.execute(serviceContext, arg); // 获取sql
	//		// if (log.isInfoEnabled()) {
	//		// context.parseSqlLog();
	//		// }
	//		if (log.isInfoEnabled() || isTraceCommand()) {
	//			context.parseSqlLog();
	//		}
	//
	//		long startTime = System.currentTimeMillis();
	//		context.beforeExecute(this, true);// 只是打开连接
	//
	//		if (null == this.incrementKey) {
	//			int count = context.executeInsert(this);
	//			if (null != this.resultKey) {
	//				Ognl.setValue(arg, this.resultKey, count);
	//			}
	//		} else {
	//			InsertReturn insertReturn = context.executeInsertReturn(this);
	//			if (null != this.resultKey) {
	//				Ognl.setValue(arg, this.resultKey, insertReturn.getRowCount());
	//			}
	//			Object columns = insertReturn.getColumns();
	//			Ognl.setValue(arg, this.incrementKey, columns);
	//		}
	//
	//		context.afterExecute(this);
	//		if (log.isInfoEnabled()) {
	//			log.info("sql execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		if (null != cacheClean) {
	//			cacheClean.removeObject(arg);
	//		}
	//
	//		return true;
	//	}

}
