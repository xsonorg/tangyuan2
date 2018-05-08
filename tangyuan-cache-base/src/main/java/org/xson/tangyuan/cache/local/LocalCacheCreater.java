package org.xson.tangyuan.cache.local;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheCreater;
import org.xson.tangyuan.cache.CacheException;
import org.xson.tangyuan.cache.CacheVo;
import org.xson.tangyuan.cache.local.LocalCache.CacheStrategyType;
import org.xson.tangyuan.cache.util.PlaceholderResourceSupport;
import org.xson.tangyuan.cache.util.ResourceManager;

public class LocalCacheCreater implements CacheCreater {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public AbstractCache newInstance(CacheVo cacheVo) {

		//		Map<String, String> properties = cacheVo.getProperties();

		Map<String, String> properties = null;
		String resource = cacheVo.getResource();
		try {
			if (null == resource) {
				properties = cacheVo.getProperties();
				PlaceholderResourceSupport.processMap(properties, cacheVo.getPlaceholderMap());
			} else {
				InputStream inputStream = ResourceManager.getInputStream(resource, cacheVo.getPlaceholderMap());
				Properties p = new Properties();
				p.load(inputStream);
				inputStream.close();
				properties = (Map) p;
			}
		} catch (Throwable e) {
			throw new CacheException(e);
		}

		AbstractCache localCache = new LocalCache(cacheVo.getId());

		CacheStrategyType strategyType = CacheStrategyType.LRU;
		String strategy = properties.get("strategy");
		if (null != strategy) {
			if ("FIFO".equalsIgnoreCase(strategy)) {
				strategyType = CacheStrategyType.FIFO;
			} else if ("SOFT".equalsIgnoreCase(strategy)) {
				strategyType = CacheStrategyType.SOFT;
			} else if ("WEAK".equalsIgnoreCase(strategy)) {
				strategyType = CacheStrategyType.WEAK;
			} else if ("TIME".equalsIgnoreCase(strategy)) {
				strategyType = CacheStrategyType.TIME;
			}
		}

		int maxSize = 1024;
		String _maxSize = properties.get("maxSize");
		if (null != _maxSize) {
			maxSize = Integer.parseInt(_maxSize);
		}

		// String _survivalTime = properties.get("survivalTime");
		// if (null != _survivalTime) {
		// survivalTime = Integer.parseInt(_survivalTime);
		// }

		long defaultExpiry = 10; // 10秒
		if (null != cacheVo.getExpiry()) {
			defaultExpiry = cacheVo.getExpiry();
		}

		// 根据设置
		if (CacheStrategyType.LRU == strategyType) {
			localCache = new LRUCache(localCache, maxSize);
		} else if (CacheStrategyType.FIFO == strategyType) {
			localCache = new FIFOCache(localCache, maxSize);
		} else if (CacheStrategyType.SOFT == strategyType) {
			localCache = new SoftCache(localCache, maxSize);
		} else if (CacheStrategyType.WEAK == strategyType) {
			localCache = new WeakCache(localCache, maxSize);
		} else if (CacheStrategyType.TIME == strategyType) {
			localCache = new ScheduledCache(localCache, defaultExpiry);
		}

		// 如果是local必须
		localCache = new SynchronizedCache(localCache);

		// log可选
		boolean log = false;
		String _log = properties.get("log");
		if (null != _log) {
			log = Boolean.parseBoolean(_log);
		}
		if (log) {
			localCache = new LoggingCache(localCache);
		}

		return localCache;
	}

}
