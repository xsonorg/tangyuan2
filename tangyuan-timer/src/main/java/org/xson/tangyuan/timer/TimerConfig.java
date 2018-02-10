package org.xson.tangyuan.timer;

public class TimerConfig {

	private String		scheduled;
	private String		service;
	private boolean		sync;
	private boolean		onExceptionBreak;
	private String		desc;
	private CustomJob	customJob;

	public TimerConfig(String scheduled, String service, boolean sync, boolean onExceptionBreak, String desc, CustomJob customJob) {
		this.scheduled = scheduled;
		this.service = service;
		this.sync = sync;
		this.onExceptionBreak = onExceptionBreak;
		this.desc = desc;
		this.customJob = customJob;
	}

	public String getScheduled() {
		return scheduled;
	}

	public String getService() {
		return service;
	}

	public boolean isSync() {
		return sync;
	}

	public boolean isOnExceptionBreak() {
		return onExceptionBreak;
	}

	public String getDesc() {
		return desc;
	}

	public CustomJob getCustomJob() {
		return customJob;
	}

}
