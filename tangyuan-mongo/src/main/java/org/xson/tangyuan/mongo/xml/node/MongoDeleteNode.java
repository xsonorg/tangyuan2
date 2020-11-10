package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoDeleteNode extends AbstractMongoNode {

	private static Log log = LogFactory.getLog(MongoDeleteNode.class);

	public MongoDeleteNode(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode, CacheCleanVo cacheClean, String desc, String[] groups) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.simple = true;
		this.cacheClean = cacheClean;

		this.desc = desc;
		this.groups = groups;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, MongoServiceContext context, Object temp) throws Throwable {
		Object result = context.executeDelete(this, temp);
		// 7. 设置结果
		ac.setResult(result);
		return result;
	}

	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) throws Throwable {
	//		MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);
	//
	//		// 2. 清理和重置执行环境
	//		mongoContext.resetExecEnv();
	//
	//		long startTime = System.currentTimeMillis();
	//		sqlNode.execute(context, arg); // 获取sql
	//		if (log.isInfoEnabled()) {
	//			log.info(mongoContext.getSql());
	//		}
	//
	//		int result = mongoContext.executeDelete(this, arg);
	//		context.setResult(result);
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
