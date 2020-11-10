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
		String     resource = cacheVo.getResource();
		Properties p        = MixedResourceManager.getProperties(resource, true, true);
		client = new JedisClient();
		client.start(p);

		// register reloader
		// XmlGlobalContext.addReloaderVo(new ResourceReloaderVo(resource, this));
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
		Long              expiryTime = getExpiry(expiry, defaultExpiry);
		CacheSerializer   cs         = getSerializer();
		final JedisClient client     = this.client;
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
				String result = client.set(parseKey(key).getBytes(keyEncode), (byte[]) cs.serialize(value), "nx".getBytes(keyEncode), "ex".getBytes(keyEncode),
						expiryTime.longValue());
				// System.out.println(result);
				if (!"OK".equalsIgnoreCase(result)) {
					client.set(parseKey(key).getBytes(keyEncode), (byte[]) cs.serialize(value), "xx".getBytes(keyEncode), "ex".getBytes(keyEncode), expiryTime.longValue());
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

	//	private Log         log    = LogFactory.getLog(getClass());
	//		if (this.creator != creator || !this.creator.equals(creator)) {
	//			return;
	//		}
	//			InputStream inputStream = ResourceManager.getInputStream(resource, cacheVo.getPlaceholderMap());
	//			Properties  properties  = new Properties();
	//			properties.load(inputStream);
	//			inputStream.close();
	//		try {
	//			String resource = cacheVo.getResource();
	//			client = new JedisClient();
	//			Properties p = MixedResourceManager.getProperties(resource, true);
	//			client.start(p);
	//		} catch (Throwable e) {
	//			throw new CacheException(e);
	//		}

	//	private void update(JedisClient client) {
	//		this.client = client;
	//	}

	//	@Override
	//	public void reload(String resource, String context) throws Throwable {
	//		Properties  p         = getPropertiesForReload(resource, context, true, true);
	//		JedisClient newClient = new JedisClient();
	//		newClient.start(p);
	//		JedisClient oldClient = this.client;
	//		update(newClient);
	//		log.info(TangYuanLang.get("resource.reload"), resource);
	//
	//		// reload的时候就不考虑多线程并发的问题，有一定几率的错误情况
	//		// 如果要避免此问题，需要所有相关方法操作为同步，并且关闭的时候需要时间等待，但也是有几率出现错误的
	//
	//		//		new Thread(() -> {
	//		//			try {
	//		//				wait(reloadWaitTime);
	//		//			} catch (InterruptedException e) {
	//		//				log.error(e);
	//		//			}
	//		//			oldClient.stop();
	//		//		}).start();
	//
	//		Thread ct = new Thread(() -> {
	//			try {
	//				wait(reloadWaitTime);
	//			} catch (InterruptedException e) {
	//				log.error(e);
	//			}
	//			oldClient.stop();
	//		});
	//		ct.setDaemon(true);
	//		ct.start();
	//	}
}
