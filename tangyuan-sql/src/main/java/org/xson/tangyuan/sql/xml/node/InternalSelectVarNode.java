package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.sql.executor.SqlServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的SelectVarNode
 */
public class InternalSelectVarNode extends AbstractSqlNode {

	private static Log	log	= LogFactory.getLog(InternalSelectVarNode.class);

	// 返回结果的key
	private String		resultKey;
	private CacheUseVo	cacheUse;

	public InternalSelectVarNode(String dsKey, String resultKey, TangYuanNode sqlNode, CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
		this.simple = false;
		this.cacheUse = cacheUse;
	}

	@Override
	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
		SqlServiceContext context = (SqlServiceContext) serviceContext.getSqlServiceContext();

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
		// if (log.isInfoEnabled()) {
		// context.parseSqlLog();
		// }
		if (log.isInfoEnabled() || isTraceCommand()) {
			context.parseSqlLog();
		}

		long startTime = System.currentTimeMillis();
		context.beforeExecute(this, true);// 只是打开连接

		Object result = context.executeSelectVar(this);
		if (null != this.resultKey) {
			Ognl.setValue(arg, this.resultKey, result);
		}

		context.afterExecute(this);

		if (log.isInfoEnabled()) {
			log.info("sql execution time: " + getSlowServiceLog(startTime));
		}

		if (null != cacheUse) {
			cacheUse.putObject(arg, result);
		}

		return true;
	}

}
