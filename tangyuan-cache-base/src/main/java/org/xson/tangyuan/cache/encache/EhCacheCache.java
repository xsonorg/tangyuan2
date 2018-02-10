package org.xson.tangyuan.cache.encache;

import java.io.InputStream;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheException;
import org.xson.tangyuan.cache.CacheVo;
import org.xson.tangyuan.cache.util.PlaceholderResourceSupport;
import org.xson.tangyuan.cache.util.Resources;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class EhCacheCache extends AbstractCache {

	// private String cacheId = null;
	private CacheManager	cacheManager	= null;
	private Cache			cache			= null;

	public EhCacheCache(String cacheId) {
		this.cacheId = cacheId;
	}

	// @Override
	// public void start(String resource, Map<String, String> properties) {
	// if (null != cacheManager) {
	// return;
	// }
	// try {
	// InputStream inputStream = Resources.getResourceAsStream(resource);
	// this.cacheManager = CacheManager.create(inputStream);
	// this.cache = cacheManager.getCache(cacheManager.getCacheNames()[0]);
	// } catch (Throwable e) {
	// throw new CacheException(e);
	// }
	// }

	@Override
	public void start(CacheVo cacheVo) {
		if (null != cacheManager) {
			return;
		}

		String resource = cacheVo.getResource();

		try {
			InputStream inputStream = Resources.getResourceAsStream(resource);
			inputStream = PlaceholderResourceSupport.processInputStream(inputStream, cacheVo.getPlaceholderMap());
			this.cacheManager = CacheManager.create(inputStream);
			this.cache = cacheManager.getCache(cacheManager.getCacheNames()[0]);
		} catch (Throwable e) {
			throw new CacheException(e);
		}

		this.defaultExpiry = cacheVo.getExpiry();
		this.serializer = cacheVo.getSerializer();
	}

	@Override
	public void stop(String creator) {
		if (this.creator != creator || !this.creator.equals(creator)) {
			return;
		}
		if (null != cacheManager) {
			cacheManager.shutdown();
		}
	}

	@Override
	public Object get(Object key) {
		Element element = this.cache.get(key);
		if (null != element) {
			return element.getObjectValue();
		}
		return null;
	}

	// 1.TTI timeToIdleSeconds is the maximum number of seconds that an element
	// can exist in the cache without being accessed:
	// TTI用于设置对象在cache中的最大闲置时间，就是 在一直不访问这个对象的前提下，这个对象可以在cache中的存活时间。
	// 2.TTL timeToLiveSeconds is the maximum number of seconds that an element
	// can exist in the cache whether
	// or not is has been accessed.
	// TTL用于设置对象在cache中的最大存活时间，就是 无论对象访问或是不访问(闲置),这个对象在cache中的存活时间。
	// 3.If the eternal flag is set, elements are allowed to exist in the cache
	// eternally and none are evicted。
	// 当配置了 eternal ，那么TTI和TTL这两个配置将被覆盖，对象会永恒存在cache中，永远不会过期。

	@Override
	public void put(Object key, Object value, Long expiry) {
		Element element = null;
		Long expiryTime = getExpiry(expiry, defaultExpiry);
		if (null == expiryTime) {
			element = new Element(key, value);
		} else {
			element = new Element(key, value, expiryTime.intValue(), expiryTime.intValue());
		}
		this.cache.put(element);
	}

	@Override
	public Object remove(Object key) {
		Object result = get(key);
		if (null != result) {
			this.cache.remove(key);
		}
		return result;
	}

	@Override
	public void clear() {
		this.cache.removeAll();
	}

	@Override
	public int getSize() {
		return cache.getSize();
	}

	public Cache getCache() {
		return cache;
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	// @Override
	// public String getId() {
	// return cacheId;
	// }

	// //得到缓存对象占用内存的大小
	// cache.getMemoryStoreSize();
	// //得到缓存读取的命中次数
	// cache.getStatistics().getCacheHits()
	// //得到缓存读取的错失次数
	// cache.getStatistics().getCacheMisses()

}
