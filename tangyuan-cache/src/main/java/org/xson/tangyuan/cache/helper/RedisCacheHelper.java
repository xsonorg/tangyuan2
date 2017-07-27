package org.xson.tangyuan.cache.helper;

import org.xson.tangyuan.cache.CacheComponent;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.cache.redis.RedisCache;
import org.xson.thirdparty.redis.JedisClient;

public class RedisCacheHelper {

	/**
	 * 从系统容器中获取JedisClient实例
	 */
	public static JedisClient getJedisClient(String cacheId) {
		TangYuanCache tangYuanCache = CacheComponent.getInstance().getCache(cacheId);
		if (null != tangYuanCache) {
			RedisCache redisCache = (RedisCache) tangYuanCache;
			return redisCache.getClient();
		}
		return null;
	}

}
