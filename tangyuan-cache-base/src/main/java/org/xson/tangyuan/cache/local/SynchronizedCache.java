package org.xson.tangyuan.cache.local;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheVo;

public class SynchronizedCache extends AbstractCache {

	private AbstractCache	delegate;
	private ReadWriteLock	lock;

	public SynchronizedCache(AbstractCache delegate) {
		this.delegate = delegate;
		this.lock = new ReentrantReadWriteLock();
	}

	// @Override
	// public void start(String resource, Map<String, String> properties) {
	// this.cache.start(resource, properties);
	// }

	@Override
	public void start(CacheVo cacheVo) {
		this.delegate.start(cacheVo);
	}

	@Override
	public void stop(String creator) {
		this.delegate.stop(creator);
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public void put(Object key, Object value, Long expiry) {
		this.lock.writeLock().lock();
		try {
			delegate.put(key, value, expiry);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public Object get(Object key) {
		this.lock.readLock().lock();
		try {
			return delegate.get(key);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	@Override
	public Object remove(Object key) {
		this.lock.writeLock().lock();
		try {
			return delegate.remove(key);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public void clear() {
		this.lock.writeLock().lock();
		try {
			delegate.clear();
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public int getSize() {
		this.lock.readLock().lock();
		try {
			return delegate.getSize();
		} finally {
			this.lock.readLock().unlock();
		}
	}
}
