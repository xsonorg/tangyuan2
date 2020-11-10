package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.cmd.SQLCommandContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class SelectOneNode extends AbstractSqlNode {

	private static Log log       = LogFactory.getLog(SelectOneNode.class);

	private MappingVo  resultMap = null;

	public SelectOneNode(String id, String ns, String serviceKey, Class<?> resultType, MappingVo resultMap, String dsKey, XTransactionDefinition txDef, TangYuanNode sqlNode,
			CacheUseVo cacheUse, String desc, String[] groups) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.resultType = resultType;
		this.resultMap = resultMap;

		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.txDef = txDef;

		this.cacheUse = cacheUse;

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
		Object result = sqlContext.executeSelectOneXCO(this, this.resultMap, null);
		sqlContext.afterExecute(this);
		// 7. 设置结果
		ac.setResult(result);
		return result;
	}

	//	protected Object executeSql(SqlServiceContext sqlContext) throws Throwable {
	//		sqlContext.beforeExecute(this);
	//		return sqlContext.executeSelectOneXCO(this, this.resultMap, null);
	//	}

	//	@Override
	//	public boolean execute(ServiceContext sc, Object arg, Object temp) throws Throwable {
	//		SqlServiceContext sqlContext = (SqlServiceContext) sc.getSqlServiceContext();
	//
	//		String            cacheKey   = null;
	//
	//		// 1. cache使用
	//		if (null != cacheUse) {
	//			cacheKey = this.cacheUse.buildKey(arg);
	//			Object result = cacheUse.getObject(cacheKey);
	//			if (null != result) {
	//				sc.setResult(result);
	//				return true;
	//			}
	//		}
	//
	//		// 1. 清理和重置执行环境
	//		sqlContext.resetExecEnv();
	//
	//		Object  result             = null;
	//		boolean createdTranscation = false;
	//		long    startTime          = 0L;
	//		try {
	//			// 2. 解析SQL
	//			sqlNode.execute(sc, arg, arg); // 获取sql
	//			if (log.isInfoEnabled() || sqlContext.isTraceCommand()) {
	//				sqlContext.parseSqlLog();
	//			}
	//
	//			// 3. 开启事务
	//			startTime = System.currentTimeMillis();
	//			sqlContext.beforeExecute(this); // 开启事务异常, 可认为是事务之前的异常
	//			createdTranscation = true; // *
	//
	//			// 4. 执行SQL
	//			//			if (XCO.class == resultType) {
	//			//				result = sqlContext.executeSelectOneXCO(this, this.resultMap, null);
	//			//			} else {
	//			//				result = sqlContext.executeSelectOneMap(this, this.resultMap, null);
	//			//			}
	//
	//			result = sqlContext.executeSelectOneXCO(this, this.resultMap, null);
	//
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
	//			sc.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), createdTranscation));
	//			throw e;
	//		}
	//
	//		if (null != cacheUse) {
	//			putCache(sc, cacheKey, result);
	//		}
	//
	//		return true;
	//	}

	//	private void putCache(ServiceContext sc, String cacheKey, Object result) {
	//		sc.addPostTask(new Runnable() {
	//			@Override
	//			public void run() {
	//				cacheUse.putObject(cacheKey, result);
	//			}
	//		});
	//	}

	//	@Override
	//	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
	//		SqlServiceContext sqlContext = (SqlServiceContext) serviceContext.getSqlServiceContext();
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
	//		// 1. 清理和重置执行环境
	//		sqlContext.resetExecEnv();
	//
	//		Object  result             = null;
	//		boolean createdTranscation = false;
	//		long    startTime          = 0L;
	//		try {
	//			// 2. 解析SQL
	//			sqlNode.execute(serviceContext, arg); // 获取sql
	//			// if (log.isInfoEnabled()) {
	//			// sqlContext.parseSqlLog();
	//			// }
	//			if (log.isInfoEnabled() || isTraceCommand()) {
	//				sqlContext.parseSqlLog();
	//			}
	//
	//			// 3. 开启事务
	//			startTime = System.currentTimeMillis();
	//			sqlContext.beforeExecute(this); // 开启事务异常, 可认为是事务之前的异常
	//			createdTranscation = true; // *
	//
	//			// 4. 执行SQL
	//			if (XCO.class == resultType) {
	//				result = sqlContext.executeSelectOneXCO(this, this.resultMap, null);
	//			} else {
	//				result = sqlContext.executeSelectOneMap(this, this.resultMap, null);
	//			}
	//
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
	//			serviceContext.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), createdTranscation));
	//			throw e;
	//		}
	//
	//		if (null != cacheUse) {
	//			cacheUse.putObject(arg, result);
	//		}
	//
	//		return true;
	//	}
	//
	//	@Override
	//	public Class<?> getResultType() {
	//		if (null != this.resultType) {
	//			return this.resultType;
	//		}
	//		return this.resultMap.getBeanClass();
	//	}
	//
	//	@Override
	//	@SuppressWarnings("unchecked")
	//	public Object getResult(ServiceContext context) {
	//		Object value = context.getResult();
	//		context.setResult(null);
	//		if (null == value) {
	//			return value;
	//		}
	//		try {
	//			if (null == resultMap) {
	//				if (XCO.class == resultType || Map.class == resultType) {
	//					return value;// 原始的
	//				} else {
	//					return OgnlBean.mapToBean((Map<String, Object>) value, resultType);
	//				}
	//			} else {
	//				Class<?> beanClass = resultMap.getBeanClass();
	//				if (null == beanClass && XCO.class != resultType && Map.class != resultType) {
	//					beanClass = resultType;
	//				}
	//				if (null != beanClass) {
	//					return OgnlBean.mapToBean((Map<String, Object>) value, beanClass);
	//				}
	//				return value;
	//			}
	//		} catch (Throwable e) {
	//			context.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), false));
	//			throw e;
	//		}
	//	}

}
