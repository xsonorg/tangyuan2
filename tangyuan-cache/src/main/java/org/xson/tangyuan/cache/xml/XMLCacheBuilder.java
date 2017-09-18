package org.xson.tangyuan.cache.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheComponent;
import org.xson.tangyuan.cache.CacheGroupVo;
import org.xson.tangyuan.cache.CacheRefVo;
import org.xson.tangyuan.cache.CacheVo;
import org.xson.tangyuan.cache.CacheVo.CacheType;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XMLCacheBuilder {

	private Log						log				= LogFactory.getLog(getClass());
	private XPathParser				xPathParser		= null;

	private CacheVo					defaultCacheVo	= null;
	private Map<String, CacheVo>	cacheVoMap		= new LinkedHashMap<String, CacheVo>();

	public XMLCacheBuilder(String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = Resources.getResourceAsStream(resource);
		this.xPathParser = new XPathParser(inputStream);
	}

	public void parse() throws Throwable {
		configurationElement(xPathParser.evalNode("/cache-component"));
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildConfigNodes(context.evalNodes("config-property"));
		// 解析缓存定义
		buildCache(context.evalNodes("cache"));
		// 解析缓存组定义
		buildCacheGroup(context.evalNodes("cacheGroup"));

		Map<String, AbstractCache> cacheMap = new HashMap<String, AbstractCache>();
		// 启动缓存
		for (Map.Entry<String, CacheVo> entry : cacheVoMap.entrySet()) {
			CacheVo cacheVo = entry.getValue();
			cacheVo.start();
			cacheMap.put(cacheVo.getId(), cacheVo.getCache());
			log.info("cache start: " + entry.getValue().getId());
		}

		AbstractCache defaultCache = (null == defaultCacheVo) ? null : defaultCacheVo.getCache();

		CacheComponent.getInstance().setDefaultCache(defaultCache);
		CacheComponent.getInstance().setCacheMap(cacheMap);

		cacheVoMap.clear();
		cacheVoMap = null;
		defaultCacheVo = null;
	}

	private void buildConfigNodes(List<XmlNodeWrapper> contexts) throws Exception {
		// <config-property name="A" value="B" />
		Map<String, String> configMap = new HashMap<String, String>();
		for (XmlNodeWrapper context : contexts) {
			String name = StringUtils.trim(context.getStringAttribute("name"));
			String value = StringUtils.trim(context.getStringAttribute("value"));
			if (null == name || null == value) {
				throw new RuntimeException("<config-property> missing names or value");
			}
			configMap.put(name.toUpperCase(), value);
		}
		if (configMap.size() > 0) {
			CacheComponent.getInstance().config(configMap);
		}
	}

	private void buildCache(List<XmlNodeWrapper> contexts) throws Exception {
		int size = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));// xml v
			if (cacheVoMap.containsKey(id)) {
				throw new XmlParseException("duplicate cache:" + id);
			}
			String _type = StringUtils.trim(xNode.getStringAttribute("type"));
			CacheType type = null;
			if (null != _type) {
				type = getCacheType(_type);
			}

			String className = StringUtils.trim(xNode.getStringAttribute("class"));
			AbstractCache handler = null;
			if (null != className) {
				Class<?> handlerClass = ClassUtils.forName(className);
				if (!AbstractCache.class.isAssignableFrom(handlerClass)) {
					throw new XmlParseException("cache class not implement the ICache interface: " + className);
				}
				handler = (AbstractCache) handlerClass.newInstance();
			}

			String _defaultCache = StringUtils.trim(xNode.getStringAttribute("default"));
			boolean defaultCache = false;
			if (null != _defaultCache) {
				defaultCache = Boolean.parseBoolean(_defaultCache);
				if (defaultCache && null != this.defaultCacheVo) {
					throw new XmlParseException("The default cache already exists: " + id);
				}
			}

			String resource = StringUtils.trim(xNode.getStringAttribute("resource"));

			Map<String, String> propertiesMap = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				propertiesMap.put(StringUtils.trim(propertyNode.getStringAttribute("name")),
						StringUtils.trim(propertyNode.getStringAttribute("value")));
			}

			if (null == handler && null == type) {
				throw new XmlParseException("cache type and cache class can not be empty.");
			}

			// String jndiName = StringUtils.trim(xNode.getStringAttribute("jndiName"));
			String sharedUse = StringUtils.trim(xNode.getStringAttribute("sharedUse"));

			CacheVo cVo = new CacheVo(id, type, handler, resource, propertiesMap, sharedUse, TangYuanContainer.getInstance().getSystemName());
			cacheVoMap.put(id, cVo);

			log.info("add cache: " + id);

			if (defaultCache) {
				this.defaultCacheVo = cVo;
			}
		}
	}

	private void buildCacheGroup(List<XmlNodeWrapper> contexts) {
		int size = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));// xml v
			if (cacheVoMap.containsKey(id)) {
				throw new XmlParseException("duplicate cache: " + id);
			}
			String _defaultCache = StringUtils.trim(xNode.getStringAttribute("default"));
			boolean defaultCache = false;
			if (null != _defaultCache) {
				defaultCache = Boolean.parseBoolean(_defaultCache);
				if (defaultCache && null != this.defaultCacheVo) {
					throw new XmlParseException("The default cache already exists: " + id);
				}
			}

			List<CacheRefVo> cacheRefList = new ArrayList<CacheRefVo>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("cache");
			for (XmlNodeWrapper propertyNode : properties) {
				String _ref = StringUtils.trim(propertyNode.getStringAttribute("ref"));
				String _include = StringUtils.trim(propertyNode.getStringAttribute("include"));
				String _exclude = StringUtils.trim(propertyNode.getStringAttribute("exclude"));
				CacheVo cacheVo = cacheVoMap.get(_ref);
				if (null == cacheVo || cacheVo.isGroup()) {
					throw new XmlParseException("The referenced cache does not exist: " + _ref);
				}
				String[] include = null;
				if (null != _include && _include.trim().length() > 0) {
					include = _include.split(",");
				}
				String[] exclude = null;
				if (null != _exclude && _exclude.trim().length() > 0) {
					exclude = _exclude.split(",");
				}
				CacheRefVo refVo = new CacheRefVo(cacheVo, include, exclude);
				cacheRefList.add(refVo);
			}

			if (0 == cacheRefList.size()) {
				throw new XmlParseException("The referenced cache can not be empty.");
			}

			String jndiName = StringUtils.trim(xNode.getStringAttribute("jndiName"));

			CacheVo cVo = new CacheGroupVo(id, cacheRefList, jndiName);

			cacheVoMap.put(id, cVo);
			log.info("add cache group: " + id);

			if (defaultCache) {
				this.defaultCacheVo = cVo;
			}
		}
	}

	private CacheType getCacheType(String str) {
		if ("LOCAL".equalsIgnoreCase(str)) {
			return CacheType.LOCAL;
		} else if ("EHCACHE".equalsIgnoreCase(str)) {
			return CacheType.EHCACHE;
		} else if ("MEMCACHE".equalsIgnoreCase(str)) {
			return CacheType.MEMCACHE;
		} else if ("REDIS".equalsIgnoreCase(str)) {
			return CacheType.REDIS;
		} else if ("SHARE".equalsIgnoreCase(str)) {
			return CacheType.SHARE;
		} else {
			return null;
		}
	}
}
