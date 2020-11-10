package org.xson.tangyuan.cache.local;

import java.util.LinkedList;

import org.xson.tangyuan.cache.AbstractCache;

public class FIFOCache extends AbstractCache {

	private final AbstractCache	delegate;
	private LinkedList<Object>	keyList;
	private int					size;

	public FIFOCache(AbstractCache delegate, int size) {
		this.delegate = delegate;
		this.keyList = new LinkedList<Object>();
		this.size = size;// 1024
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	@Override
	public void put(Object key, Object value, Long expiry) {
		cycleKeyList(key);
		delegate.put(key, value, expiry);
	}

	@Override
	public Object get(Object key) {
		return delegate.get(key);
	}

	@Override
	public Object remove(Object key) {
		return delegate.remove(key);
	}

	@Override
	public void clear() {
		delegate.clear();
		keyList.clear();
	}

	private void cycleKeyList(Object key) {
		keyList.addLast(key);
		if (keyList.size() > size) {
			Object oldestKey = keyList.removeFirst();
			delegate.remove(oldestKey);
		}
	}
}
