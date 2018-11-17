package org.xson.tangyuan.timer;

import java.util.Map;

/**
 * 定时程序高可用控制器
 */
public interface TimerHAController {

	public boolean isAvailable();

	public void doStart(Map<String, Object> properties) throws Throwable;

	public void doStop();

}
