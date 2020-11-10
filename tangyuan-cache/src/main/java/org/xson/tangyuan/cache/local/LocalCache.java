package org.xson.tangyuan.cache.local;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheException;

/**
 * 底层Cache
 */
public class LocalCache extends AbstractCache {

	public enum CacheStrategyType {
		LRU, FIFO, SOFT, WEAK, TIME
	}

	private Map<Object, Object> cache = new HashMap<Object, Object>(1024);

	public LocalCache(String cacheId) {
		this.cacheId = cacheId;
	}

	public int getSize() {
		return cache.size();
	}

	@Override
	public void put(Object key, Object value, Long expiry) {
		cache.put(key, value);
	}

	public Object get(Object key) {
		return cache.get(key);
	}

	public Object remove(Object key) {
		return cache.remove(key);
	}

	public void clear() {
		cache.clear();
	}

	public boolean equals(Object o) {
		if (getId() == null) {
			throw new CacheException("Cache instances require an ID.");
		}
		if (this == o) {
			return true;
		}

		if (!(o instanceof AbstractCache)) {
			return false;
		}

		AbstractCache otherCache = (AbstractCache) o;
		return getId().equals(otherCache.getId());
	}

	public int hashCode() {
		if (getId() == null) {
			throw new CacheException("Cache instances require an ID.");
		}
		return getId().hashCode();
	}

}
