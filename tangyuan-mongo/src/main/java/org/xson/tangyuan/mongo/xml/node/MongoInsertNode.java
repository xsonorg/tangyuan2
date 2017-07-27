package org.xson.tangyuan.mongo.xml.node;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.mongo.executor.MongoServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoInsertNode extends AbstractMongoNode {

	private static Log		log	= LogFactory.getLog(MongoInsertNode.class);

	private CacheCleanVo	cacheClean;

	// public MongoInsertNode(String id, String ns, String serviceKey, Class<?> resultType, String dsKey, TangYuanNode sqlNode,
	// CacheCleanVo cacheClean) {
	//
	// this.id = id;
	// this.ns = ns;
	// this.serviceKey = serviceKey;
	//
	// this.resultType = resultType;
	//
	// this.dsKey = dsKey;
	// this.sqlNode = sqlNode;
	//
	// this.simple = true;
	//
	// this.cacheClean = cacheClean;
	// }

	public MongoInsertNode(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode, CacheCleanVo cacheClean) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.dsKey = dsKey;
		this.sqlNode = sqlNode;

		this.simple = true;

		this.cacheClean = cacheClean;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {

		MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);

		// 2. 清理和重置执行环境
		mongoContext.resetExecEnv();

		long startTime = System.currentTimeMillis();
		sqlNode.execute(context, arg); // 获取sql
		if (log.isInfoEnabled()) {
			log.info(mongoContext.getSql());
		}

		Object result = mongoContext.executeInsert(this);
		context.setResult(result);

		if (log.isInfoEnabled()) {
			log.info("mongo execution time: " + getSlowServiceLog(startTime));
		}

		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		return true;
	}
}
