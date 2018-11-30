package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.sql.executor.SqlServiceContext;
import org.xson.tangyuan.sql.executor.SqlServiceExceptionInfo;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class DeleteNode extends AbstractSqlNode {

	private static Log		log	= LogFactory.getLog(DeleteNode.class);

	private CacheCleanVo	cacheClean;

	public DeleteNode(String id, String ns, String serviceKey, String dsKey, XTransactionDefinition txDef, TangYuanNode sqlNode,
			CacheCleanVo cacheClean) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.txDef = txDef;
		this.simple = true;
		this.cacheClean = cacheClean;
	}

	@Override
	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
		SqlServiceContext sqlContext = (SqlServiceContext) serviceContext.getSqlServiceContext();

		// 1. 清理和重置执行环境
		sqlContext.resetExecEnv();

		boolean createdTranscation = false;
		long startTime = 0L;
		try {
			// 2. 解析SQL
			sqlNode.execute(serviceContext, arg); // 获取sql
			// if (log.isInfoEnabled()) {
			// sqlContext.parseSqlLog();
			// }
			if (log.isInfoEnabled() || isTraceCommand()) {
				sqlContext.parseSqlLog();
			}

			// 3. 开启事务
			startTime = System.currentTimeMillis();
			sqlContext.beforeExecute(this); // 开启事务异常, 可认为是事务之前的异常
			createdTranscation = true; // *

			// 4. 执行SQL
			int result = sqlContext.executeDelete(this);

			// 5. 提交:这里做不确定的提交
			sqlContext.commit(false);
			if (txDef.isNewTranscation()) {
				createdTranscation = false; // *
			}
			sqlContext.afterExecute(this);

			// 6. 设置结果
			serviceContext.setResult(result);
			if (log.isInfoEnabled()) {
				log.info("sql execution time: " + getSlowServiceLog(startTime));
			}
		} catch (Throwable e) {
			serviceContext.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), createdTranscation));
			throw e;
		}

		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		return true;
	}

}
