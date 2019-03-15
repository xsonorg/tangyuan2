package org.xson.tangyuan.hive.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.hive.executor.HiveServiceContext;
import org.xson.tangyuan.hive.transaction.XTransactionDefinition;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class HqlNode extends AbstractSqlNode {

	private static Log		log			= LogFactory.getLog(HqlNode.class);

	private CacheCleanVo	cacheClean	= null;

	private boolean			asyncHql	= false;

	public HqlNode(String id, String ns, String serviceKey, String dsKey, XTransactionDefinition txDef, TangYuanNode sqlNode, CacheCleanVo cacheClean,
			boolean asyncHql) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.txDef = txDef;
		this.simple = true;
		this.cacheClean = cacheClean;
		this.asyncHql = asyncHql;
	}

	@Override
	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {

		HiveServiceContext sqlContext = (HiveServiceContext) serviceContext.getServiceContext(TangYuanServiceType.HIVE);

		// 1. 清理和重置执行环境
		sqlContext.resetExecEnv();
		long startTime = 0L;
		// 2. 解析SQL.获取SQL
		sqlNode.execute(serviceContext, arg);
		if (log.isInfoEnabled() || isTraceCommand()) {
			sqlContext.parseSqlLog();
		}
		// 3. 开启事务
		startTime = System.currentTimeMillis();
		sqlContext.beforeExecute(this); // 开启事务异常, 可认为是事务之前的异常
		// 4. 执行SQL
		// boolean result = sqlContext.executeHql(this);
		sqlContext.executeHql(this, this.asyncHql);
		// 5. 提交
		// sqlContext.commit(false);
		sqlContext.afterExecute(this);
		// 6. 设置结果
		// serviceContext.setResult(result ? 0 : -1);
		// serviceContext.setResult(0);
		if (log.isInfoEnabled()) {
			log.info("hql execution time: " + getSlowServiceLog(startTime));
		}

		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		return true;
	}

}
