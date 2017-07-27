package org.xson.tangyuan.cache.share;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheCreater;
import org.xson.tangyuan.cache.CacheException;
import org.xson.tangyuan.cache.CacheVo;
import org.xson.tangyuan.cache.ShareCacheContainer;

public class ShareCacheCreater implements CacheCreater {

	@Override
	public AbstractCache newInstance(CacheVo cacheVo) {
		String jndiName = cacheVo.getJndiName();
		AbstractCache cache = ShareCacheContainer.getInstance().getCache(jndiName);
		if (null == cache) {
			throw new CacheException("Non-existent share cache: " + jndiName);
		}
		return cache;
	}

}
