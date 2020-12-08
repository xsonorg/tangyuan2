package org.xson.tangyuan.hive.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.hive.service.cmd.HIVECommandContext;
import org.xson.tangyuan.hive.service.context.HiveServiceContext;
import org.xson.tangyuan.hive.transaction.XTransactionDefinition;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class ServiceNode extends AbstractHiveNode {

	private static Log log = LogFactory.getLog(ServiceNode.class);

	//	private CacheUseVo   cacheUse;
	//	private CacheCleanVo cacheClean;

	public ServiceNode(String id, String ns, String serviceKey, String dsKey, XTransactionDefinition txDef, TangYuanNode sqlNode, CacheUseVo cacheUse, CacheCleanVo cacheClean,
			Class<?> resultType, String desc, String[] groups) {
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

		this.desc = desc;
		this.groups = groups;
	}

	public boolean execute(ActuatorContext ac, Object arg, Object acArg) throws Throwable {
		HiveServiceContext sqlContext = (HiveServiceContext) ac.getServiceContext(this.serviceType);

		long               startTime  = System.currentTimeMillis();
		String             cacheKey   = null;
		Object             result     = null;

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
					log.info("hive execution time: " + getSlowServiceLog(startTime) + " use cache");
				}
				return true;
			}
		}

		// 0. 克隆参数
		// if (null == temp) {
		// temp = cloneArg(arg);
		// }

		// sqlContext.setResultType(arg);// 设置同进同出的类型

		if (ac instanceof HIVECommandContext) {
			sqlNode.execute(ac, arg, acArg);
			sqlContext.afterExecute(this);
		} else {
			// 1. 这里只是创建事务
			sqlContext.beforeExecute(this, false);
			// 2. 执行服务
			sqlNode.execute(ac, arg, acArg);
			sqlContext.afterExecute(this);
		}

		if (log.isInfoEnabled()) {
			log.info("hive execution time: " + getSlowServiceLog(startTime));
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
	//	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
	//
	//		HiveServiceContext sqlContext = (HiveServiceContext) serviceContext.getServiceContext(TangYuanServiceType.HIVE);
	//		// 1. cache使用
	//		if (null != cacheUse) {
	//			Object result = cacheUse.getObject(arg);
	//			if (null != result) {
	//				serviceContext.setResult(result);
	//				return true;
	//			}
	//		}
	//
	//		long startTime = 0L;
	//		startTime = System.currentTimeMillis();
	//		// 1. 这里只是创建事务
	//		sqlContext.beforeExecute(this, false);
	//		// 2. 执行服务
	//		sqlNode.execute(serviceContext, arg);
	//		// 3. 提交(不确定)
	//		// sqlContext.commit(false);
	//		sqlContext.afterExecute(this);
	//		if (log.isInfoEnabled()) {
	//			log.info("hql execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		if (null != cacheUse) {
	//			cacheUse.putObject(arg, serviceContext.getResult());
	//		}
	//		if (null != cacheClean) {
	//			cacheClean.removeObject(arg);
	//		}
	//
	//		return true;
	//	}
}
