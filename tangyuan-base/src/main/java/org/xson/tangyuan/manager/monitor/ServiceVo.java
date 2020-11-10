package org.xson.tangyuan.manager.monitor;

public class ServiceVo {

	private String  service;
	//	private String	hostName;
	private long    timeCost;
	private boolean success;
	private long    startTime;
	private long    endTime;

	//	public ServiceVo(String target, String hostName, boolean success, long startTime, long endTime) {
	//		this.target = target;
	//		this.hostName = hostName;
	//		this.success = success;
	//		this.startTime = startTime;
	//		this.endTime = endTime;
	//		this.timeCost = this.endTime - this.startTime;
	//	}

	public ServiceVo(String service, boolean success, long startTime, long endTime) {
		this.service = service;
		this.success = success;
		this.startTime = startTime;
		this.endTime = endTime;
		this.timeCost = this.endTime - this.startTime;
	}

	//	public String getServiceKey() {
	//		return hostName + "_" + target;
	//	}
	//
	//	public String getTarget() {
	//		return target;
	//	}
	//
	//	public String getHostName() {
	//		return hostName;
	//	}

	public String getService() {
		return service;
	}

	public long getTimeCost() {
		return timeCost;
	}

	public boolean isSuccess() {
		return success;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

}
