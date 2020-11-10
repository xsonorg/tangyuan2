package org.xson.tangyuan.cache;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.Version;
import org.xson.tangyuan.cache.apply.DefaultMD5CacheKeyBuilder;
import org.xson.tangyuan.cache.xml.XmlCacheComponentBuilder;
import org.xson.tangyuan.cache.xml.XmlCacheContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.util.StringUtils;

public class CacheComponent implements TangYuanComponent {

	private static CacheComponent      instance              = new CacheComponent();
	private Log                        log                   = LogFactory.getLog(getClass());
	private Map<String, AbstractCache> cacheMap              = new HashMap<String, AbstractCache>();
	private AbstractCache              defaultCache          = null;

	private String                     defaultCacheKeyPrefix = null;
	private CacheKeyBuilder            cacheKeyBuilder       = new DefaultMD5CacheKeyBuilder();

	static {
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "cache"));
	}

	private CacheComponent() {
	}

	public static CacheComponent getInstance() {
		return instance;
	}

	public TangYuanCache getCache(String cacheId) {
		if (null == cacheId) {
			return defaultCache;
		}
		return cacheMap.get(cacheId);
	}

	public void setCacheMap(Map<String, AbstractCache> cacheMap) {
		this.cacheMap = cacheMap;
	}

	public void setDefaultCache(AbstractCache defaultCache) {
		this.defaultCache = defaultCache;
	}

	public String getDefaultCacheKeyPrefix() {
		return defaultCacheKeyPrefix;
	}

	public CacheKeyBuilder getCacheKeyBuilder() {
		return cacheKeyBuilder;
	}

	@Override
	public void config(Map<String, String> properties) {
		this.defaultCacheKeyPrefix = StringUtils.trimEmpty(properties.get("defaultCacheKeyPrefix".toUpperCase()));
		log.info(TangYuanLang.get("config.property.load"), "cache-component");
	}

	@Override
	public void start(String resource) throws Throwable {
		log.info(TangYuanLang.get("component.dividing.line"));
		log.info(TangYuanLang.get("component.starting"), "cache", Version.getVersion());

		XmlCacheContext componentContext = new XmlCacheContext();
		componentContext.setXmlContext(TangYuanContainer.getInstance().getXmlGlobalContext());

		XmlCacheComponentBuilder builder = new XmlCacheComponentBuilder();
		builder.parse(componentContext, resource);
		componentContext.clean();

		log.info(TangYuanLang.get("component.starting.successfully"), "cache");
	}

	@Override
	public void stop(long waitingTime, boolean asyn) {
		log.info(TangYuanLang.get("component.stopping"), "cache");
		for (Map.Entry<String, AbstractCache> entry : cacheMap.entrySet()) {
			AbstractCache cache = entry.getValue();
			cache.stop();
			log.info(TangYuanLang.get("instance.stop.id"), "cache", entry.getValue().getId());
		}
		log.info(TangYuanLang.get("component.stopping.successfully"), "cache");
	}

}
