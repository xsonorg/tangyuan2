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
		String              resource   = cacheVo.getResource();
		if (null == resource) {
			properties = (Map) new Properties();
		} else {
			properties = (Map) MixedResourceManager.getProperties(resource, true, true);
		}

		// create
		AbstractCache localCache    = new LocalCache(cacheVo.getId());

		int           maxSize       = PropertyUtils.getIntValue(properties, "maxSize", 1024);
		long          defaultExpiry = 10; // 10秒
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

	//////////////////////////////////////////////////////////////////////////////

	//	Map<String, String> properties = null;
	//	String              resource   = cacheVo.getResource();
	//	try {
	//		if (null == resource) {
	//			properties = cacheVo.getProperties();
	//			//				PlaceholderResourceSupport.processMap(properties, cacheVo.getPlaceholderMap());
	//			PlaceholderResourceSupport.processMap(properties);
	//		} else {
	//			//InputStream inputStream = ResourceManager.getInputStream(resource, cacheVo.getPlaceholderMap());
	//			InputStream inputStream = MixedResourceManager.getInputStream(resource);
	//			Properties  p           = new Properties();
	//			p.load(inputStream);
	//			inputStream.close();
	//			properties = (Map) p;
	//		}
	//		properties = (Map) MixedResourceManager.getProperties(resource, true, true);
	//	} catch (Throwable e) {
	//		throw new CacheException(e);
	//	}

	//		int    maxSize  = 1024;
	//		String _maxSize = properties.get("maxSize");
	//		if (null != _maxSize) {
	//			maxSize = Integer.parseInt(_maxSize);
	//		}
	// String            strategy     = properties.get("strategy");
	// String            strategy     = properties.get("strategy");
	//	//		CacheStrategyType strategyType = null;
	//	String strategy = PropertyUtils.getStringValue(properties, "strategy", "LRU");
	//	if ("LRU".equalsIgnoreCase(strategy)) {
	//		//			strategyType = CacheStrategyType.LRU;
	//		localCache = new LRUCache(localCache, maxSize);
	//	} else if ("FIFO".equalsIgnoreCase(strategy)) {
	//		//			strategyType = CacheStrategyType.FIFO;
	//		localCache = new FIFOCache(localCache, maxSize);
	//	} else if ("SOFT".equalsIgnoreCase(strategy)) {
	//		//			strategyType = CacheStrategyType.SOFT;
	//		localCache = new SoftCache(localCache, maxSize);
	//	} else if ("WEAK".equalsIgnoreCase(strategy)) {
	//		//			strategyType = CacheStrategyType.WEAK;
	//		localCache = new WeakCache(localCache, maxSize);
	//	} else if ("TIME".equalsIgnoreCase(strategy)) {
	//		//			strategyType = CacheStrategyType.TIME;
	//		localCache = new ScheduledCache(localCache, defaultExpiry);
	//	} else {
	//		throw new XmlParseException(TangYuanLang.get("property.invalid", "strategy", strategy));
	//	}

	// 根据设置
	//		if (CacheStrategyType.LRU == strategyType) {
	//			localCache = new LRUCache(localCache, maxSize);
	//		} else if (CacheStrategyType.FIFO == strategyType) {
	//			localCache = new FIFOCache(localCache, maxSize);
	//		} else if (CacheStrategyType.SOFT == strategyType) {
	//			localCache = new SoftCache(localCache, maxSize);
	//		} else if (CacheStrategyType.WEAK == strategyType) {
	//			localCache = new WeakCache(localCache, maxSize);
	//		} else if (CacheStrategyType.TIME == strategyType) {
	//			localCache = new ScheduledCache(localCache, defaultExpiry);
	//		}
	//		boolean log  = false;
	//		String  _log = properties.get("log");
	//		if (null != _log) {
	//			log = Boolean.parseBoolean(_log);
	//		}
}
