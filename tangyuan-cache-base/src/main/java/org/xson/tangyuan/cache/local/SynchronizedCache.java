package org.xson.tangyuan.cache.local;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.xson.tangyuan.cache.AbstractCache;

public class SynchronizedCache extends AbstractCache {

	private AbstractCache	cache;
	private ReadWriteLock	lock;

	public SynchronizedCache(AbstractCache cache) {
		this.cache = cache;
		this.lock = new ReentrantReadWriteLock();
	}

	@Override
	public void start(String resource, Map<String, String> properties) {
		this.cache.start(resource, properties);
	}

	@Override
	public void stop(String creator) {
		this.cache.stop(creator);
	}

	@Override
	public String getId() {
		return cache.getId();
	}

	@Override
	public void put(Object key, Object value, Long expiry) {
		this.lock.writeLock().lock();
		try {
			cache.put(key, value, expiry);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public Object get(Object key) {
		this.lock.readLock().lock();
		try {
			return cache.get(key);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	@Override
	public Object remove(Object key) {
		this.lock.writeLock().lock();
		try {
			return cache.remove(key);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public void clear() {
		this.lock.writeLock().lock();
		try {
			cache.clear();
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public int getSize() {
		this.lock.readLock().lock();
		try {
			return cache.getSize();
		} finally {
			this.lock.readLock().unlock();
		}
	}
}
