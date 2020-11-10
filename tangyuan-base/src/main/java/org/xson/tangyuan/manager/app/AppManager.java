package org.xson.tangyuan.manager.app;

/**
 * APP管理器，负责整个App是否接受外部请求
 */
public interface AppManager {

	void init(String resource);

	boolean isAccessed(String service);

	//	void update(XCO data);
}
