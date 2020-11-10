package org.xson.tangyuan.cache.local;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.cache.AbstractCache;

public class ScheduledCache extends AbstractCache {

	private AbstractCache     delegate;
	private Map<Object, Long> keyMap;

	public ScheduledCache(AbstractCache delegate, long defaultExpiry) {
		this.delegate = delegate;
		this.defaultExpiry = defaultExpiry;
		this.keyMap = new HashMap<Object, Long>();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	/**
	 * time: 超时时间, 单位秒
	 */
	@Override
	public void put(Object key, Object value, Long expiry) {
		Long expiryTime = getExpiry(expiry, defaultExpiry);
		keyMap.put(key, System.currentTimeMillis() + expiryTime.longValue() * 1000L);
		delegate.put(key, value);
	}

	@Override
	public Object get(Object key) {
		if (clearWhenStale(key)) {
			return null;
		} else {
			return delegate.get(key);
		}
	}

	@Override
	public Object remove(Object key) {
		keyMap.remove(key);
		return delegate.remove(key);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	// true: 超时, false: 未超时
	private boolean clearWhenStale(Object key) {
		Long survivalTime = keyMap.get(key);
		if (null == survivalTime) {
			return true;
		}
		if (System.currentTimeMillis() > survivalTime.longValue()) {
			remove(key);
			return true;
		}
		return false;
	}

}
