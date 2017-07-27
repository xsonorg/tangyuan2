package org.xson.tangyuan.mongo.xml.node;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.mongo.executor.MongoServiceContext;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的SelectSetNode
 */
public class InternalMongoSelectSetNode extends AbstractMongoNode {

	private static Log	log	= LogFactory.getLog(InternalMongoSelectSetNode.class);

	private Integer		fetchSize;
	private String		resultKey;
	private CacheUseVo	cacheUse;

	public InternalMongoSelectSetNode(String dsKey, String resultKey, TangYuanNode sqlNode, Class<?> resultType, Integer fetchSize,
			CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
		this.fetchSize = fetchSize;
		this.resultType = resultType;
		this.simple = false;
		this.cacheUse = cacheUse;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {

		MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);

		// 1. cache使用
		if (null != cacheUse) {
			Object result = cacheUse.getObject(arg);
			if (null != result) {
				context.setResult(result);
				return true;
			}
		}

		mongoContext.resetExecEnv();

		long startTime = System.currentTimeMillis();
		sqlNode.execute(context, arg); // 获取sql
		if (log.isInfoEnabled()) {
			log.info(mongoContext.getSql());
		}

		Object result = null;
		if (XCO.class == resultType) {
			result = mongoContext.executeSelectSetListXCO(this, null, fetchSize);
		} else {
			result = mongoContext.executeSelectSetListMap(this, null, fetchSize);
		}
		if (null != this.resultKey) {
			Ognl.setValue(arg, this.resultKey, result);
		}

		if (log.isInfoEnabled()) {
			log.info("mongo execution time: " + getSlowServiceLog(startTime));
		}

		if (null != cacheUse) {
			cacheUse.putObject(arg, result);
		}

		return true;
	}

}
