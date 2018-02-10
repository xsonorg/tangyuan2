package org.xson.tangyuan.cache;

public interface CacheSerializer {

	/**
	 * 序列化
	 */
	public Object serialize(Object object) throws Throwable;

	/**
	 * 反序列化
	 */
	public Object deserialize(Object object) throws Throwable;

}
