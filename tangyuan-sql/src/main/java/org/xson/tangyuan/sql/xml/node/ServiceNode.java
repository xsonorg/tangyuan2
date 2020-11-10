package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.cmd.SQLCommandContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class ServiceNode extends AbstractSqlNode {

	private static Log log = LogFactory.getLog(ServiceNode.class);

	public ServiceNode(String id, String ns, String serviceKey, String dsKey, XTransactionDefinition txDef, TangYuanNode sqlNode, CacheUseVo cacheUse, CacheCleanVo cacheClean,
			Class<?> resultType, String desc, String[] groups) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.txDef = txDef;

		this.cacheUse = cacheUse;
		this.cacheClean = cacheClean;

		this.resultType = resultType;

		this.desc = desc;
		this.groups = groups;

		this.simple = false;
	}

	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
		SqlServiceContext sqlContext = (SqlServiceContext) ac.getServiceContext(this.serviceType);

		long              startTime  = System.currentTimeMillis();
		String            cacheKey   = null;
		Object            result     = null;

		// 1. cache使用
		if (null != this.cacheUse && null == cacheKey) {
			cacheKey = this.cacheUse.buildKey(arg);
		}
		if (null != this.cacheClean && null == cacheKey) {
			cacheKey = this.cacheClean.buildKey(arg);
		}
		if (null != this.cacheUse) {
			result = this.cacheUse.getObject(cacheKey);
			if (null != result) {
				ac.setResult(result);
				if (log.isInfoEnabled()) {
					// TODO use cache
					log.info("sql execution time: " + getSlowServiceLog(startTime));
				}
				return true;
			}
		}

		// 0. 克隆参数
		//		if (null == temp) {
		//			temp = cloneArg(arg);
		//		}

		// sqlContext.setResultType(arg);// 设置同进同出的类型

		if (ac instanceof SQLCommandContext) {
			sqlNode.execute(ac, arg, temp);
			sqlContext.afterExecute(this);
		} else {
			// 1. 这里只是创建事务
			sqlContext.beforeExecute(this, false);
			// 2. 执行服务
			sqlNode.execute(ac, arg, temp);
			sqlContext.afterExecute(this);
		}

		if (log.isInfoEnabled()) {
			log.info("sql execution time: " + getSlowServiceLog(startTime));
		}

		// 8. 放置缓存
		if (null != cacheUse) {
			result = ac.getResult();
			putCache(ac, cacheKey, result);
		}
		// 8. 清理缓存
		if (null != cacheClean) {
			removeCache(ac, cacheKey);
		}

		return true;
	}

	//	@Override
	//	protected Object cloneArg(Object arg) {
	//		if (arg instanceof XCO) {
	//			return ((XCO) arg).clone();
	//		}
	//		return arg;
	//	}

	//	@Override
	//	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
	//		boolean flag = true;
	//		try {
	//			flag = execute0(ac, arg, temp);
	//		} catch (Throwable e) {
	//			((SqlServiceContext) ac.getServiceContext(this.serviceType)).onException(e);
	//		}
	//		return flag;
	//	}

	//	@Override
	//	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
	//
	//		SqlServiceContext sqlContext = (SqlServiceContext) ac.getServiceContext(this.serviceType);
	//
	//		long              startTime  = System.currentTimeMillis();
	//		String            cacheKey   = null;
	//		Object            result     = null;
	//
	//		// 1. cache使用
	//		if (null != this.cacheUse && null == cacheKey) {
	//			cacheKey = this.cacheUse.buildKey(arg);
	//		}
	//		if (null != this.cacheClean && null == cacheKey) {
	//			cacheKey = this.cacheClean.buildKey(arg);
	//		}
	//		if (null != this.cacheUse) {
	//			result = this.cacheUse.getObject(cacheKey);
	//			if (null != result) {
	//				ac.setResult(result);
	//				if (log.isInfoEnabled()) {
	//					log.info("sql execution time: " + getSlowServiceLog(startTime));
	//				}
	//				return true;
	//			}
	//		}
	//
	//		// 0. 克隆参数
	//		arg = cloneArg(arg);
	//		// sqlContext.setResultType(arg);// 设置同进同出的类型
	//		log.info("start transaction: " + this.txDef.getId());
	//
	//		// 1. 这里只是创建事务
	//		sqlContext.beforeExecute(this, false);
	//		// 2. 执行服务
	//		//		sqlNode.execute(ac, arg);
	//		sqlNode.execute(ac, arg, temp);
	//		// 3. 提交(不确定)
	//		sqlContext.commit(false);
	//		sqlContext.afterExecute(this);
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("sql execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		// 8. 放置缓存
	//		if (null != cacheUse) {
	//			putCache(ac, cacheKey, result);
	//		}
	//		// 8. 清理缓存
	//		if (null != cacheClean) {
	//			removeCache(ac, cacheKey);
	//		}
	//
	//		return true;
	//	}

	//	@Override
	//	public boolean execute(ServiceContext sc, Object arg, Object temp) throws Throwable {
	//		SqlServiceContext sqlContext = (SqlServiceContext) sc.getSqlServiceContext();
	//
	//		String            cacheKey   = null;
	//		if (null != cacheUse || null != cacheClean) {
	//			cacheKey = this.cacheUse.buildKey(arg);
	//		}
	//
	//		// 1. cache使用
	//		if (null != cacheUse) {
	//			Object result = cacheUse.getObject(cacheKey);
	//			if (null != result) {
	//				sc.setResult(result);
	//				return true;
	//			}
	//		}
	//
	//		// 设置同进同出的类型
	//		// sqlContext.setResultType(arg);
	//		temp = cloneArg(arg);
	//
	//		boolean createdTranscation = false;
	//		long    startTime          = 0L;
	//		try {
	//			log.info("start transaction: " + this.txDef.getId());
	//			startTime = System.currentTimeMillis();
	//			// 1. 这里只是创建事务
	//			sqlContext.beforeExecute(this, false);
	//			createdTranscation = true;
	//			// 2. 执行服务
	//			//			sqlNode.execute(sc, arg);
	//			sqlNode.execute(sc, arg, temp);
	//			// 3. 提交(不确定)
	//			sqlContext.commit(false);
	//			createdTranscation = false;
	//			sqlContext.afterExecute(this);
	//			if (log.isInfoEnabled()) {
	//				log.info("sql execution time: " + getSlowServiceLog(startTime));
	//			}
	//		} catch (Throwable e) {
	//			sc.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), createdTranscation));
	//			throw e;
	//		}
	//
	//		//		if (null != cacheUse) {
	//		//			cacheUse.putObject(arg, serviceContext.getResult());
	//		//		}
	//		//		if (null != cacheClean) {
	//		//			cacheClean.removeObject(arg);
	//		//		}
	//
	//		if (null != cacheUse) {
	//			putCache(sc, cacheKey, sc.getResult());
	//		}
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
	//

}
