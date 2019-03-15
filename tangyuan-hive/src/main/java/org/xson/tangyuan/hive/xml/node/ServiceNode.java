package org.xson.tangyuan.hive.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.hive.executor.HiveServiceContext;
import org.xson.tangyuan.hive.transaction.XTransactionDefinition;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class ServiceNode extends AbstractSqlNode {

	private static Log		log	= LogFactory.getLog(ServiceNode.class);

	private CacheUseVo		cacheUse;

	private CacheCleanVo	cacheClean;

	public ServiceNode(String id, String ns, String serviceKey, String dsKey, XTransactionDefinition txDef, TangYuanNode sqlNode, CacheUseVo cacheUse,
			CacheCleanVo cacheClean, Class<?> resultType) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.txDef = txDef;
		this.simple = false;

		this.cacheUse = cacheUse;
		this.cacheClean = cacheClean;

		this.resultType = resultType;
	}

	@Override
	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {

		HiveServiceContext sqlContext = (HiveServiceContext) serviceContext.getServiceContext(TangYuanServiceType.HIVE);
		// 1. cache使用
		if (null != cacheUse) {
			Object result = cacheUse.getObject(arg);
			if (null != result) {
				serviceContext.setResult(result);
				return true;
			}
		}

		long startTime = 0L;
		startTime = System.currentTimeMillis();
		// 1. 这里只是创建事务
		sqlContext.beforeExecute(this, false);
		// 2. 执行服务
		sqlNode.execute(serviceContext, arg);
		// 3. 提交(不确定)
		// sqlContext.commit(false);
		sqlContext.afterExecute(this);
		if (log.isInfoEnabled()) {
			log.info("hql execution time: " + getSlowServiceLog(startTime));
		}

		if (null != cacheUse) {
			cacheUse.putObject(arg, serviceContext.getResult());
		}
		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		return true;
	}
}
