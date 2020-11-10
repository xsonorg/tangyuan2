package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoSelectVarNode extends AbstractMongoNode {

	private static Log log       = LogFactory.getLog(MongoSelectVarNode.class);

	private MappingVo  resultMap = null;// TODO

	public MongoSelectVarNode(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode, CacheUseVo cacheUse, String desc, String[] groups) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.dsKey = dsKey;
		this.sqlNode = sqlNode;

		this.cacheUse = cacheUse;

		this.simple = true;

		this.desc = desc;
		this.groups = groups;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, MongoServiceContext context, Object temp) throws Throwable {
		Object result = context.executeSelectVar(this, this.resultMap, temp);
		// 7. 设置结果
		ac.setResult(result);
		return result;
	}

	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) throws Throwable {
	//
	//		MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);
	//
	//		// 1. cache使用
	//		if (null != cacheUse) {
	//			Object result = cacheUse.getObject(arg);
	//			if (null != result) {
	//				context.setResult(result);
	//				return true;
	//			}
	//		}
	//
	//		// 2. 清理和重置执行环境
	//		mongoContext.resetExecEnv();
	//		long   startTime = System.currentTimeMillis();
	//		Object result    = null;
	//
	//		sqlNode.execute(context, arg); // 获取sql
	//		if (log.isInfoEnabled()) {
	//			log.info(mongoContext.getSql());
	//		}
	//
	//		result = mongoContext.executeSelectVar(this, arg);
	//		context.setResult(result);
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("mongo execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		if (null != cacheUse) {
	//			cacheUse.putObject(arg, result);
	//		}
	//
	//		return true;
	//	}

}
