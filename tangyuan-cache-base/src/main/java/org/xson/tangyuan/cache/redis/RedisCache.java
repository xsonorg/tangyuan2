package org.xson.tangyuan.cache.redis;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheException;
import org.xson.tangyuan.cache.util.Resources;
import org.xson.tangyuan.cache.util.SerializationUtils;
import org.xson.thirdparty.redis.JedisClient;

public class RedisCache extends AbstractCache {

	private String		cacheId	= null;
	private JedisClient	client	= null;

	public RedisCache(String cacheId) {
		this.cacheId = cacheId;
	}

	@Override
	public void start(String resource, Map<String, String> propertyMap) {
		if (null != client) {
			return;
		}
		try {
			// client = JedisClient.getInstance();
			client = new JedisClient();
			Properties properties = new Properties();
			InputStream inputStream = Resources.getResourceAsStream(resource);
			properties.load(inputStream);
			client.start(properties);
		} catch (Throwable e) {
			throw new CacheException(e);
		}
	}

	@Override
	public void stop(String creator) {
		if (this.creator != creator || !this.creator.equals(creator)) {
			return;
		}
		if (null != client) {
			client.stop();
		}
	}

	@Override
	public Object get(Object key) {
		try {
			byte[] bytes = this.client.get(parseKey(key).getBytes(keyEncode));
			if (null != bytes) {
				return SerializationUtils.deserialize(bytes);
			}
			return null;
		} catch (Throwable e) {
			throw new CacheException(e);
		}
	}

	@Override
	public void put(Object key, Object value, Long expiry) {
		try {
			if (null == expiry) {
				this.client.set(parseKey(key).getBytes(keyEncode), SerializationUtils.serialize(value));
			} else {
				// EX second ：设置键的过期时间为 second 秒。 SET key value EX second 效果等同于
				// SETEX key second value 。
				// PX millisecond ：设置键的过期时间为 millisecond 毫秒。 SET key value PX
				// millisecond 效果等同于 PSETEX key millisecond value 。
				// NX ：只在键不存在时，才对键进行设置操作。 SET key value NX 效果等同于 SETNX key value
				// XX ：只在键已经存在时，才对键进行设置操作。
				String result = this.client.set(parseKey(key).getBytes(keyEncode), SerializationUtils.serialize(value), "nx".getBytes(keyEncode),
						"ex".getBytes(keyEncode), expiry.longValue());
				// System.out.println(result);
				if (!"OK".equalsIgnoreCase(result)) {
					this.client.set(parseKey(key).getBytes(keyEncode), SerializationUtils.serialize(value), "xx".getBytes(keyEncode),
							"ex".getBytes(keyEncode), expiry.longValue());
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

	@Override
	public String getId() {
		return cacheId;
	}

}
