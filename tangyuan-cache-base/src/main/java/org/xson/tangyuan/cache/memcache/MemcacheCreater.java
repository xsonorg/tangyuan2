package org.xson.tangyuan.cache.memcache;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheCreater;
import org.xson.tangyuan.cache.CacheVo;

public class MemcacheCreater implements CacheCreater {

	@Override
	public AbstractCache newInstance(CacheVo cacheVo) {
		MemcachedCache cache = new MemcachedCache(cacheVo.getId());
		cache.start(cacheVo);
		return cache;
	}

}
