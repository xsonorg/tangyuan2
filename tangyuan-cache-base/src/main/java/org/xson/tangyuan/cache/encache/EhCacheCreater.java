package org.xson.tangyuan.cache.encache;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheCreater;
import org.xson.tangyuan.cache.CacheException;
import org.xson.tangyuan.cache.CacheVo;

public class EhCacheCreater implements CacheCreater {

	@Override
	public AbstractCache newInstance(CacheVo cacheVo) {
		EhCacheCache cache = new EhCacheCache(cacheVo.getId());
		String resource = cacheVo.getResource();
		if (null == resource) {
			throw new CacheException("missing resource in ehcache type");
		}
		// cache.start(resource, cacheVo.getProperties());
		cache.start(cacheVo);
		return cache;
	}

}
