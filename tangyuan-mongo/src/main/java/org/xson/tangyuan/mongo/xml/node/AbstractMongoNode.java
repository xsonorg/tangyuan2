package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public abstract class AbstractMongoNode extends AbstractServiceNode {

	protected TangYuanNode sqlNode    = null;
	protected String       dsKey      = null;
	protected CacheUseVo   cacheUse   = null;
	protected CacheCleanVo cacheClean = null;

	protected boolean      simple;

	protected AbstractMongoNode() {
		this.serviceType = TangYuanServiceType.MONGO;
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

		MongoServiceContext context   = (MongoServiceContext) ac.getServiceContext(this.serviceType);
		long                startTime = System.currentTimeMillis();
		String              cacheKey  = null;
		Object              result    = null;

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
					getLog().info("mongo execution time: " + getSlowServiceLog(startTime) + " use cache");
				}
				return true;
			}
		}

		// 0. 克隆参数
		//		if (null == temp) {
		//			temp = cloneArg(arg);
		//		}

		// 1. 清理和重置执行环境
		context.resetExecEnv();

		// 2. 解析SQL
		sqlNode.execute(ac, arg, temp);

		//		if (getLog().isInfoEnabled() || context.isTraceCommand()) {
		//			// 准备日志输出的SQL
		//			context.parseSqlLog();
		//		}

		// 3. 执行
		result = executeSql(ac, context, temp);

		if (getLog().isInfoEnabled()) {
			getLog().info("mongo execution time: " + getSlowServiceLog(startTime));
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

	protected Object executeSql(ActuatorContext ac, MongoServiceContext context, Object temp) throws Throwable {
		throw new TangYuanException("subclass must implement this method");
	}

	// protected boolean isTraceCommand() {
	// return RuntimeContext.isTraceCommand(TrackingManager.SERVICE_TYPE_MONGO);
	// }
}
