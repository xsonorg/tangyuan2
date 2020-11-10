package org.xson.tangyuan.cache.memcache;

import java.util.Date;
import java.util.Map;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.xml.vo.CacheVo;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.util.MixedResourceManager;
import org.xson.tangyuan.util.PropertyUtils;
import org.xson.tangyuan.xml.XmlParseException;

import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;

public class MemcachedCache extends AbstractCache {

	private MemCachedClient cachedClient = null;
	private SockIOPool      pool         = null;

	public MemcachedCache(String cacheId) {
		this.cacheId = cacheId;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void start(CacheVo cacheVo) throws Throwable {
		if (null != cachedClient || null != pool) {
			return;
		}

		String              resource   = cacheVo.getResource();
		Map<String, String> properties = (Map) MixedResourceManager.getProperties(resource, true, true);

		// { "cache0.server.com:12345", "cache1.server.com:12345" };
		String[]            serverlist = PropertyUtils.getStringArrayValue(properties, "serverlist", ",");
		if (null == serverlist) {
			throw new XmlParseException(TangYuanLang.get("property.empty", "serverlist"));
		}
		// { new Integer(5), new Integer(2) };
		Integer[] weights = PropertyUtils.getIntegerArrayValue(properties, "weights", ",");
		if (null == weights) {
			throw new XmlParseException(TangYuanLang.get("property.empty", "weights"));
		}

		int        initialConnections  = PropertyUtils.getIntValue(properties, "initialConnections", 10);
		int        minSpareConnections = PropertyUtils.getIntValue(properties, "minSpareConnections", 5);
		int        maxSpareConnections = PropertyUtils.getIntValue(properties, "maxSpareConnections", 50);
		// 30 minutes
		int        maxIdleTime         = PropertyUtils.getIntValue(properties, "maxIdleTime", 1000 * 60 * 30);
		// 5 minutes
		long       maxBusyTime         = PropertyUtils.getLongValue(properties, "maxIdleTime", 1000 * 60 * 5);
		// 5 seconds
		long       maintThreadSleep    = PropertyUtils.getLongValue(properties, "maxIdleTime", 1000 * 5);
		// 3 seconds to block on reads
		int        socketTimeOut       = PropertyUtils.getIntValue(properties, "socketTimeOut", 1000 * 60 * 1000 * 3);
		// 3 seconds to block on initial connections. If 0, then will use
		// blocking connect (default)
		int        socketConnectTO     = PropertyUtils.getIntValue(properties, "socketConnectTO", 1000 * 60 * 1000 * 3);
		// turn off Nagle's algorithm on all sockets in pool
		boolean    failover            = PropertyUtils.getBooleanValue(properties, "failover", false);
		boolean    failback            = PropertyUtils.getBooleanValue(properties, "failback", false);
		// turn off Nagle's algorithm on all sockets in pool
		boolean    nagleAlg            = PropertyUtils.getBooleanValue(properties, "nagleAlg", false);
		// disable health check of socket on checkout
		boolean    aliveCheck          = PropertyUtils.getBooleanValue(properties, "aliveCheck", false);

		SockIOPool pool                = SockIOPool.getInstance();
		pool.setServers(serverlist);
		pool.setWeights(weights);
		pool.setInitConn(initialConnections);
		pool.setMinConn(minSpareConnections);
		pool.setMaxConn(maxSpareConnections);
		pool.setMaxIdle(maxIdleTime);
		pool.setMaxBusyTime(maxBusyTime);
		pool.setMaintSleep(maintThreadSleep);
		pool.setSocketTO(socketTimeOut);
		pool.setSocketConnectTO(socketConnectTO);
		pool.setFailover(failover);
		pool.setFailback(failback);
		pool.setNagle(nagleAlg);
		pool.setHashingAlg(SockIOPool.NEW_COMPAT_HASH);
		pool.setAliveCheck(aliveCheck);
		pool.initialize();

		cachedClient = new MemCachedClient();
		if (properties.containsKey("defaultEncoding".toUpperCase())) {
			cachedClient.setDefaultEncoding(properties.get("defaultEncoding".toUpperCase()).trim());
		}
		if (properties.containsKey("primitiveAsString".toUpperCase())) {
			cachedClient.setPrimitiveAsString(Boolean.parseBoolean(properties.get("primitiveAsString".toUpperCase()).trim()));
		}
		if (properties.containsKey("sanitizeKeys".toUpperCase())) {
			cachedClient.setSanitizeKeys(Boolean.parseBoolean(properties.get("sanitizeKeys".toUpperCase()).trim()));
		}

		this.defaultExpiry = cacheVo.getExpiry();
		this.serializer = cacheVo.getSerializer();
	}

	@Override
	public void stop() {
		if (null != pool) {
			pool.shutDown();
		}
	}

	@Override
	public void put(Object key, Object value, Long expiry) {
		Long expiryTime = getExpiry(expiry, defaultExpiry);
		if (null == expiryTime) {
			cachedClient.set(parseKey(key), value);
		} else {
			cachedClient.set(parseKey(key), value, new Date(expiryTime.longValue() * 1000L));
		}
	}

	@Override
	public Object get(Object key) {
		return cachedClient.get(parseKey(key));
	}

	@Override
	public Object remove(Object key) {
		Object result = get(key);
		if (null != result) {
			cachedClient.delete(parseKey(key));
		}
		return result;
	}

	@Override
	public void clear() {
		cachedClient.flushAll();
	}

	public MemCachedClient getCachedClient() {
		return cachedClient;
	}

	//		Map<String, String> properties = null;
	//		String              resource   = cacheVo.getResource();
	//		try {
	//			if (null == resource) {
	//				properties = cacheVo.getProperties();
	//				//				PlaceholderResourceSupport.processMap(properties, cacheVo.getPlaceholderMap());
	//				PlaceholderResourceSupport.processMap(properties);
	//			} else {
	//				// InputStream inputStream = ResourceManager.getInputStream(resource, cacheVo.getPlaceholderMap());
	//				InputStream inputStream = MixedResourceManager.getInputStream(resource);
	//				Properties  p           = new Properties();
	//				p.load(inputStream);
	//				inputStream.close();
	//				properties = (Map) p;
	//			}
	//		} catch (Throwable e) {
	//			throw new CacheException(e);
	//		}
	// { "cache0.server.com:12345", "cache1.server.com:12345" };
	//		String              _serverlist = properties.get("serverlist");
	//		if (null == _serverlist) {
	//			throw new CacheException("Memcached miss property 'serverlist'");
	//		}
	//		String[] serverlist = _serverlist.split(",");
	//
	//		// { new Integer(5), new Integer(2) };
	//		String   _weights   = properties.get("weights");
	//		if (null == _weights) {
	//			throw new CacheException("Memcached miss property 'weights'");
	//		}
	//		String[]  array   = _weights.split(",");
	//		Integer[] weights = new Integer[array.length];
	//		for (int i = 0; i < array.length; i++) {
	//			weights[i] = Integer.parseInt(array[i]);
	//		}
}
