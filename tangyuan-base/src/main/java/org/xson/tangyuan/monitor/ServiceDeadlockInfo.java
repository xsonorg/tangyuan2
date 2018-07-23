package org.xson.tangyuan.monitor;

/**
 * 上下文监控信息
 */
public class ServiceDeadlockInfo {

//	// 线程ID
//	private long			threadId;
//	// 线程Name
//	private String			threadName;
//	// 开始时间
//	private long			startTime;
//	// 结束时间
//	private long			endTime	= 0L;
//	// 执行状态
//	private boolean			running	= true;
//	// 当前上下文HASH
//	private int				contextHashCode;
//	// 所涉及的服务
//	private List<String>	services;
//
//	public ServiceDeadlockInfo(int hashCode) {
//		this.contextHashCode = hashCode;
//		this.startTime = System.currentTimeMillis();
//		Thread currentThread = Thread.currentThread();
//		this.threadId = currentThread.getId();
//		this.threadName = currentThread.getName();
//		services = new ArrayList<String>();
//	}
//
//	@Override
//	public String toString() {
//		long _endTime = this.endTime;
//		if (0L == _endTime) {
//			_endTime = System.currentTimeMillis();
//		}
//		long execTime = _endTime - startTime;
//		if (execTime < TangYuanContainer.getInstance().getDeadlockIntervalTime()) {
//			return null;
//		}
//
//		// context[xxxx], thread[xxxx], startTime[xxx], execTime[xxx], running[true], services[a,b,c,d],
//		StringBuilder builder = new StringBuilder();
//		builder.append("context[" + this.contextHashCode + "], ");
//		builder.append("thread[" + this.threadId + ":" + this.threadName + "], ");
//		builder.append("startTime[" + DateUtils.getDateTimeString(new Date(this.startTime)) + "], ");
//		builder.append("execTime[" + execTime + "], ");
//		builder.append("running[" + this.running + "], ");
//		// builder.append("services[a,b,c,d]");
//		builder.append("services[");
//		for (int i = 0; i < services.size(); i++) {
//			String temp = services.get(i);
//			builder.append(temp);
//			if (i > 0) {
//				builder.append(",");
//			}
//		}
//		builder.append("]");
//
//		return builder.toString();
//	}
//
//	public boolean isRunning() {
//		return running;
//	}
//
//	public void joinMonitor() {
//		TangYuanContainer.getInstance().getDeadlockMonitor().add(this);
//	}
//
//	public void update(String service) {
//		services.add(service);
//	}
//
//	public void stop() {
//		this.running = false;
//		this.endTime = System.currentTimeMillis();
//	}
}
