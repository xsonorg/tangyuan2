package org.xson.tangyuan.cache.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheComponent;
import org.xson.tangyuan.cache.CacheSerializer;
import org.xson.tangyuan.cache.xml.vo.CacheGroupVo;
import org.xson.tangyuan.cache.xml.vo.CacheRefVo;
import org.xson.tangyuan.cache.xml.vo.CacheVo;
import org.xson.tangyuan.cache.xml.vo.CacheVo.CacheType;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlCacheComponentBuilder extends DefaultXmlComponentBuilder {

	private XmlCacheContext              componentContext = null;
	private CacheVo                      defaultCacheVo   = null;
	private Map<String, CacheVo>         cacheVoMap       = new LinkedHashMap<String, CacheVo>();
	private Map<String, CacheSerializer> serializerMap    = new HashMap<String, CacheSerializer>();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		//		this.globalContext = (XmlGlobalContext) xmlContext;
		this.componentContext = (XmlCacheContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "cache-component", true);
		this.configurationElement();
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();

		this.componentContext = null;

		this.cacheVoMap.clear();
		this.cacheVoMap = null;
		this.defaultCacheVo = null;
		this.serializerMap = null;
	}

	private void configurationElement() throws Throwable {
		// 解析配置项目
		buildConfigNode(this.root.evalNodes("config-property"), CacheComponent.getInstance());
		// 解析序列化器
		buildSerializerNode(this.root.evalNodes("serializer"));
		// 解析缓存定义
		buildCache(this.root.evalNodes("cache"));
		// 解析缓存组定义
		buildCacheGroup(this.root.evalNodes("cacheGroup"));

		Map<String, AbstractCache> cacheMap = new HashMap<String, AbstractCache>();
		// 启动缓存
		for (Map.Entry<String, CacheVo> entry : cacheVoMap.entrySet()) {
			CacheVo cacheVo = entry.getValue();
			cacheVo.start();
			cacheMap.put(cacheVo.getId(), cacheVo.getCache());
			//			log.info("cache start: " + entry.getValue().getId());
			log.info(TangYuanLang.get("instance.start.id"), "cache", entry.getValue().getId());
		}

		AbstractCache defaultCache = (null == defaultCacheVo) ? null : defaultCacheVo.getCache();
		CacheComponent.getInstance().setDefaultCache(defaultCache);
		CacheComponent.getInstance().setCacheMap(cacheMap);
	}

	private void buildSerializerNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "serializer";
		for (XmlNodeWrapper xNode : contexts) {
			String id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String className = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));
			if (this.serializerMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			CacheSerializer serializer = getInstanceForName(className, CacheSerializer.class, lang("xml.class.impl.interface", className, CacheSerializer.class.getName()));
			this.serializerMap.put(id, serializer);
			log.info(lang("add.tag.class"), tagName, className);
		}
	}

	private void buildCache(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "cache";
		int    size    = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode        = contexts.get(i);

			String         id           = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String         _type        = getStringFromAttr(xNode, "type");
			String         className    = getStringFromAttr(xNode, "class");
			boolean        defaultCache = getBoolFromAttr(xNode, "isDefault", false);
			String         resource     = getStringFromAttr(xNode, "resource");
			String         serializer   = getStringFromAttr(xNode, "serializer");
			Long           expiry       = getLongWrapperFromAttr(xNode, "defaultExpiry");

			if (this.cacheVoMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			CacheType     type      = getCacheType(_type);

			AbstractCache cacheImpl = null;
			if (null != className) {
				cacheImpl = getInstanceForName(className, AbstractCache.class, lang("xml.class.impl.superClass", className, AbstractCache.class.getName()));
			}
			if (null == cacheImpl && null == type) {
				throw new XmlParseException(lang("xml.tag.attribute.empty", "type|class", tagName, this.resource));
			}
			if (defaultCache && null != this.defaultCacheVo) {
				throw new XmlParseException(lang("xml.tag.mostone.default", tagName));
			}
			CacheSerializer cs = null;
			if (null != serializer) {
				cs = this.serializerMap.get(serializer);
				throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", serializer, "serializer", tagName, this.resource));
			}

			CacheVo cVo = new CacheVo(id, type, cacheImpl, resource, null, expiry, cs);
			cacheVoMap.put(id, cVo);
			if (defaultCache) {
				this.defaultCacheVo = cVo;
			}
			log.info(lang("add.tag", tagName, id));
		}
	}

	private void buildCacheGroup(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "cacheGroup";
		int    size    = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode        = contexts.get(i);

			String         id           = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			boolean        defaultCache = getBoolFromAttr(xNode, "isDefault", false);

			if (this.cacheVoMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			if (defaultCache && null != this.defaultCacheVo) {
				throw new XmlParseException(lang("xml.tag.mostone.default", tagName));
			}

			List<CacheRefVo>     cacheRefList = new ArrayList<CacheRefVo>();
			List<XmlNodeWrapper> properties   = xNode.evalNodes("cache");
			for (XmlNodeWrapper propertyNode : properties) {
				String   _ref    = getStringFromAttr(propertyNode, "ref", lang("xml.tag.attribute.empty", "ref", tagName + ".cache", this.resource));
				String[] include = getStringArrayFromAttr(xNode, "include");
				String[] exclude = getStringArrayFromAttr(xNode, "exclude");
				CacheVo  cacheVo = cacheVoMap.get(_ref);
				if (null == cacheVo || cacheVo.isGroup()) {
					throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", _ref, "ref", tagName, this.resource));
				}
				CacheRefVo refVo = new CacheRefVo(cacheVo, include, exclude);
				cacheRefList.add(refVo);
			}
			if (0 == cacheRefList.size()) {
				throw new XmlParseException(lang("xml.tag.invalid", tagName, this.resource));
			}

			CacheVo cVo = new CacheGroupVo(id, cacheRefList);
			cacheVoMap.put(id, cVo);
			if (defaultCache) {
				this.defaultCacheVo = cVo;
			}
			log.info(lang("add.tag", tagName, id));
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
		}
		return null;
	}

	///////////////////////////////////////////////////////////////////////////////////////////

	//			CacheType type = null;
	//			if (null != _type) {
	//				type = getCacheType(_type);
	//			}
	//cacheImpl = getInstanceForName(className, AbstractCache.class, lang("xml.class.impl.interface", className, AbstractCache.class.getName()));
	//			Map<String, String>  propertiesMap = new HashMap<String, String>();
	//			List<XmlNodeWrapper> properties    = xNode.evalNodes("property");
	//			for (XmlNodeWrapper propertyNode : properties) {
	//				// propertiesMap.put(StringUtils.trim(propertyNode.getStringAttribute("name")), StringUtils.trim(propertyNode.getStringAttribute("value")));
	//				String name  = getStringFromAttr(propertyNode, "name", lang("xml.tag.attribute.empty", "name", tagName + ".property", this.resource));
	//				String value = getStringFromAttr(propertyNode, "value", lang("xml.tag.attribute.empty", "value", tagName + ".property", this.resource));
	//				propertiesMap.put(name, value);
	//			}
	//			CacheVo cVo = new CacheVo(id, type, cacheImpl, resource, propertiesMap, expiry, cs);

	//		else if ("SHARE".equalsIgnoreCase(str)) {
	//			return CacheType.SHARE;
	//		} 
	//			String         _type        = getStringFromAttr(xNode, "type");
	//			String         className    = getStringFromAttr(xNode, "class");
	//			String         resource     = getStringFromAttr(xNode, "resource");
	//			String         serializer   = getStringFromAttr(xNode, "serializer");
	//			Long           expiry       = getLongWrapperFromAttr(xNode, "defaultExpiry");
	//			String         id           = StringUtils.trim(xNode.getStringAttribute("id"));// xml v
	//			if (cacheVoMap.containsKey(id)) {
	//				throw new XmlParseException("duplicate cache: " + id);
	//			}
	//			String  _defaultCache = StringUtils.trim(xNode.getStringAttribute("isDefault"));
	//			boolean defaultCache  = false;
	//			if (null != _defaultCache) {
	//				defaultCache = Boolean.parseBoolean(_defaultCache);
	//				if (defaultCache && null != this.defaultCacheVo) {
	//					throw new XmlParseException("The default cache already exists: " + id);
	//				}
	//			}
	//	public XmlCacheComponentBuilder(String resource) throws Throwable {
	//		log.info("*** Start parsing: " + resource);
	//		//		InputStream inputStream = Resources.getResourceAsStream(resource);
	//		//		inputStream = PlaceholderResourceSupport.processInputStream(inputStream,
	//		//				TangYuanContainer.getInstance().getXmlGlobalContext().getPlaceholderMap());
	//		InputStream inputStream = ResourceManager.getInputStream(resource, true);
	//		this.xPathParser = new XPathParser(inputStream);
	//		inputStream.close();
	//	}

	//	public void parse() throws Throwable {
	//		configurationElement(xPathParser.evalNode("/cache-component"));
	//	}

	//	private void configurationElement(XmlNodeWrapper context) throws Throwable {
	//
	//		buildConfigNodes(context.evalNodes("config-property"));
	//		buildSerializerNodes(context.evalNodes("serializer"));
	//		// 解析缓存定义
	//		buildCache(context.evalNodes("cache"));
	//		// 解析缓存组定义
	//		buildCacheGroup(context.evalNodes("cacheGroup"));
	//
	//		Map<String, AbstractCache> cacheMap = new HashMap<String, AbstractCache>();
	//		// 启动缓存
	//		for (Map.Entry<String, CacheVo> entry : cacheVoMap.entrySet()) {
	//			CacheVo cacheVo = entry.getValue();
	//			cacheVo.start();
	//			cacheMap.put(cacheVo.getId(), cacheVo.getCache());
	//			log.info("cache start: " + entry.getValue().getId());
	//		}
	//
	//		AbstractCache defaultCache = (null == defaultCacheVo) ? null : defaultCacheVo.getCache();
	//
	//		CacheComponent.getInstance().setDefaultCache(defaultCache);
	//		CacheComponent.getInstance().setCacheMap(cacheMap);
	//
	//		cacheVoMap.clear();
	//		cacheVoMap = null;
	//		defaultCacheVo = null;
	//	}

	//	private void buildConfigNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		// <config-property name="A" value="B" />
	//		Map<String, String> configMap = new HashMap<String, String>();
	//		for (XmlNodeWrapper context : contexts) {
	//			String name  = StringUtils.trim(context.getStringAttribute("name"));
	//			String value = StringUtils.trim(context.getStringAttribute("value"));
	//			if (null == name || null == value) {
	//				throw new XmlParseException("<config-property> missing name or value");
	//			}
	//			configMap.put(name.toUpperCase(), value);
	//		}
	//		if (configMap.size() > 0) {
	//			// TangYuanContainer.getInstance().getXmlGlobalContext().getPlaceholder().processMap(configMap);
	//			CacheComponent.getInstance().config(configMap);
	//		}
	//	}

	//	String         id    = StringUtils.trim(xNode.getStringAttribute("id"));// xml v
	//			if (cacheVoMap.containsKey(id)) {
	//				throw new XmlParseException("duplicate cache:" + id);
	//			}
	//			String    _type = StringUtils.trim(xNode.getStringAttribute("type"));
	//			String        className = StringUtils.trim(xNode.getStringAttribute("class"));
	//			AbstractCache cacheImpl = null;
	//			if (null != className) {
	//				Class<?> handlerClass = ClassUtils.forName(className);
	//				if (!AbstractCache.class.isAssignableFrom(handlerClass)) {
	//					throw new XmlParseException("cache class not implement the ICache interface: " + className);
	//				}
	//				// cacheImpl = (AbstractCache) handlerClass.newInstance();
	//				cacheImpl = (AbstractCache) TangYuanUtil.newInstance(handlerClass);
	//			}

	//			String  _defaultCache = StringUtils.trim(xNode.getStringAttribute("isDefault"));
	//			boolean defaultCache  = false;
	//			if (null != _defaultCache) {
	//				defaultCache = Boolean.parseBoolean(_defaultCache);
	//				if (defaultCache && null != this.defaultCacheVo) {
	//					throw new XmlParseException("The default cache already exists: " + id);
	//				}
	//			}
	//			String               resource      = StringUtils.trim(xNode.getStringAttribute("resource"));

	// TangYuanContainer.getInstance().getXmlGlobalContext().getPlaceholder().processMap(propertiesMap);
	//			if (null == cacheImpl && null == type) {
	//				throw new XmlParseException("cache type and cache class can not be empty.");
	//			}

	//			String sharedUse     = StringUtils.trim(xNode.getStringAttribute("sharedUse"));

	//			Long   expiry        = null;
	//			String defaultExpiry = StringUtils.trim(xNode.getStringAttribute("defaultExpiry"));
	//			if (null != defaultExpiry) {
	//				expiry = Long.parseLong(defaultExpiry);
	//			}

	//			CacheSerializer cs = null;
	//			//			String          serializer = StringUtils.trim(xNode.getStringAttribute("serializer"));
	//			if (null != serializer) {
	//				cs = this.serializerMap.get(serializer);
	//				TangYuanAssert.objectEmpty(cs, TangYuanUtil.format("The referenced cache serializer[{}] does not exist", serializer));
	//			}

	//	private void buildCacheGroup(List<XmlNodeWrapper> contexts) {
	//		int size = contexts.size();
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode = contexts.get(i);
	//			String         id    = StringUtils.trim(xNode.getStringAttribute("id"));// xml v
	//			if (cacheVoMap.containsKey(id)) {
	//				throw new XmlParseException("duplicate cache: " + id);
	//			}
	//			String  _defaultCache = StringUtils.trim(xNode.getStringAttribute("isDefault"));
	//			boolean defaultCache  = false;
	//			if (null != _defaultCache) {
	//				defaultCache = Boolean.parseBoolean(_defaultCache);
	//				if (defaultCache && null != this.defaultCacheVo) {
	//					throw new XmlParseException("The default cache already exists: " + id);
	//				}
	//			}
	//
	//			List<CacheRefVo>     cacheRefList = new ArrayList<CacheRefVo>();
	//			List<XmlNodeWrapper> properties   = xNode.evalNodes("cache");
	//			for (XmlNodeWrapper propertyNode : properties) {
	//				String  _ref     = StringUtils.trim(propertyNode.getStringAttribute("ref"));
	//				String  _include = StringUtils.trim(propertyNode.getStringAttribute("include"));
	//				String  _exclude = StringUtils.trim(propertyNode.getStringAttribute("exclude"));
	//				CacheVo cacheVo  = cacheVoMap.get(_ref);
	//				if (null == cacheVo || cacheVo.isGroup()) {
	//					throw new XmlParseException("The referenced cache does not exist: " + _ref);
	//				}
	//				String[] include = null;
	//				if (null != _include && _include.trim().length() > 0) {
	//					include = _include.split(",");
	//				}
	//				String[] exclude = null;
	//				if (null != _exclude && _exclude.trim().length() > 0) {
	//					exclude = _exclude.split(",");
	//				}
	//				CacheRefVo refVo = new CacheRefVo(cacheVo, include, exclude);
	//				cacheRefList.add(refVo);
	//			}
	//
	//			if (0 == cacheRefList.size()) {
	//				throw new XmlParseException("The referenced cache can not be empty.");
	//			}
	//
	//			//			fix bug
	//			//			String jndiName = StringUtils.trim(xNode.getStringAttribute("jndiName"));
	//			//			CacheVo cVo = new CacheGroupVo(id, cacheRefList, jndiName);
	//
	//			CacheVo cVo = new CacheGroupVo(id, cacheRefList, TangYuanContainer.getInstance().getSystemName());
	//
	//			cacheVoMap.put(id, cVo);
	//			log.info("add cache group: " + id);
	//
	//			if (defaultCache) {
	//				this.defaultCacheVo = cVo;
	//			}
	//		}
	//	}

}
