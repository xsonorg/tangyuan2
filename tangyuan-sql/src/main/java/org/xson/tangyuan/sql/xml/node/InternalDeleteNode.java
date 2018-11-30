package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.sql.executor.SqlServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的DeleteNode
 */
public class InternalDeleteNode extends AbstractSqlNode {

	private static Log		log	= LogFactory.getLog(InternalDeleteNode.class);

	// 返回结果的key
	private String			resultKey;

	private CacheCleanVo	cacheClean;

	public InternalDeleteNode(String dsKey, String rowCount, TangYuanNode sqlNode, CacheCleanVo cacheClean) {
		this.dsKey = dsKey;
		this.resultKey = rowCount;
		this.sqlNode = sqlNode;
		this.simple = false;
		this.cacheClean = cacheClean;
	}

	@Override
	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
		SqlServiceContext context = (SqlServiceContext) serviceContext.getSqlServiceContext();

		context.resetExecEnv();

		sqlNode.execute(serviceContext, arg); // 获取sql
		// if (log.isInfoEnabled()) {
		// context.parseSqlLog();
		// }
		if (log.isInfoEnabled() || isTraceCommand()) {
			context.parseSqlLog();
		}

		long startTime = System.currentTimeMillis();
		context.beforeExecute(this, true);// 只是打开连接

		int count = context.executeDelete(this);
		if (null != this.resultKey) {
			Ognl.setValue(arg, this.resultKey, count);
		}

		context.afterExecute(this);
		if (log.isInfoEnabled()) {
			log.info("sql execution time: " + getSlowServiceLog(startTime));
		}

		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		return true;
	}

}
