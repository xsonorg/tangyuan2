package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的SelectVarNode
 */
public class InternalSelectVarNode extends AbstractSqlNode {

	private static Log log = LogFactory.getLog(InternalSelectVarNode.class);

	// 返回结果的key
	private String     resultKey;

	public InternalSelectVarNode(String dsKey, String resultKey, TangYuanNode sqlNode, CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
		this.simple = false;
		this.cacheUse = cacheUse;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, SqlServiceContext sqlContext, Object temp) throws Throwable {
		sqlContext.beforeExecute(this, true);
		Object result = sqlContext.executeSelectVar(this);
		sqlContext.afterExecute(this);
		if (null != this.resultKey) {
			Ognl.setValue(temp, this.resultKey, result);
		}
		return result;
	}

	//	protected Object executeSql(SqlServiceContext sqlContext, Object temp) throws Throwable {
	//		// 只是打开连接
	//		sqlContext.beforeExecute(this, true);
	//		Object result = sqlContext.executeSelectVar(this);
	//		sqlContext.afterExecute(this);
	//		if (null != this.resultKey) {
	//			Ognl.setValue(temp, this.resultKey, result);
	//		}
	//		return result;
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
	//		//		if (null != cacheUse) {
	//		//			Object result = cacheUse.getObject(arg);
	//		//			if (null != result) {
	//		//				serviceContext.setResult(result);
	//		//				return true;
	//		//			}
	//		//		}
	//
	//		if (null != cacheUse) {
	//			cacheKey = this.cacheUse.buildKey(arg);
	//			Object result = cacheUse.getObject(cacheKey);
	//			if (null != result) {
	//				sc.setResult(result);
	//				return true;
	//			}
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
	//		Object result = context.executeSelectVar(this);
	//		if (null != this.resultKey) {
	//			//			Ognl.setValue(arg, this.resultKey, result);
	//			Ognl.setValue(temp, this.resultKey, result);
	//		}
	//
	//		context.afterExecute(this);
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("sql execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		if (null != cacheUse) {
	//			putCache(sc, cacheKey, result);
	//		}
	//
	//		return true;
	//	}

	//	@Override
	//	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
	//		SqlServiceContext context = (SqlServiceContext) serviceContext.getSqlServiceContext();
	//
	//		// 1. cache使用
	//		if (null != cacheUse) {
	//			Object result = cacheUse.getObject(arg);
	//			if (null != result) {
	//				serviceContext.setResult(result);
	//				return true;
	//			}
	//		}
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
	//		Object result = context.executeSelectVar(this);
	//		if (null != this.resultKey) {
	//			Ognl.setValue(arg, this.resultKey, result);
	//		}
	//
	//		context.afterExecute(this);
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("sql execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		if (null != cacheUse) {
	//			cacheUse.putObject(arg, result);
	//		}
	//
	//		return true;
	//	}

}
