package org.xson.tangyuan.cache.local;

import java.util.Map;
import java.util.Properties;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheCreater;
import org.xson.tangyuan.cache.xml.vo.CacheVo;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.util.MixedResourceManager;
import org.xson.tangyuan.util.PropertyUtils;
import org.xson.tangyuan.xml.XmlParseException;

public class LocalCacheCreater implements CacheCreater {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public AbstractCache newInstance(CacheVo cacheVo) throws Throwable {
		// load
		Map<String, String> properties = null;
		String resource = cacheVo.getResource();
		if (null == resource) {
			properties = (Map) new Properties();
		} else {
			properties = (Map) MixedResourceManager.getProperties(resource, true, true);
		}

		// create
		AbstractCache localCache = new LocalCache(cacheVo.getId());

		int maxSize = PropertyUtils.getIntValue(properties, "maxSize", 1024);
		long defaultExpiry = 10; // 10秒
		if (null != cacheVo.getExpiry()) {
			defaultExpiry = cacheVo.getExpiry().longValue();
		}

		// 策略
		String strategy = PropertyUtils.getStringValue(properties, "strategy", "LRU");
		if ("LRU".equalsIgnoreCase(strategy)) {
			localCache = new LRUCache(localCache, maxSize);
		} else if ("FIFO".equalsIgnoreCase(strategy)) {
			localCache = new FIFOCache(localCache, maxSize);
		} else if ("SOFT".equalsIgnoreCase(strategy)) {
			localCache = new SoftCache(localCache, maxSize);
		} else if ("WEAK".equalsIgnoreCase(strategy)) {
			localCache = new WeakCache(localCache, maxSize);
		} else if ("TIME".equalsIgnoreCase(strategy)) {
			localCache = new ScheduledCache(localCache, defaultExpiry);
		} else {
			throw new XmlParseException(TangYuanLang.get("property.invalid", "strategy", strategy));
		}
		// 加上同步锁
		localCache = new SynchronizedCache(localCache);
		// log可选
		boolean log = PropertyUtils.getBoolValue(properties, "log", false);
		if (log) {
			localCache = new LoggingCache(localCache);
		}

		return localCache;
	}

}
