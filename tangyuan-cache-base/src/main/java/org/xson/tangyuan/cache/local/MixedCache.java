package org.xson.tangyuan.cache.local;

import java.util.List;

import org.xson.tangyuan.cache.AbstractCache;

public class MixedCache extends AbstractCache {

	// private String cacheId = null;
	private List<AbstractCache> cacheList = null;

	public MixedCache(String cacheId, List<AbstractCache> cacheList) {
		this.cacheId = cacheId;
		this.cacheList = cacheList;
	}

	@Override
	public void put(Object key, Object value, Long expiry) {
		for (AbstractCache ref : cacheList) {
			ref.put(key, value, expiry);
		}
	}

	@Override
	public Object get(Object key) {
		Object result = null;
		for (AbstractCache ref : cacheList) {
			result = ref.get(key);
			if (null != result) {
				break;
			}
		}
		return result;
	}

	@Override
	public Object remove(Object key) {
		Object result = null;
		for (AbstractCache ref : cacheList) {
			Object value = ref.remove(key);
			if (null == result && null != value) {
				result = value;
			}
		}
		return result;
	}

	// @Override
	// public String getId() {
	// return cacheId;
	// }

}
