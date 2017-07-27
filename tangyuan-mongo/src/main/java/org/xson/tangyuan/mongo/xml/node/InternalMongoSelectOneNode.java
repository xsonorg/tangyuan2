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
 * 内部的SelectOneNode
 */
public class InternalMongoSelectOneNode extends AbstractMongoNode {

	private static Log	log	= LogFactory.getLog(InternalMongoSelectOneNode.class);

	private String		resultKey;
	private CacheUseVo	cacheUse;

	public InternalMongoSelectOneNode(String dsKey, String resultKey, TangYuanNode sqlNode, Class<?> resultType, CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
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
			result = mongoContext.executeSelectOneXCO(this, null, null);
		} else {
			result = mongoContext.executeSelectOneMap(this, null, null);
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
