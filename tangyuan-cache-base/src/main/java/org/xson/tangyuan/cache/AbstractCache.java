package org.xson.tangyuan.cache;

import java.util.Map;

public abstract class AbstractCache implements TangYuanCache {

	protected String	keyEncode	= "UTF-8";

	protected String	creator;

	public void start(String resource, Map<String, String> properties) {
	}

	public void stop(String creator) {
	}

	public void clear() {
	}

	public int getSize() {
		return -1;
	}

	public String getId() {
		return null;
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

}
