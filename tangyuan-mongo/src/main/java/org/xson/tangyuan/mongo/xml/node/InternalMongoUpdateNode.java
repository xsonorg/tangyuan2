package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的UpdateNode
 */
public class InternalMongoUpdateNode extends AbstractMongoNode {

	private static Log log = LogFactory.getLog(InternalMongoUpdateNode.class);

	private String     resultKey;

	public InternalMongoUpdateNode(String dsKey, String rowCount, TangYuanNode sqlNode, CacheCleanVo cacheClean) {
		this.dsKey = dsKey;
		this.resultKey = rowCount;
		this.sqlNode = sqlNode;
		this.simple = false;
		this.cacheClean = cacheClean;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, MongoServiceContext context, Object temp) throws Throwable {
		Object result = context.executeUpdate(this, temp);
		// 7. 设置结果
		if (null != this.resultKey) {
			Ognl.setValue(temp, this.resultKey, result);
		}
		return result;
	}

	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) throws Throwable {
	//
	//		MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);
	//
	//		mongoContext.resetExecEnv();
	//
	//		long startTime = System.currentTimeMillis();
	//		sqlNode.execute(context, arg); // 获取sql
	//		if (log.isInfoEnabled()) {
	//			log.info(mongoContext.getSql());
	//		}
	//
	//		int count = mongoContext.executeUpdate(this, arg);
	//		if (null != this.resultKey) {
	//			Ognl.setValue(arg, this.resultKey, count);
	//		}
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("mongo execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		if (null != cacheClean) {
	//			cacheClean.removeObject(arg);
	//		}
	//
	//		return true;
	//	}

}
