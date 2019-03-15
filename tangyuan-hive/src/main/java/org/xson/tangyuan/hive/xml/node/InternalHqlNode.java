package org.xson.tangyuan.hive.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.hive.executor.HiveServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的UpdateNode
 */
public class InternalHqlNode extends AbstractSqlNode {

	private static Log		log	= LogFactory.getLog(InternalHqlNode.class);

	/** 返回结果的key */
	// private String resultKey;
	private CacheCleanVo	cacheClean;
	private boolean			asyncHql;

	public InternalHqlNode(String dsKey, String resultKey, TangYuanNode sqlNode, CacheCleanVo cacheClean, boolean asyncHql) {
		this.dsKey = dsKey;
		// this.resultKey = rowCount;
		this.sqlNode = sqlNode;
		this.simple = false;
		this.cacheClean = cacheClean;

		this.asyncHql = asyncHql;
	}

	@Override
	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {

		HiveServiceContext context = (HiveServiceContext) serviceContext.getServiceContext(TangYuanServiceType.HIVE);

		context.resetExecEnv();

		sqlNode.execute(serviceContext, arg); // 获取sql
		if (log.isInfoEnabled() || isTraceCommand()) {
			context.parseSqlLog();
		}

		long startTime = System.currentTimeMillis();
		context.beforeExecute(this, true);// 只是打开连接

		context.executeHql(this, asyncHql);
		// if (null != this.resultKey) {
		// Ognl.setValue(arg, this.resultKey, 0);
		// }
		context.afterExecute(this);

		if (log.isInfoEnabled()) {
			log.info("hql execution time: " + getSlowServiceLog(startTime));
		}

		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		return true;
	}

}
