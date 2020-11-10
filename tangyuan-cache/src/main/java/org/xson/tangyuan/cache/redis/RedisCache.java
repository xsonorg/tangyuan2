package org.xson.tangyuan.cache.redis;

import java.util.Properties;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheException;
import org.xson.tangyuan.cache.CacheSerializer;
import org.xson.tangyuan.cache.util.JDKSerializer;
import org.xson.tangyuan.cache.xml.vo.CacheVo;
import org.xson.tangyuan.util.MixedResourceManager;
import org.xson.thirdparty.redis.JedisClient;

public class RedisCache extends AbstractCache {

	private JedisClient client = null;

	public RedisCache(String cacheId) {
		this.cacheId = cacheId;
	}

	@Override
	public void start(CacheVo cacheVo) throws Throwable {
		if (null != client) {
			return;
		}
		this.defaultExpiry = cacheVo.getExpiry();
		this.serializer = cacheVo.getSerializer();
		// init
		String resource = cacheVo.getResource();
		Properties p = MixedResourceManager.getProperties(resource, true, true);
		client = new JedisClient();
		client.start(p);
	}

	@Override
	public void stop() {
		if (null != this.client) {
			this.client.stop();
		}
	}

	@Override
	public Object get(Object key) {
		CacheSerializer cs = getSerializer();
		try {
			byte[] bytes = this.client.get(parseKey(key).getBytes(keyEncode));
			if (null != bytes) {
				return cs.deserialize(bytes);
			}
			return null;
		} catch (Throwable e) {
			throw new CacheException(e);
		}
	}

	@Override
	public void put(Object key, Object value, Long expiry) {
		if (null == value) {
			remove(key);
			return;
		}
		Long expiryTime = getExpiry(expiry, defaultExpiry);
		CacheSerializer cs = getSerializer();
		final JedisClient client = this.client;
		try {
			if (null == expiryTime) {
				client.set(parseKey(key).getBytes(keyEncode), (byte[]) cs.serialize(value));
			} else {
				// EX second ：设置键的过期时间为 second 秒。 SET key value EX second 效果等同于
				// SETEX key second value 。
				// PX millisecond ：设置键的过期时间为 millisecond 毫秒。 SET key value PX
				// millisecond 效果等同于 PSETEX key millisecond value 。
				// NX ：只在键不存在时，才对键进行设置操作。 SET key value NX 效果等同于 SETNX key value
				// XX ：只在键已经存在时，才对键进行设置操作。
				String result = client.set(parseKey(key).getBytes(keyEncode), (byte[]) cs.serialize(value), "nx".getBytes(keyEncode),
						"ex".getBytes(keyEncode), expiryTime.longValue());
				if (!"OK".equalsIgnoreCase(result)) {
					client.set(parseKey(key).getBytes(keyEncode), (byte[]) cs.serialize(value), "xx".getBytes(keyEncode), "ex".getBytes(keyEncode),
							expiryTime.longValue());
				}
			}
		} catch (Throwable e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Object remove(Object key) {
		Object result = get(key);
		if (null != result) {
			this.client.del(parseKey(key));
		}
		return result;
	}

	public JedisClient getClient() {
		return client;
	}

	protected CacheSerializer getSerializer() {
		if (null != serializer) {
			return serializer;
		}
		return JDKSerializer.instance;
	}

}
