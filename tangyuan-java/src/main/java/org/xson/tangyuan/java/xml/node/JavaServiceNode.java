package org.xson.tangyuan.java.xml.node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

/**
 * JAVA服务
 */
public class JavaServiceNode extends AbstractServiceNode {

	private static Log   log            = LogFactory.getLog(JavaServiceNode.class);

	private Object       instance       = null;
	private Method       method         = null;
	private CacheUseVo   cacheUse       = null;
	private CacheCleanVo cacheClean     = null;

	private boolean      forcedCloneArg = false;

	public JavaServiceNode(String id, String ns, String serviceKey, Object instance, Method method, CacheUseVo cacheUse, CacheCleanVo cacheClean, boolean forcedCloneArg,
			String desc, String[] groups) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.instance = instance;
		this.method = method;
		this.serviceType = TangYuanServiceType.JAVA;

		this.cacheUse = cacheUse;
		this.cacheClean = cacheClean;

		this.desc = desc;
		this.groups = groups;

		this.forcedCloneArg = forcedCloneArg;
	}

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {

		long   startTime = System.currentTimeMillis();
		String cacheKey  = null;
		Object result    = null;

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
				if (log.isInfoEnabled()) {
					log.info("java execution time: " + getSlowServiceLog(startTime) + " use cache");
				}
				return true;
			}
		}

		// 0. 克隆参数
		if (this.forcedCloneArg) {
			arg = cloneArg(arg);
		}

		try {
			//			result = method.invoke(instance, temp);
			//			result = method.invoke(instance, cloneArg(arg));
			result = method.invoke(instance, arg);
			ac.setResult(result);
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw ((InvocationTargetException) e).getTargetException();
			} else {
				throw e;
			}
		}
		if (log.isInfoEnabled()) {
			log.info("java execution time: " + getSlowServiceLog(startTime));
		}

		// 8. 放置缓存
		if (null != cacheUse) {
			putCache(ac, cacheKey, result);
		}
		// 8. 清理缓存
		if (null != cacheClean) {
			removeCache(ac, cacheKey);
		}

		return true;
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

}
