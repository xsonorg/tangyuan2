package org.xson.tangyuan.cache.local;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.cache.AbstractCache;

public class ScheduledCache extends AbstractCache {

	private AbstractCache		delegate;

	private Map<Object, Long>	keyMap;

	private long				defaultSurvivalTime;

	public ScheduledCache(AbstractCache delegate, long defaultSurvivalTime) {
		this.delegate = delegate;
		this.defaultSurvivalTime = defaultSurvivalTime;
		keyMap = new HashMap<Object, Long>();
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
		long survivalTime = this.defaultSurvivalTime;
		if (null != expiry) {
			survivalTime = expiry.longValue();
		}
		keyMap.put(key, System.currentTimeMillis() + survivalTime * 1000L);
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
