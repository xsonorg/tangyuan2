package org.xson.tangyuan.java.xml.node;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.PatternMatchUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlNodeBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

public class XMLJavaNodeBuilder extends XmlNodeBuilder {

	private Log					log		= LogFactory.getLog(getClass());
	private XmlNodeWrapper		root	= null;
	private XmlGlobalContext	context	= null;

	@Override
	public Log getLog() {
		return this.log;
	}

	@Override
	public void setContext(XmlNodeWrapper root, XmlContext context) {
		this.context = (XmlGlobalContext) context;
		this.root = root;
	}

	private void existingService(String fullId) {
		if (null != this.context.getIntegralServiceMap().get(fullId)) {
			throw new XmlParseException("Duplicate service nodes: " + fullId);
		}
		if (null != this.context.getIntegralRefMap().get(fullId)) {
			throw new XmlParseException("Duplicate service nodes: " + fullId);
		}
		this.context.getIntegralServiceMap().put(fullId, 1);
	}

	protected String getFullId(String ns, String id) {
		return TangYuanUtil.getQualifiedName(ns, id, null, TangYuanContainer.getInstance().getNsSeparator());
	}

	@Override
	public void parseRef() {
	}

	@Override
	public void parseService() {
		List<XmlNodeWrapper> contexts = this.root.evalNodes("service");
		for (XmlNodeWrapper context : contexts) {
			String ns = StringUtils.trim(context.getStringAttribute("ns"));
			String clazz = StringUtils.trim(context.getStringAttribute("class"));

			if (null == clazz || clazz.length() == 0) {
				throw new XmlParseException("Invalid service class");
			}
			// 判断clazz重复
			if (this.context.getIntegralServiceClassMap().containsKey(clazz)) {
				throw new XmlParseException("Duplicate service class: " + clazz);
			}

			if (null == ns || ns.trim().length() == 0) {
				ns = getSimpleClassName(clazz);
			}

			// 判断ns重复
			if (this.context.getIntegralServiceNsMap().containsKey(ns)) {
				throw new XmlParseException("Duplicate service ns: " + ns);
			}

			List<String> includeList = new ArrayList<String>();
			List<String> excludeList = new ArrayList<String>();

			// <include></include>
			List<XmlNodeWrapper> includeNodes = context.evalNodes("include");
			for (XmlNodeWrapper include : includeNodes) {
				String body = StringUtils.trim(include.getStringBody());
				if (null != body) {
					includeList.add(body);
				}
			}
			// <exclude></exclude>
			List<XmlNodeWrapper> excludeNodes = context.evalNodes("exclude");
			for (XmlNodeWrapper exclude : excludeNodes) {
				String body = StringUtils.trim(exclude.getStringBody());
				if (null != body) {
					excludeList.add(body);
				}
			}

			parseClass(ns, clazz, includeList, excludeList);

			this.context.getIntegralServiceNsMap().put(ns, 1);
			this.context.getIntegralServiceClassMap().put(clazz, 1);
		}
	}

	/** 类解析 */
	private void parseClass(String ns, String className, List<String> includeList, List<String> excludeList) {
		Class<?> clazz = ClassUtils.forName(className);
		Object instance = TangYuanUtil.newInstance(clazz);

		Method[] methods = clazz.getMethods();
		for (Method m : methods) {
			if (isIgnoredMethod(m.getName())) {
				continue;
			}

			boolean continueFlag = false;

			for (String pattern : excludeList) {
				if (PatternMatchUtils.simpleMatch(pattern, m.getName())) {
					continueFlag = true;
					break;
				}
			}

			if (continueFlag) {
				continue;
			}

			if (includeList.size() > 0) {
				for (String pattern : includeList) {
					if (!PatternMatchUtils.simpleMatch(pattern, m.getName())) {
						continueFlag = true;
						break;
					}
				}
			}

			if (continueFlag) {
				continue;
			}

			// Ignored static method
			int modifiers = m.getModifiers();
			if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isNative(modifiers) || Modifier.isInterface(modifiers)
					|| Modifier.isAbstract(modifiers) || Modifier.isStrict(modifiers)) {
				continue;
			}

			existingService(getFullId(ns, m.getName()));

			AbstractServiceNode service = createService(instance, m, ns);
			registerService(service, "service");
		}
	}

	private AbstractServiceNode createService(Object instance, Method method, String ns) {
		return new JavaServiceNode(method.getName(), ns, getFullId(ns, method.getName()), instance, method);
	}

	// 取类的名称
	private String getSimpleClassName(String clazz) {
		int pos = clazz.lastIndexOf(".");
		if (pos > -1) {
			clazz = clazz.substring(pos + 1);
		}
		clazz = clazz.substring(0, 1).toLowerCase() + clazz.substring(1);
		return clazz;
	}

	/** 需要忽略的方法 */
	private String[] ignoredMethodName = { "main", "getClass", "hashCode", "equals", "toString", "notify", "notifyAll", "wait" };

	private boolean isIgnoredMethod(String name) {
		for (int i = 0; i < ignoredMethodName.length; i++) {
			if (name.equals(ignoredMethodName[i])) {
				return true;
			}
		}
		return false;
	}

}
