package org.xson.tangyuan.hive.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.hive.executor.HiveServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的SelectSetNode
 */
public class InternalSelectSetNode extends AbstractSqlNode {

	private static Log	log	= LogFactory.getLog(InternalSelectSetNode.class);

	private Integer		fetchSize;
	private String		resultKey;
	private CacheUseVo	cacheUse;

	public InternalSelectSetNode(String dsKey, String resultKey, TangYuanNode sqlNode, Class<?> resultType, Integer fetchSize, CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
		this.fetchSize = fetchSize;
		this.resultType = resultType;
		this.simple = false;
		this.cacheUse = cacheUse;
	}

	@Override
	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {

		HiveServiceContext context = (HiveServiceContext) serviceContext.getServiceContext(TangYuanServiceType.HIVE);

		// 1. cache使用
		if (null != cacheUse) {
			Object result = cacheUse.getObject(arg);
			if (null != result) {
				serviceContext.setResult(result);
				return true;
			}
		}

		context.resetExecEnv();

		sqlNode.execute(serviceContext, arg); // 获取sql
		if (log.isInfoEnabled() || isTraceCommand()) {
			context.parseSqlLog();
		}

		long startTime = System.currentTimeMillis();
		context.beforeExecute(this, true);// 只是打开连接

		Object result = null;
		if (XCO.class == resultType) {
			result = context.executeSelectSetListXCO(this, null, fetchSize);
		} else {
			result = context.executeSelectSetListMap(this, null, fetchSize);
		}

		if (null != this.resultKey) {
			Ognl.setValue(arg, this.resultKey, result);
		}
		context.afterExecute(this);

		if (log.isInfoEnabled()) {
			log.info("hql execution time: " + getSlowServiceLog(startTime));
		}

		if (null != cacheUse) {
			cacheUse.putObject(arg, result);
		}

		return true;
	}

	public int getFetchSize() {
		return fetchSize;
	}

}
