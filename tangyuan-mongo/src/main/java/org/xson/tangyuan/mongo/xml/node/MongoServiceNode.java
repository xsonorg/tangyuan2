package org.xson.tangyuan.mongo.xml.node;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoServiceNode extends AbstractMongoNode {

	private static Log		log	= LogFactory.getLog(MongoServiceNode.class);

	private CacheUseVo		cacheUse;

	private CacheCleanVo	cacheClean;

	public MongoServiceNode(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode, CacheUseVo cacheUse, CacheCleanVo cacheClean,
			Class<?> resultType) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.simple = false;

		this.cacheUse = cacheUse;
		this.cacheClean = cacheClean;
		this.resultType = resultType;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {

		// MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);

		// 1. cache使用
		if (null != cacheUse) {
			Object result = cacheUse.getObject(arg);
			if (null != result) {
				context.setResult(result);
				return true;
			}
		}

		// try {
		// } catch (Throwable e) {
		// throw new ServiceException(e);
		// }

		long startTime = System.currentTimeMillis();

		sqlNode.execute(context, arg);

		if (log.isInfoEnabled()) {
			log.info("mongo execution time: " + getSlowServiceLog(startTime));
		}

		if (null != cacheUse) {
			cacheUse.putObject(arg, context.getResult());
		}
		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		return true;
	}
}
