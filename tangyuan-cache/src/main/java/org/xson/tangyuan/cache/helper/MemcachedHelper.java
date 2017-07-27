package org.xson.tangyuan.cache.helper;

import org.xson.tangyuan.cache.CacheComponent;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.cache.memcache.MemcachedCache;

import com.whalin.MemCached.MemCachedClient;

public class MemcachedHelper {

	/**
	 * 从系统容器中获取MemcachedCache实例
	 * 
	 * @param cacheId
	 * @return
	 */
	public static MemCachedClient getMemcachedCache(String cacheId) {
		TangYuanCache tangYuanCache = CacheComponent.getInstance().getCache(cacheId);
		if (null != tangYuanCache) {
			MemcachedCache memcachedCache = (MemcachedCache) tangYuanCache;
			return memcachedCache.getCachedClient();
		}
		return null;
	}

}
