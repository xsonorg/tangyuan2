package org.xson.tangyuan.timer;

/**
 * 定时器控制处理器
 */
public interface TimerControlerHandler {

	/**
	 * 检测服务是否可以执行
	 * 
	 * @param service
	 *            服务名称
	 * @return true:可以执行
	 */
	public boolean checkRunning(String service);

	/**
	 * 更新执行的服务标识, 比如时间
	 */
	public boolean update(String service);
}
