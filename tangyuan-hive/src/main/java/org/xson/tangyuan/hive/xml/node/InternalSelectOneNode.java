package org.xson.tangyuan.hive.xml.node;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.hive.service.context.HiveServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的SelectOneNode
 */
public class InternalSelectOneNode extends AbstractHiveNode {

	private static Log log       = LogFactory.getLog(InternalSelectOneNode.class);

	private String     resultKey = null;
	private MappingVo  resultMap = null;

	//	public InternalSelectOneNode(String dsKey, String resultKey, TangYuanNode sqlNode, Class<?> resultType, CacheUseVo cacheUse) {
	//		this.dsKey = dsKey;
	//		this.resultKey = resultKey;
	//		this.sqlNode = sqlNode;
	//		this.resultType = resultType;
	//		this.simple = false;
	//		this.cacheUse = cacheUse;
	//	}

	public InternalSelectOneNode(String dsKey, String resultKey, TangYuanNode sqlNode, Class<?> resultType, MappingVo resultMap, CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
		this.resultType = resultType;
		this.resultMap = resultMap;

		this.cacheUse = cacheUse;

		this.simple = false;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, HiveServiceContext sqlContext, Object acArg) throws Throwable {
		// 只是打开连接
		sqlContext.beforeExecute(this, true);
		Object result = sqlContext.executeSelectOneXCO(this, this.resultMap, null);
		sqlContext.afterExecute(this);
		if (null != this.resultKey) {
			Ognl.setValue(acArg, this.resultKey, result);
		}
		return result;
	}

	//	@Override
	//	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
	//
	//		HiveServiceContext context = (HiveServiceContext) serviceContext.getServiceContext(TangYuanServiceType.HIVE);
	//
	//		// 1. cache使用
	//		if (null != cacheUse) {
	//			Object result = cacheUse.getObject(arg);
	//			if (null != result) {
	//				serviceContext.setResult(result);
	//				return true;
	//			}
	//		}
	//
	//		context.resetExecEnv();
	//
	//		sqlNode.execute(serviceContext, arg); // 获取sql
	//		if (log.isInfoEnabled() || isTraceCommand()) {
	//			context.parseSqlLog();
	//		}
	//
	//		long startTime = System.currentTimeMillis();
	//		context.beforeExecute(this, true);// 只是打开连接
	//
	//		Object result = null;
	//		if (XCO.class == resultType) {
	//			result = context.executeSelectOneXCO(this, null, null);
	//		} else {
	//			result = context.executeSelectOneMap(this, null, null);
	//		}
	//		if (null != this.resultKey) {
	//			Ognl.setValue(arg, this.resultKey, result);
	//		}
	//
	//		context.afterExecute(this);
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("hql execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		if (null != cacheUse) {
	//			cacheUse.putObject(arg, result);
	//		}
	//
	//		return true;
	//	}
}
