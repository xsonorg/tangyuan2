package org.xson.tangyuan.java.xml.node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

/**
 * JAVA服务
 */
public class JavaServiceNode extends AbstractServiceNode {

	private static Log		log	= LogFactory.getLog(JavaServiceNode.class);

	private Object			instance;

	private Method			method;

	private CacheUseVo		cacheUse;

	private CacheCleanVo	cacheClean;

	// public JavaServiceNode(String id, String ns, String serviceKey, Object instance, Method method) {
	// this.id = id;
	// this.ns = ns;
	// this.serviceKey = serviceKey;
	// this.instance = instance;
	// this.method = method;
	// this.serviceType = TangYuanServiceType.JAVA;
	// }

	public JavaServiceNode(String id, String ns, String serviceKey, Object instance, Method method, CacheUseVo cacheUse, CacheCleanVo cacheClean) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.instance = instance;
		this.method = method;
		this.serviceType = TangYuanServiceType.JAVA;

		this.cacheUse = cacheUse;
		this.cacheClean = cacheClean;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		Object result = null;

		// 1. cache使用
		if (null != cacheUse) {
			result = cacheUse.getObject(arg);
			if (null != result) {
				context.setResult(result);
				return true;
			}
		}

		long startTime = System.currentTimeMillis();
		try {
			result = method.invoke(instance, arg);
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
		context.setResult(result);

		if (null != cacheUse) {
			cacheUse.putObject(arg, result);
		}
		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		return true;
	}

}
