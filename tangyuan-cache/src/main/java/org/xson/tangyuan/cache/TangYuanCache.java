package org.xson.tangyuan.cache;

public interface TangYuanCache {

	void put(Object key, Object value);

	/**
	 * 放置缓存内容
	 * @param key
	 * @param value
	 * @param expiry 有效期（单位秒）
	 */
	void put(Object key, Object value, Long expiry);

	Object get(Object key);

	Object remove(Object key);

}
