package org.xson.tangyuan.manager.service;

/**
 * 服务管理器
 */
public interface ServiceManager {

	void init(String resource);

	boolean isAccessed(String service);

	//	void update(XCO data);
}
