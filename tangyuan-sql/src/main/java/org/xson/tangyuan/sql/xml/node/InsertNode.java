package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.sql.executor.SqlServiceContext;
import org.xson.tangyuan.sql.executor.SqlServiceExceptionInfo;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class InsertNode extends AbstractSqlNode {

	private static Log		log	= LogFactory.getLog(InsertNode.class);

	private CacheCleanVo	cacheClean;

	// 插入后的返回类型, 这里使用resultType, long, array, list, 空认为返回行数

	public InsertNode(String id, String ns, String serviceKey, Class<?> resultType, String dsKey, XTransactionDefinition txDef, TangYuanNode sqlNode,
			CacheCleanVo cacheClean) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.resultType = resultType;

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
			if (log.isInfoEnabled()) {
				sqlContext.parseSqlLog();
			}
			// 3. 开启事务
			startTime = System.currentTimeMillis();
			sqlContext.beforeExecute(this); // 开启事务异常, 可认为是事务之前的异常
			createdTranscation = true; // *
			// 4. 执行SQL
			Object result = null;
			if (null == resultType) {
				result = sqlContext.executeInsert(this);
			} else {
				InsertReturn insertReturn = sqlContext.executeInsertReturn(this);
				result = insertReturn.getColumns();
			}
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
			// 考虑此处设置当前事务的特征, 上层统一处理(无论如何)
			serviceContext.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), createdTranscation));
			throw e;
		}

		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		return true;
	}
}
