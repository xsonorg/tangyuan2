package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的SelectVarNode
 */
public class InternalMongoSelectVarNode extends AbstractMongoNode {

	private static Log log       = LogFactory.getLog(InternalMongoSelectVarNode.class);

	private String     resultKey = null;
	private MappingVo  resultMap = null;// TODO

	public InternalMongoSelectVarNode(String dsKey, String resultKey, TangYuanNode sqlNode, CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
		this.simple = false;
		this.cacheUse = cacheUse;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, MongoServiceContext context, Object temp) throws Throwable {
		Object result = context.executeSelectVar(this, this.resultMap, temp);
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
	//		// 1. cache使用
	//		if (null != cacheUse) {
	//			Object result = cacheUse.getObject(arg);
	//			if (null != result) {
	//				context.setResult(result);
	//				return true;
	//			}
	//		}
	//
	//		mongoContext.resetExecEnv();
	//
	//		long startTime = System.currentTimeMillis();
	//		sqlNode.execute(context, arg); // 获取sql
	//		if (log.isInfoEnabled()) {
	//			log.info(mongoContext.getSql());
	//		}
	//
	//		Object result = mongoContext.executeSelectVar(this, arg);
	//
	//		if (null != this.resultKey) {
	//			Ognl.setValue(arg, this.resultKey, result);
	//		}
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
