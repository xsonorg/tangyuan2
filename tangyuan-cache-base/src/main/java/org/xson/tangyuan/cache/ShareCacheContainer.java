package org.xson.tangyuan.cache;

import java.util.Map;
import java.util.Map.Entry;

/**
 * 共享数据源
 */
public final class ShareCacheContainer {

	private static ShareCacheContainer	instance	= new ShareCacheContainer();

	private Map<String, AbstractCache>	cacheMap;

	private ShareCacheContainer() {
	}

	public static ShareCacheContainer getInstance() {
		return instance;
	}

	public AbstractCache getCache(String sharedName) {
		if (null == cacheMap) {
			return null;
		}
		return cacheMap.get(sharedName);
	}

	public void setCacheMap(Map<String, AbstractCache> cacheMap) {
		this.cacheMap = cacheMap;
	}

	public void close(String creator) {
		if (null == cacheMap) {
			return;
		}
		for (Entry<String, AbstractCache> entity : cacheMap.entrySet()) {
			try {
				entity.getValue().stop(creator);
			} catch (Throwable e) {
			}
		}
	}
}
