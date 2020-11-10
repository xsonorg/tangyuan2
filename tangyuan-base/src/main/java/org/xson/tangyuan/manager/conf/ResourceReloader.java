package org.xson.tangyuan.manager.conf;

/**
 * 资源重载接口
 */
public interface ResourceReloader {

	/**
	 * 
	 * @param resource
	 * @param context 默认为Null
	 * @throws Throwable
	 */
	//	void reload(String resource, String context) throws Throwable;

	void reload(String resource) throws Throwable;

}
