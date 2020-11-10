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

			String ns    = getStringFromAttr(xNode, "ns");
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
			List<String>         includeList  = new ArrayList<String>();
			List<String>         excludeList  = new ArrayList<String>();
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
			String   method         = getStringFromAttr(xNode, "name", lang("xml.tag.attribute.empty", "name", tagName, this.resource));
			String   cacheUse       = getStringFromAttr(xNode, "cacheUse");
			String   cacheClean     = getStringFromAttr(xNode, "cacheClean");
			String   desc           = getStringFromAttr(xNode, "desc");
			String[] groups         = getStringArrayFromAttr(xNode, "group");
			boolean  forcedCloneArg = getBoolFromAttr(xNode, "cloneArg", false);

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
			String   method         = getStringFromAttr(xNode, "method", lang("xml.tag.attribute.empty", "method", tagName, this.resource));
			String   cacheUse       = getStringFromAttr(xNode, "cacheUse");
			String   cacheClean     = getStringFromAttr(xNode, "cacheClean");
			String   desc           = getStringFromAttr(xNode, "desc");
			String[] groups         = getStringArrayFromAttr(xNode, "group");
			boolean  forcedCloneArg = getBoolFromAttr(xNode, "cloneArg", false);

			if (serviceInfoMap.containsKey(method)) {
				throw new XmlParseException(lang("xml.tag.attribute.repeated", method, tagName, this.resource));
			}
			JavaServiceInfoVo serviceInfoVo = new JavaServiceInfoVo(cacheUse, cacheClean, desc, groups, forcedCloneArg);
			serviceInfoMap.put(method, serviceInfoVo);
		}
	}

	/** 方法解析 */
	private void parseClass(String ns, String className, List<String> includeList, List<String> excludeList, Map<String, JavaServiceInfoVo> serviceInfoMap) {
		Class<?> clazz    = ClassUtils.forName(className);
		Object   instance = TangYuanUtil.newInstance(clazz);

		Method[] methods  = clazz.getMethods();
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
			if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isNative(modifiers) || Modifier.isInterface(modifiers) || Modifier.isAbstract(modifiers)
					|| Modifier.isStrict(modifiers)) {
				continue;
			}

			String fullId = getFullId(ns, m.getName());
			checkServiceRepeated(m.getName(), fullId, "service");

			// 解析服务的其他信息
			String            desc           = null;
			String[]          groups         = null;
			boolean           forcedCloneArg = false;
			CacheUseVo        cacheUse       = null;
			CacheCleanVo      cacheClean     = null;
			JavaServiceInfoVo serviceInfoVo  = serviceInfoMap.get(m.getName());
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

			AbstractServiceNode service = new JavaServiceNode(m.getName(), ns, fullId, instance, m, cacheUse, cacheClean, forcedCloneArg, desc, groups);
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

		String   cacheUse;
		String   cacheClean;

		String   desc;
		String[] groups;

		boolean  forcedCloneArg;

		public JavaServiceInfoVo(String cacheUse, String cacheClean, String desc, String[] groups, boolean forcedCloneArg) {
			this.cacheUse = cacheUse;
			this.cacheClean = cacheClean;
			this.desc = desc;
			this.groups = groups;
			this.forcedCloneArg = forcedCloneArg;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	//	class JavaServiceCacheVo {
	//		String   cacheUse;
	//		String   cacheClean;
	//
	//		public JavaServiceCacheVo(String cacheUse, String cacheClean) {
	//			this.cacheUse = cacheUse;
	//			this.cacheClean = cacheClean;
	//		}
	//	}
	//	private Map<String, JavaServiceInfoVo> parseServiceCache(List<XmlNodeWrapper> contexts) {
	//		String                         tagName        = "methodCache";
	//		Map<String, JavaServiceInfoVo> serviceInfoMap = new HashMap<String, JavaServiceInfoVo>();
	//		for (XmlNodeWrapper xNode : contexts) {
	//			String method     = getStringFromAttr(xNode, "method", lang("xml.tag.attribute.empty", "method", tagName, this.resource));
	//			String cacheUse   = getStringFromAttr(xNode, "cacheUse");
	//			String cacheClean = getStringFromAttr(xNode, "cacheClean");
	//			if (serviceInfoMap.containsKey(method)) {
	//				throw new XmlParseException(lang("xml.tag.attribute.repeated", method, tagName, this.resource));
	//			}
	//			serviceInfoMap.put(method, new JavaServiceInfoVo(cacheUse, cacheClean));
	//		}
	//		return serviceInfoMap;
	//	}

	// existingService(getFullId(ns, m.getName()));
	//			existingService(fullId);
	//					cacheUse = parseCacheUse(jscVo.cacheUse, fullId);
	//					cacheClean = parseCacheClean(jscVo.cacheClean, fullId);
	// private AbstractServiceNode createService(Object instance, Method method, String ns) {
	// return new JavaServiceNode(method.getName(), ns, getFullId(ns, method.getName()), instance, method);
	// }
	//	/**
	//	 * 解析: ID:xxx; key:xxx; expiry:10(秒)
	//	 */
	//	private CacheUseVo parseCacheUse(String cacheUse, String service) {
	//		CacheUseVo cacheUseVo = null;
	//		String[]   array      = cacheUse.split(";");
	//		if (array.length > 0) {
	//			Map<String, String> map = new HashMap<String, String>();
	//			for (int i = 0; i < array.length; i++) {
	//				String[] item = array[i].split(":");
	//				map.put(item[0].trim().toUpperCase(), item[1].trim());
	//			}
	//			TangYuanCache cache = CacheComponent.getInstance().getCache(map.get("id".toUpperCase()));
	//			if (null == cache) {
	//				throw new XmlParseException("Non-existent cache: " + cacheUse);
	//			}
	//			String key = map.get("key".toUpperCase());
	//			if (null == key) {
	//				throw new XmlParseException("Missing cache.key: " + cacheUse);
	//			}
	//			Long expiry = null;
	//			if (map.containsKey("expiry".toUpperCase())) {
	//				expiry = Long.parseLong(map.get("expiry".toUpperCase()));
	//			}
	//			cacheUseVo = new CacheUseVo(cache, key, expiry, service);
	//		}
	//		return cacheUseVo;
	//	}

	//	/**
	//	 * 解析: ID:xxx; key:xxx;
	//	 */
	//	private CacheCleanVo parseCacheClean(String cacheUse, String service) {
	//		CacheCleanVo cacheCleanVo = null;
	//		String[]     array        = cacheUse.split(";");
	//		if (array.length > 0) {
	//			Map<String, String> map = new HashMap<String, String>();
	//			for (int i = 0; i < array.length; i++) {
	//				String[] item = array[i].split(":");
	//				map.put(item[0].trim().toUpperCase(), item[1].trim());
	//			}
	//			TangYuanCache cache = CacheComponent.getInstance().getCache(map.get("id".toUpperCase()));
	//			if (null == cache) {
	//				throw new XmlParseException("Non-existent cache: " + cacheUse);
	//			}
	//			String key = map.get("key".toUpperCase());
	//			if (null == key) {
	//				throw new XmlParseException("Missing cache.key: " + cacheUse);
	//			}
	//			cacheCleanVo = new CacheCleanVo(cache, key, service);
	//		}
	//		return cacheCleanVo;
	//	}

	//////////////

	//	private void configurationElement() {
	//		List<XmlNodeWrapper> contexts = this.root.evalNodes("service");
	//		String               tagName  = "service";
	//		for (XmlNodeWrapper xNode : contexts) {
	//			//			String ns    = StringUtils.trim(context.getStringAttribute("ns"));
	//			//			String clazz = StringUtils.trim(context.getStringAttribute("class"));
	//			String ns    = getStringFromAttr(xNode, "ns");
	//			String clazz = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
	//
	//			if (null == clazz || clazz.length() == 0) {
	//				throw new XmlParseException("Invalid service class");
	//			}
	//			// 判断clazz重复
	//			if (this.globalContext.getIntegralServiceClassMap().containsKey(clazz)) {
	//				throw new XmlParseException("Duplicate service class: " + clazz);
	//			}
	//
	//			if (null == ns || ns.trim().length() == 0) {
	//				ns = getSimpleClassName(clazz);
	//			}
	//
	//			// 判断ns重复
	//			if (this.globalContext.getIntegralServiceNsMap().containsKey(ns)) {
	//				throw new XmlParseException("Duplicate service ns: " + ns);
	//			}
	//
	//			List<String>         includeList  = new ArrayList<String>();
	//			List<String>         excludeList  = new ArrayList<String>();
	//
	//			// <include></include>
	//			List<XmlNodeWrapper> includeNodes = context.evalNodes("include");
	//			for (XmlNodeWrapper include : includeNodes) {
	//				String body = StringUtils.trim(include.getStringBody());
	//				if (null != body) {
	//					includeList.add(body);
	//				}
	//			}
	//			// <exclude></exclude>
	//			List<XmlNodeWrapper> excludeNodes = context.evalNodes("exclude");
	//			for (XmlNodeWrapper exclude : excludeNodes) {
	//				String body = StringUtils.trim(exclude.getStringBody());
	//				if (null != body) {
	//					excludeList.add(body);
	//				}
	//			}
	//
	//			// 缓存解析
	//			// Map<String, JavaServiceCacheVo> serviceCacheMap = parseServiceCache(context.evalNodes("cache"));
	//			Map<String, JavaServiceCacheVo> serviceCacheMap = parseServiceCache(context.evalNodes("methodCache"));
	//
	//			parseClass(ns, clazz, includeList, excludeList, serviceCacheMap);
	//
	//			this.context.getIntegralServiceNsMap().put(ns, 1);
	//			this.context.getIntegralServiceClassMap().put(clazz, 1);
	//		}
	//	}

	//	public Map<String, JavaServiceCacheVo> parseServiceCache(List<XmlNodeWrapper> contexts) {
	//		String                          tagName         = "methodCache";
	//		Map<String, JavaServiceCacheVo> serviceCacheMap = new HashMap<String, JavaServiceCacheVo>();
	//		for (XmlNodeWrapper xNode : contexts) {
	//
	//			//			String method     = StringUtils.trim(context.getStringAttribute("method"));
	//			//			String cacheUse   = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//			//			String cacheClean = StringUtils.trim(context.getStringAttribute("cacheClean"));
	//
	//			String method     = getStringFromAttr(xNode, "method", lang("xml.tag.attribute.empty", "method", tagName, this.resource));
	//			String cacheUse   = getStringFromAttr(xNode, "cacheUse");
	//			String cacheClean = getStringFromAttr(xNode, "cacheClean");
	//
	//			TangYuanAssert.stringEmpty(method, "In tag <cache>, attribute 'method' can not be empty.");
	//			if (serviceCacheMap.containsKey(method)) {
	//				throw new XmlParseException("method [" + method + "] repeats the setting cache.");
	//			}
	//			serviceCacheMap.put(method, new JavaServiceCacheVo(cacheUse, cacheClean));
	//		}
	//		return serviceCacheMap;
	//	}
	//	private XmlGlobalContext context = null;
	//	@Override
	//	public void setContext(XmlNodeWrapper root, XmlContext context) {
	//		this.context = (XmlGlobalContext) context;
	//		this.root = root;
	//	}
	//	private void existingService(String fullId) {
	//		if (null != this.context.getIntegralServiceMap().get(fullId)) {
	//			throw new XmlParseException("Duplicate service nodes: " + fullId);
	//		}
	//		if (null != this.context.getIntegralRefMap().get(fullId)) {
	//			throw new XmlParseException("Duplicate service nodes: " + fullId);
	//		}
	//		this.context.getIntegralServiceMap().put(fullId, 1);
	//	}

	//	protected String getFullId(String ns, String id) {
	//		return TangYuanUtil.getQualifiedName(ns, id, null, TangYuanContainer.getInstance().getNsSeparator());
	//	}
}
