package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的SelectSetNode
 */
public class InternalSelectSetNode extends AbstractSqlNode {

	private static Log log       = LogFactory.getLog(InternalSelectSetNode.class);

	private Integer    fetchSize = null;
	private String     resultKey = null;
	private MappingVo  resultMap = null;

	public InternalSelectSetNode(String dsKey, String resultKey, TangYuanNode sqlNode, Class<?> resultType, MappingVo resultMap, Integer fetchSize, CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
		this.fetchSize = fetchSize;

		this.resultType = resultType;
		this.resultMap = resultMap;

		this.cacheUse = cacheUse;

		this.simple = false;
	}

	public int getFetchSize() {
		return fetchSize;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, SqlServiceContext sqlContext, Object temp) throws Throwable {
		// 只是打开连接
		sqlContext.beforeExecute(this, true);
		Object result = sqlContext.executeSelectSetListXCO(this, this.resultMap, this.fetchSize);
		sqlContext.afterExecute(this);
		if (null != this.resultKey) {
			Ognl.setValue(temp, this.resultKey, result);
		}
		return result;
	}

	//	protected Object executeSql(SqlServiceContext sqlContext, Object temp) throws Throwable {
	//		// 只是打开连接
	//		sqlContext.beforeExecute(this, true);
	//		Object result = sqlContext.executeSelectSetListXCO(this, null, fetchSize);
	//		sqlContext.afterExecute(this);
	//		if (null != this.resultKey) {
	//			Ognl.setValue(temp, this.resultKey, result);
	//		}
	//		return result;
	//	}

	//	@Override
	//	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
	//		return executeInternal(ac, arg, temp);
	//	}

	//	public boolean executeInternal(ActuatorContext ac, Object arg, Object temp) throws Throwable {
	//		SqlServiceContext sqlContext = (SqlServiceContext) ac.getServiceContext(this.serviceType);
	//		String            cacheKey = null;
	//
	//		//		// 1. cache使用
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
	//		Object result = null;
	//		// if (XCO.class == context.getResultType(resultType)) {
	//		//		if (XCO.class == resultType) {
	//		//			result = context.executeSelectSetListXCO(this, null, fetchSize);
	//		//		} else {
	//		//			result = context.executeSelectSetListMap(this, null, fetchSize);
	//		//		}
	//		result = context.executeSelectSetListXCO(this, null, fetchSize);
	//
	//		if (null != this.resultKey) {
	//			//			Ognl.setValue(arg, this.resultKey, result);
	//			Ognl.setValue(temp, this.resultKey, result);
	//		}
	//		context.afterExecute(this);
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("sql execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		//		if (null != cacheUse) {
	//		//			cacheUse.putObject(arg, result);
	//		//		}
	//
	//		if (null != cacheUse) {
	//			putCache(sc, cacheKey, result);
	//		}
	//
	//		return true;
	//	}

	//	@Override
	//	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
	//
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
	//		Object result = null;
	//		// if (XCO.class == context.getResultType(resultType)) {
	//		if (XCO.class == resultType) {
	//			result = context.executeSelectSetListXCO(this, null, fetchSize);
	//		} else {
	//			result = context.executeSelectSetListMap(this, null, fetchSize);
	//		}
	//
	//		if (null != this.resultKey) {
	//			Ognl.setValue(arg, this.resultKey, result);
	//		}
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
