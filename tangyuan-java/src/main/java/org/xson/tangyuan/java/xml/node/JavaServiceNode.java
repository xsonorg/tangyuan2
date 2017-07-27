package org.xson.tangyuan.java.xml.node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

/**
 * JAVA服务
 */
public class JavaServiceNode extends AbstractServiceNode {

	private static Log	log	= LogFactory.getLog(JavaServiceNode.class);

	private Object		instance;

	private Method		method;

	public JavaServiceNode(String id, String ns, String serviceKey, Object instance, Method method) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.instance = instance;
		this.method = method;
		this.serviceType = TangYuanServiceType.JAVA;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		Object result = null;
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
		return true;
	}

}
