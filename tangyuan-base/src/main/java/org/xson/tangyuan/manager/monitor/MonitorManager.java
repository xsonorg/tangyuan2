package org.xson.tangyuan.manager.monitor;

/**
 * 监控管理器
 */
public interface MonitorManager {

	void init(String resource) throws Throwable;

	void start();

	void stop();

	void addTask(Object task);
}
