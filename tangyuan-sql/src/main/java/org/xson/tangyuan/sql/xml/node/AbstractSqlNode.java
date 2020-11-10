package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public abstract class AbstractSqlNode extends AbstractServiceNode {

	protected TangYuanNode				sqlNode		= null;
	protected String					dsKey		= null;
	protected XTransactionDefinition	txDef		= null;
	protected boolean					simple		= false;
	protected CacheUseVo				cacheUse	= null;
	protected CacheCleanVo				cacheClean	= null;

	protected AbstractSqlNode() {
		this.serviceType = TangYuanServiceType.SQL;
	}

	public XTransactionDefinition getTxDef() {
		return txDef;
	}

	public String getDsKey() {
		return dsKey;
	}

	public boolean isSimple() {
		return simple;
	}

	protected Log getLog() {
		return null;
	}

	protected void removeCache(ActuatorContext ac, String cacheKey) {
		ac.addPostTask(new Runnable() {
			@Override
			public void run() {
				cacheClean.removeObject(cacheKey);
			}
		});
	}

	protected void putCache(ActuatorContext ac, String cacheKey, Object result) {
		ac.addPostTask(new Runnable() {
			@Override
			public void run() {
				cacheUse.putObject(cacheKey, result);
			}
		});
	}

	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {

		SqlServiceContext sqlContext = (SqlServiceContext) ac.getServiceContext(this.serviceType);
		long startTime = System.currentTimeMillis();
		String cacheKey = null;
		Object result = null;

		// 1. cache使用
		if (null != this.cacheUse && null == cacheKey) {
			cacheKey = this.cacheUse.buildKey(arg);
		}
		if (null != this.cacheClean && null == cacheKey) {
			cacheKey = this.cacheClean.buildKey(arg);
		}
		if (null != this.cacheUse) {
			result = this.cacheUse.getObject(cacheKey);
			if (null != result) {
				ac.setResult(result);
				if (getLog().isInfoEnabled()) {
					getLog().info("sql execution time: " + getSlowServiceLog(startTime) + " use cache");
				}
				return true;
			}
		}

		// 0. 克隆参数
		// if (null == temp) {
		// temp = cloneArg(arg);
		// }

		// 1. 清理和重置执行环境
		sqlContext.resetExecEnv();

		// 2. 解析SQL
		sqlNode.execute(ac, arg, temp);
		if (getLog().isInfoEnabled() || sqlContext.isTraceCommand()) {
			// 准备日志输出的SQL
			sqlContext.parseSqlLog();
		}
		// 3. 开启事务
		// sqlContext.beforeExecute(this);
		// 5. 提交:这里做不确定的提交
		// sqlContext.commit(false);
		// 6. 后置处理
		// sqlContext.afterExecute(this);
		// 4. 执行SQL
		// result = sqlContext.executeSelectSetListXCO(this, this.resultMap, fetchSize);
		// 7. 设置结果
		// ac.setResult(result);
		// result = executeSql(sqlContext, temp);

		result = executeSql(ac, sqlContext, temp);

		if (getLog().isInfoEnabled()) {
			getLog().info("sql execution time: " + getSlowServiceLog(startTime));
		}

		// 8. 放置缓存
		if (null != cacheUse) {
			// 需要在事物提交后，或者finish的时候再处理
			putCache(ac, cacheKey, result);
		}
		// 8. 清理缓存
		if (null != cacheClean) {
			removeCache(ac, cacheKey);
		}

		return true;
	}

	protected Object executeSql(ActuatorContext ac, SqlServiceContext sqlContext, Object temp) throws Throwable {
		// return null;
		throw new TangYuanException("subclass must implement this method");
	}

}
