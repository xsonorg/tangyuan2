package org.xson.tangyuan;

import java.util.Map;

public interface TangYuanComponent {

	void config(Map<String, String> properties);

	void start(String resource) throws Throwable;

	/**
	 * 关闭组件
	 * 
	 * @param waitingTime 	等待时间, 0:立即
	 * @param asyn			异步关闭
	 */
	void stop(long waitingTime, boolean asyn);
}
