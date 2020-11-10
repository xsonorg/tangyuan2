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

	private XmlCacheContext					componentContext	= null;
	private CacheVo							defaultCacheVo		= null;
	private Map<String, CacheVo>			cacheVoMap			= new LinkedHashMap<String, CacheVo>();
	private Map<String, CacheSerializer>	serializerMap		= new HashMap<String, CacheSerializer>();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
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
			// log.info("cache start: " + entry.getValue().getId());
			log.info(TangYuanLang.get("instance.start.id"), "cache", entry.getValue().getId());
		}

		AbstractCache defaultCache = (null == defaultCacheVo) ? null : defaultCacheVo.getCache();
		CacheComponent.getInstance().setDefaultCache(defaultCache);
		CacheComponent.getInstance().setCacheMap(cacheMap);
	}

	private void buildSerializerNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "serializer";
		for (XmlNodeWrapper xNode : contexts) {
			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String className = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));
			if (this.serializerMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			CacheSerializer serializer = getInstanceForName(className, CacheSerializer.class,
					lang("xml.class.impl.interface", className, CacheSerializer.class.getName()));
			this.serializerMap.put(id, serializer);
			log.info(lang("add.tag.class"), tagName, className);
		}
	}

	private void buildCache(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "cache";
		int size = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String _type = getStringFromAttr(xNode, "type");
			String className = getStringFromAttr(xNode, "class");
			boolean defaultCache = getBoolFromAttr(xNode, "isDefault", false);
			String resource = getStringFromAttr(xNode, "resource");
			String serializer = getStringFromAttr(xNode, "serializer");
			Long expiry = getLongWrapperFromAttr(xNode, "defaultExpiry");

			if (this.cacheVoMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			CacheType type = getCacheType(_type);

			AbstractCache cacheImpl = null;
			if (null != className) {
				cacheImpl = getInstanceForName(className, AbstractCache.class,
						lang("xml.class.impl.superClass", className, AbstractCache.class.getName()));
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
		int size = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			boolean defaultCache = getBoolFromAttr(xNode, "isDefault", false);

			if (this.cacheVoMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			if (defaultCache && null != this.defaultCacheVo) {
				throw new XmlParseException(lang("xml.tag.mostone.default", tagName));
			}

			List<CacheRefVo> cacheRefList = new ArrayList<CacheRefVo>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("cache");
			for (XmlNodeWrapper propertyNode : properties) {
				String _ref = getStringFromAttr(propertyNode, "ref", lang("xml.tag.attribute.empty", "ref", tagName + ".cache", this.resource));
				String[] include = getStringArrayFromAttr(xNode, "include");
				String[] exclude = getStringArrayFromAttr(xNode, "exclude");
				CacheVo cacheVo = cacheVoMap.get(_ref);
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
		if (CacheType.LOCAL.toString().equalsIgnoreCase(str)) {
			return CacheType.LOCAL;
		} else if (CacheType.EHCACHE.toString().equalsIgnoreCase(str)) {
			return CacheType.EHCACHE;
		} else if (CacheType.MEMCACHE.toString().equalsIgnoreCase(str)) {
			return CacheType.MEMCACHE;
		} else if (CacheType.REDIS.toString().equalsIgnoreCase(str)) {
			return CacheType.REDIS;
		}
		return null;
	}

}
