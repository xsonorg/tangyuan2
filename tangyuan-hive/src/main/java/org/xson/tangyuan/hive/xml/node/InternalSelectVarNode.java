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
 * 内部的SelectVarNode
 */
public class InternalSelectVarNode extends AbstractHiveNode {

	private static Log log       = LogFactory.getLog(InternalSelectVarNode.class);

	private String     resultKey = null;
	private MappingVo  resultMap = null;

	//	public InternalSelectVarNode(String dsKey, String resultKey, TangYuanNode sqlNode, CacheUseVo cacheUse) {
	//		this.dsKey = dsKey;
	//		this.resultKey = resultKey;
	//		this.sqlNode = sqlNode;
	//		this.simple = false;
	//		this.cacheUse = cacheUse;
	//	}
	public InternalSelectVarNode(String dsKey, String resultKey, TangYuanNode sqlNode, MappingVo resultMap, CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
		this.simple = false;
		this.resultMap = resultMap;
		this.cacheUse = cacheUse;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, HiveServiceContext sqlContext, Object acArg) throws Throwable {
		sqlContext.beforeExecute(this, true);
		Object result = sqlContext.executeSelectVar(this, this.resultMap);
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
	//		Object result = context.executeSelectVar(this);
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
