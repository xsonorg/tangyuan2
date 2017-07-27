package org.xson.tangyuan.mongo.xml.node;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.mongo.executor.MongoServiceContext;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的InsertNode
 */
public class InternalMongoInsertNode extends AbstractMongoNode {

	private static Log		log	= LogFactory.getLog(InternalMongoInsertNode.class);

	// 这里是返回_id
	private String			resultKey;

	private CacheCleanVo	cacheClean;

	public InternalMongoInsertNode(String dsKey, String resultKey, TangYuanNode sqlNode, CacheCleanVo cacheClean) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
		this.simple = false;
		this.cacheClean = cacheClean;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {

		MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);

		mongoContext.resetExecEnv();

		long startTime = System.currentTimeMillis();
		sqlNode.execute(context, arg); // 获取sql

		if (log.isInfoEnabled()) {
			log.info(mongoContext.getSql());
		}

		Object result = mongoContext.executeInsert(this);
		if (null != this.resultKey) {
			Ognl.setValue(arg, this.resultKey, result);
		}

		if (log.isInfoEnabled()) {
			log.info("mongo execution time: " + getSlowServiceLog(startTime));
		}

		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		return true;
	}

}
