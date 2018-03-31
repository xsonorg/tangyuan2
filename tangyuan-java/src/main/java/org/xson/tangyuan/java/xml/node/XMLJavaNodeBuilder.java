package org.xson.tangyuan.java.xml.node;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.CacheComponent;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.PatternMatchUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanAssert;
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

			// 缓存解析
			// Map<String, JavaServiceCacheVo> serviceCacheMap = parseServiceCache(context.evalNodes("cache"));
			Map<String, JavaServiceCacheVo> serviceCacheMap = parseServiceCache(context.evalNodes("methodCache"));

			parseClass(ns, clazz, includeList, excludeList, serviceCacheMap);

			this.context.getIntegralServiceNsMap().put(ns, 1);
			this.context.getIntegralServiceClassMap().put(clazz, 1);
		}
	}

	public Map<String, JavaServiceCacheVo> parseServiceCache(List<XmlNodeWrapper> contexts) {
		Map<String, JavaServiceCacheVo> serviceCacheMap = new HashMap<String, JavaServiceCacheVo>();
		for (XmlNodeWrapper context : contexts) {
			String method = StringUtils.trim(context.getStringAttribute("method"));
			String cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
			String cacheClean = StringUtils.trim(context.getStringAttribute("cacheClean"));
			TangYuanAssert.stringEmpty(method, "In tag <cache>, attribute 'method' can not be empty.");
			if (serviceCacheMap.containsKey(method)) {
				throw new XmlParseException("method [" + method + "] repeats the setting cache.");
			}
			serviceCacheMap.put(method, new JavaServiceCacheVo(cacheUse, cacheClean));
		}
		return serviceCacheMap;
	}

	/** 类解析 */
	private void parseClass(String ns, String className, List<String> includeList, List<String> excludeList,
			Map<String, JavaServiceCacheVo> serviceCacheMap) {
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

			// existingService(getFullId(ns, m.getName()));
			existingService(fullId);

			// 解析缓存
			CacheUseVo cacheUse = null;
			CacheCleanVo cacheClean = null;
			JavaServiceCacheVo jscVo = serviceCacheMap.get(m.getName());
			if (null != jscVo) {
				if (null != jscVo.cacheUse && jscVo.cacheUse.length() > 0) {
					cacheUse = parseCacheUse(jscVo.cacheUse, fullId);
				}
				if (null != jscVo.cacheClean && jscVo.cacheClean.length() > 0) {
					cacheClean = parseCacheClean(jscVo.cacheClean, fullId);
				}
			}

			// AbstractServiceNode service = createService(instance, m, ns);
			AbstractServiceNode service = new JavaServiceNode(m.getName(), ns, fullId, instance, m, cacheUse, cacheClean);
			registerService(service, "service");
		}
	}

	// private AbstractServiceNode createService(Object instance, Method method, String ns) {
	// return new JavaServiceNode(method.getName(), ns, getFullId(ns, method.getName()), instance, method);
	// }

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

	/**
	 * 解析: ID:xxx; key:xxx; expiry:10(秒)
	 */
	private CacheUseVo parseCacheUse(String cacheUse, String service) {
		CacheUseVo cacheUseVo = null;
		String[] array = cacheUse.split(";");
		if (array.length > 0) {
			Map<String, String> map = new HashMap<String, String>();
			for (int i = 0; i < array.length; i++) {
				String[] item = array[i].split(":");
				map.put(item[0].trim().toUpperCase(), item[1].trim());
			}
			TangYuanCache cache = CacheComponent.getInstance().getCache(map.get("id".toUpperCase()));
			if (null == cache) {
				throw new XmlParseException("Non-existent cache: " + cacheUse);
			}
			String key = map.get("key".toUpperCase());
			if (null == key) {
				throw new XmlParseException("Missing cache.key: " + cacheUse);
			}
			Long expiry = null;
			if (map.containsKey("expiry".toUpperCase())) {
				expiry = Long.parseLong(map.get("expiry".toUpperCase()));
			}
			cacheUseVo = new CacheUseVo(cache, key, expiry, service);
		}
		return cacheUseVo;
	}

	/**
	 * 解析: ID:xxx; key:xxx;
	 */
	private CacheCleanVo parseCacheClean(String cacheUse, String service) {
		CacheCleanVo cacheCleanVo = null;
		String[] array = cacheUse.split(";");
		if (array.length > 0) {
			Map<String, String> map = new HashMap<String, String>();
			for (int i = 0; i < array.length; i++) {
				String[] item = array[i].split(":");
				map.put(item[0].trim().toUpperCase(), item[1].trim());
			}
			TangYuanCache cache = CacheComponent.getInstance().getCache(map.get("id".toUpperCase()));
			if (null == cache) {
				throw new XmlParseException("Non-existent cache: " + cacheUse);
			}
			String key = map.get("key".toUpperCase());
			if (null == key) {
				throw new XmlParseException("Missing cache.key: " + cacheUse);
			}
			cacheCleanVo = new CacheCleanVo(cache, key, service);
		}
		return cacheCleanVo;
	}

	class JavaServiceCacheVo {
		String	cacheUse;
		String	cacheClean;

		public JavaServiceCacheVo(String cacheUse, String cacheClean) {
			this.cacheUse = cacheUse;
			this.cacheClean = cacheClean;
		}
	}
}
