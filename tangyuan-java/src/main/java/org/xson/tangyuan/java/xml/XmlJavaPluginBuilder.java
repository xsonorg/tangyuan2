package org.xson.tangyuan.java.xml;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.java.xml.node.JavaServiceNode;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.PatternMatchUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.DefaultXmlPluginBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

public class XmlJavaPluginBuilder extends DefaultXmlPluginBuilder {

	private XmlJavaContext componentContext = null;

	@Override
	public void setContext(String resource, XmlContext xmlContext) throws Throwable {
		this.componentContext = (XmlJavaContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "javaservices", false);
	}

	@Override
	public void clean() {
		super.clean();
		this.componentContext = null;
	}

	@Override
	public void parseRef() throws Throwable {

	}

	@Override
	public void parseService() throws Throwable {
		log.info(lang("xml.start.parsing.type", "plugin[service]", this.resource));
		configurationElement();
	}

	private void configurationElement() throws Throwable {
		buildServiceNode(this.root.evalNodes("service"));
	}

	private void buildServiceNode(List<XmlNodeWrapper> contexts) {
		String tagName = "service";
		for (XmlNodeWrapper xNode : contexts) {

			String ns = getStringFromAttr(xNode, "ns");
			String clazz = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));

			// 检查clazz重复
			if (this.globalContext.getIntegralServiceClassMap().containsKey(clazz)) {
				throw new XmlParseException(lang("xml.tag.service.class.repeated", clazz, tagName, this.resource));
			}
			this.globalContext.getIntegralServiceClassMap().put(clazz, 1);

			if (StringUtils.isEmptySafe(ns)) {
				ns = getSimpleClassName(clazz);
			}
			// 检查ns重复
			checkNs(ns);

			// include and exclude
			List<String> includeList = new ArrayList<String>();
			List<String> excludeList = new ArrayList<String>();
			// <include></include>
			List<XmlNodeWrapper> includeNodes = xNode.evalNodes("include");
			for (XmlNodeWrapper include : includeNodes) {
				String body = StringUtils.trimEmpty(include.getStringBody());
				if (null != body) {
					includeList.add(body);
				}
			}
			// <exclude></exclude>
			List<XmlNodeWrapper> excludeNodes = xNode.evalNodes("exclude");
			for (XmlNodeWrapper exclude : excludeNodes) {
				String body = StringUtils.trimEmpty(exclude.getStringBody());
				if (null != body) {
					excludeList.add(body);
				}
			}

			// info
			// Map<String, JavaServiceInfoVo> serviceInfoMap = parseServiceCache(xNode.evalNodes("methodCache"));
			Map<String, JavaServiceInfoVo> serviceInfoMap = new HashMap<String, JavaServiceInfoVo>();
			parseServiceMethod(xNode.evalNodes("method"), serviceInfoMap, "method");
			// 兼容老的schema
			parseServiceCache(xNode.evalNodes("methodCache"), serviceInfoMap, "methodCache");

			// method->service
			parseClass(ns, clazz, includeList, excludeList, serviceInfoMap);
		}
	}

	private void parseServiceMethod(List<XmlNodeWrapper> contexts, Map<String, JavaServiceInfoVo> serviceInfoMap, String tagName) {
		for (XmlNodeWrapper xNode : contexts) {
			String method = getStringFromAttr(xNode, "name", lang("xml.tag.attribute.empty", "name", tagName, this.resource));
			String cacheUse = getStringFromAttr(xNode, "cacheUse");
			String cacheClean = getStringFromAttr(xNode, "cacheClean");
			String desc = getStringFromAttr(xNode, "desc");
			String[] groups = getStringArrayFromAttr(xNode, "group");
			boolean forcedCloneArg = getBoolFromAttr(xNode, "cloneArg", false);

			if (serviceInfoMap.containsKey(method)) {
				throw new XmlParseException(lang("xml.tag.attribute.repeated", method, tagName, this.resource));
			}
			JavaServiceInfoVo serviceInfoVo = new JavaServiceInfoVo(cacheUse, cacheClean, desc, groups, forcedCloneArg);
			serviceInfoMap.put(method, serviceInfoVo);
		}
	}

	/** 兼容老的schema */
	private void parseServiceCache(List<XmlNodeWrapper> contexts, Map<String, JavaServiceInfoVo> serviceInfoMap, String tagName) {
		for (XmlNodeWrapper xNode : contexts) {
			String method = getStringFromAttr(xNode, "method", lang("xml.tag.attribute.empty", "method", tagName, this.resource));
			String cacheUse = getStringFromAttr(xNode, "cacheUse");
			String cacheClean = getStringFromAttr(xNode, "cacheClean");
			String desc = getStringFromAttr(xNode, "desc");
			String[] groups = getStringArrayFromAttr(xNode, "group");
			boolean forcedCloneArg = getBoolFromAttr(xNode, "cloneArg", false);

			if (serviceInfoMap.containsKey(method)) {
				throw new XmlParseException(lang("xml.tag.attribute.repeated", method, tagName, this.resource));
			}
			JavaServiceInfoVo serviceInfoVo = new JavaServiceInfoVo(cacheUse, cacheClean, desc, groups, forcedCloneArg);
			serviceInfoMap.put(method, serviceInfoVo);
		}
	}

	/** 方法解析 */
	private void parseClass(String ns, String className, List<String> includeList, List<String> excludeList,
			Map<String, JavaServiceInfoVo> serviceInfoMap) {
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

			String fullId = getFullId(ns, m.getName());
			checkServiceRepeated(m.getName(), fullId, "service");

			// 解析服务的其他信息
			String desc = null;
			String[] groups = null;
			boolean forcedCloneArg = false;
			CacheUseVo cacheUse = null;
			CacheCleanVo cacheClean = null;
			JavaServiceInfoVo serviceInfoVo = serviceInfoMap.get(m.getName());
			if (null != serviceInfoVo) {
				if (null != serviceInfoVo.cacheUse) {
					cacheUse = CacheUseVo.parseCacheUse(serviceInfoVo.cacheUse, fullId);
				}
				if (null != serviceInfoVo.cacheClean) {
					cacheClean = CacheCleanVo.parseCacheClean(serviceInfoVo.cacheClean, fullId);
				}
				desc = serviceInfoVo.desc;
				groups = serviceInfoVo.groups;
				forcedCloneArg = serviceInfoVo.forcedCloneArg;
			}

			AbstractServiceNode service = new JavaServiceNode(m.getName(), ns, fullId, instance, m, cacheUse, cacheClean, forcedCloneArg, desc,
					groups);
			registerService(service, "service");
		}
	}

	/**
	 * 默认的ns, 取类的名称
	 */
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

	class JavaServiceInfoVo {

		String		cacheUse;
		String		cacheClean;

		String		desc;
		String[]	groups;

		boolean		forcedCloneArg;

		public JavaServiceInfoVo(String cacheUse, String cacheClean, String desc, String[] groups, boolean forcedCloneArg) {
			this.cacheUse = cacheUse;
			this.cacheClean = cacheClean;
			this.desc = desc;
			this.groups = groups;
			this.forcedCloneArg = forcedCloneArg;
		}
	}

}
