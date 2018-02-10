package org.xson.tangyuan.cache;

import java.util.HashMap;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.xml.XMLCacheBuilder;

public class CacheComponent implements TangYuanComponent {

	private static CacheComponent		instance		= new CacheComponent();
	private Log							log				= LogFactory.getLog(getClass());
	private AbstractCache				defaultCache	= null;
	private Map<String, AbstractCache>	cacheMap		= new HashMap<String, AbstractCache>();

	// TODO 需要有一个默认的过期时间

	static {
		// cache 20 60
		// TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "cache", 20, 60));
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "cache"));
	}

	private CacheComponent() {
	}

	public static CacheComponent getInstance() {
		return instance;
	}

	@Override
	public void config(Map<String, String> properties) {
		// 设置配置文件
	}

	@Override
	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("cache component starting...");
		XMLCacheBuilder builder = new XMLCacheBuilder(resource);
		builder.parse();
		// TODO register contoner
		log.info("cache component start successfully.");
	}

	@Override
	public void stop(boolean wait) {
		log.info("cache component stopping...");
		for (Map.Entry<String, AbstractCache> entry : cacheMap.entrySet()) {
			AbstractCache cache = entry.getValue();
			cache.stop(TangYuanContainer.getInstance().getSystemName());
			log.info("cache stop: " + entry.getValue().getId());
		}
		log.info("cache component stop successfully.");
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
}
