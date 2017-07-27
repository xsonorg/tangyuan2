package org.xson.tangyuan.cache;

public interface TangYuanCache {

	void put(Object key, Object value);

	/**
	 * 放置任务
	 * 
	 * @param key
	 * @param value
	 * @param expiry
	 *            过期时间
	 */
	void put(Object key, Object value, Long expiry);

	Object get(Object key);

	Object remove(Object key);

}
