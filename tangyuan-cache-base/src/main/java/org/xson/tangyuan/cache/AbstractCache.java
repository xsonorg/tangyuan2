package org.xson.tangyuan.cache;

public abstract class AbstractCache implements TangYuanCache {

	protected String			keyEncode		= "UTF-8";
	protected String			cacheId			= null;
	protected String			creator			= null;
	protected Long				defaultExpiry	= null;
	protected CacheSerializer	serializer		= null;

	public void start(CacheVo cacheVo) {
	}

	public void stop(String creator) {
	}

	public void clear() {
	}

	public int getSize() {
		return -1;
	}

	public String getId() {
		return this.cacheId;
	}

	@Override
	public void put(Object key, Object value) {
		put(key, value, null);
	}

	protected String parseKey(Object key) {
		if (null == key) {
			throw new CacheException("cache key does not allow null");
		}
		if (key instanceof String) {
			return (String) key;
		}
		return key.toString();
	}

	protected Long getExpiry(Long currentExpiry, Long defaultExpiry) {
		if (null != currentExpiry) {
			return currentExpiry;
		}
		if (null != defaultExpiry) {
			return defaultExpiry;
		}
		return null;
	}

	// public void start(String resource, Map<String, String> properties) {
	// }
}
