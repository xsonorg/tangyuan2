package org.xson.tangyuan.app;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.util.ClassUtils;

/**
 * 静态工具方法容器
 */
public class StaticMethodContainer {

	private static StaticMethodContainer	instance	= new StaticMethodContainer();

	private Log								log			= LogFactory.getLog(getClass());

	private StaticMethodContainer() {
	}

	private Map<String, Method> methodMap = new ConcurrentHashMap<>();

	private Method getStaticMethod0(String key) {
		return methodMap.get(key);
	}

	private Method parseStaticMethod(String fullName) {
		int lastpos = fullName.lastIndexOf(".");
		if (lastpos < 0) {
			throw new TangYuanException("Illegal method call name: " + fullName);
		}
		String className = fullName.substring(0, lastpos);
		String methodName = fullName.substring(lastpos + 1);
		Class<?> clazz = ClassUtils.forName(className);
		Method[] methods = clazz.getMethods();
		for (Method m : methods) {
			if (m.getName().equals(methodName)) {
				if (!Modifier.isStatic(m.getModifiers())) {
					throw new TangYuanException("The method invoked in XML must be static: " + fullName);
				}
				return m;
			}
		}
		throw new TangYuanException("Non-existent method call name: " + fullName);
	}

	private void register0(String shortName, String methodFullName) {
		String registerName = shortName;
		if (null == registerName) {
			registerName = methodFullName;
		}
		if (this.methodMap.containsKey(registerName)) {
			throw new TangYuanException("已存在的静态方法: " + registerName);
		}
		Method m = parseStaticMethod(methodFullName);
		this.methodMap.put(registerName, m);

		log.info("add static method: " + registerName);
	}

	public static Method getStaticMethod(String method) {
		return instance.getStaticMethod0(method);
	}

	/**
	 * 注册一个静态方法
	 */
	public static void register(String shortName, String methodFullName) {
		instance.register0(shortName, methodFullName);
	}

	public static void register(String methodFullName) {
		instance.register0(null, methodFullName);
	}

}
