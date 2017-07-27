package org.xson.tangyuan.mongo.xml.node;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.mongo.executor.MongoServiceContext;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的SelectVarNode
 */
public class InternalMongoSelectVarNode extends AbstractMongoNode {

	private static Log	log	= LogFactory.getLog(InternalMongoSelectVarNode.class);

	// 返回结果的key
	private String		resultKey;
	private CacheUseVo	cacheUse;

	public InternalMongoSelectVarNode(String dsKey, String resultKey, TangYuanNode sqlNode, CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
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

		Object result = mongoContext.executeSelectVar(this);

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
