package org.xson.tangyuan.timer;

import java.util.Map;

/**
 * timer组件单活控制器
 */
public interface TimerComponentSingleLiveController {

	boolean isSingleLive();

	void start(Map<String, Object> properties) throws Throwable;

	void stop();

}
