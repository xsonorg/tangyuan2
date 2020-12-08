package org.xson.tangyuan.hive.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.hive.service.context.HiveServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的UpdateNode
 */
public class InternalHqlNode extends AbstractHiveNode {

	private static Log log = LogFactory.getLog(InternalHqlNode.class);

	// private String resultKey;
	//	private CacheCleanVo cacheClean;

	private boolean    asyncHql;

	public InternalHqlNode(String dsKey, String resultKey, TangYuanNode sqlNode, CacheCleanVo cacheClean, boolean asyncHql) {
		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.simple = false;
		this.cacheClean = cacheClean;
		this.asyncHql = asyncHql;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, HiveServiceContext sqlContext, Object acArg) throws Throwable {
		sqlContext.beforeExecute(this, true);
		//		Object result = sqlContext.executeSelectVar(this);
		Object result = sqlContext.executeHql(this, this.asyncHql);
		sqlContext.afterExecute(this);
		//		if (null != this.resultKey) {
		//			Ognl.setValue(acArg, this.resultKey, result);
		//		}
		return result;
	}

	//	@Override
	//	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
	//
	//		HiveServiceContext context = (HiveServiceContext) serviceContext.getServiceContext(TangYuanServiceType.HIVE);
	//
	//		context.resetExecEnv();
	//
	//		sqlNode.execute(serviceContext, arg); // 获取sql
	//		if (log.isInfoEnabled() || isTraceCommand()) {
	//			context.parseSqlLog();
	//		}
	//
	//		long startTime = System.currentTimeMillis();
	//		context.beforeExecute(this, true);// 只是打开连接
	//
	//		context.executeHql(this, asyncHql);
	//		// if (null != this.resultKey) {
	//		// Ognl.setValue(arg, this.resultKey, 0);
	//		// }
	//		context.afterExecute(this);
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("hql execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		if (null != cacheClean) {
	//			cacheClean.removeObject(arg);
	//		}
	//
	//		return true;
	//	}

}
