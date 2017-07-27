package org.xson.tangyuan.cache.helper;

import org.xson.tangyuan.cache.CacheComponent;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.cache.encache.EhCacheCache;

import net.sf.ehcache.CacheManager;

public class EhCacheHelper {

	/**
	 * 从系统容器中获取默认的EhCache实例
	 * 
	 * @param cacheId
	 * @return
	 */
	public static net.sf.ehcache.Cache getDefaultEhCache(String cacheId) {
		TangYuanCache tangYuanCache = CacheComponent.getInstance().getCache(cacheId);
		if (null != tangYuanCache) {
			EhCacheCache ehCacheCache = (EhCacheCache) tangYuanCache;
			return ehCacheCache.getCache();
		}
		return null;
	}

	/**
	 * 从系统容器中获取默认的EhCache CacheManager实例
	 * 
	 * @param cacheId
	 * @return
	 */
	public static CacheManager getEhCacheCacheManager(String cacheId) {
		TangYuanCache tangYuanCache = CacheComponent.getInstance().getCache(cacheId);
		if (null != tangYuanCache) {
			EhCacheCache ehCacheCache = (EhCacheCache) tangYuanCache;
			return ehCacheCache.getCacheManager();
		}
		return null;
	}

}
