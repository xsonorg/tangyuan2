package org.xson.tangyuan.cache.redis;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheCreater;
import org.xson.tangyuan.cache.CacheVo;

public class RedisCreater implements CacheCreater {

	@Override
	public AbstractCache newInstance(CacheVo cacheVo) {
		RedisCache cache = new RedisCache(cacheVo.getId());
		cache.start(cacheVo.getResource(), cacheVo.getProperties());
		return cache;
	}

}
