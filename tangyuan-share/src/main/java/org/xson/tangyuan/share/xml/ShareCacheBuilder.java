package org.xson.tangyuan.share.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheGroupVo;
import org.xson.tangyuan.cache.CacheRefVo;
import org.xson.tangyuan.cache.CacheVo;
import org.xson.tangyuan.cache.CacheVo.CacheType;
import org.xson.tangyuan.cache.ShareCacheContainer;
import org.xson.tangyuan.share.ShareComponent;
import org.xson.tangyuan.share.util.ClassUtils;
import org.xson.tangyuan.share.util.StringUtils;

public class ShareCacheBuilder {

	private Log						log			= LogFactory.getLog(getClass());
	private XPathParser				xPathParser	= null;
	private String					basePath	= null;
	private Map<String, CacheVo>	cacheVoMap	= new LinkedHashMap<String, CacheVo>();

	public void parse(String basePath, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = new FileInputStream(new File(basePath, resource));
		this.xPathParser = new XPathParser(inputStream);
		this.basePath = basePath;
		inputStream.close();
		configurationElement(xPathParser.evalNode("/cache-component"));
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildCache(context.evalNodes("cache"));
		buildCacheGroup(context.evalNodes("cacheGroup"));
		Map<String, AbstractCache> cacheMap = new HashMap<String, AbstractCache>();
		// 启动缓存
		for (Map.Entry<String, CacheVo> entry : cacheVoMap.entrySet()) {
			CacheVo cacheVo = entry.getValue();
			cacheVo.start();
			cacheMap.put(cacheVo.getId(), cacheVo.getCache());
			log.info("cache start: " + entry.getValue().getId());
		}

		cacheVoMap.clear();
		cacheVoMap = null;
		ShareCacheContainer.getInstance().setCacheMap(cacheMap);
	}

	private void buildCache(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));// xml v
			if (cacheVoMap.containsKey(id)) {
				throw new XmlParseException("duplicate cache:" + id);
			}
			String _type = StringUtils.trim(xNode.getStringAttribute("type"));

			// System.out.println("CacheType" + this.getClass().getClassLoader().getClass().getName());
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

			String resource = StringUtils.trim(xNode.getStringAttribute("resource"));
			resource = getResourcePath(resource);

			Map<String, String> propertiesMap = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				propertiesMap.put(StringUtils.trim(propertyNode.getStringAttribute("name")),
						StringUtils.trim(propertyNode.getStringAttribute("value")));
			}

			if (null == handler && null == type) {
				throw new XmlParseException("cache type and cache class can not be empty.");
			}

			// String jndiName = null;
			String sharedUse = null;

			CacheVo cVo = new CacheVo(id, type, handler, resource, propertiesMap, sharedUse, ShareComponent.getInstance().getSystemName());
			cacheVoMap.put(id, cVo);

			log.info("add cache: " + id);

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
		} else if ("REDIS".equalsIgnoreCase(str)) {
			return CacheType.SHARE;
		} else {
			return null;
		}
	}

	private String getResourcePath(String resource) throws IOException {
		if (null == resource) {
			return resource;
		}
		if (resource.startsWith("/")) {
			return resource;
		}
		if (resource.indexOf(":/") > -1) {
			return resource;
		}
		if (resource.indexOf(":\\") > -1) {
			return resource;
		}
		return new File(this.basePath, resource).getCanonicalPath();
	}

}
