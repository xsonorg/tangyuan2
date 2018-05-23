package org.xson.tangyuan.timer;

import java.util.Map;
import java.util.Map.Entry;

import org.xson.common.object.XCO;

public class TimerConfig {

	private String				scheduled;
	private String				service;
	private boolean				sync;
	private boolean				onExceptionBreak;
	private String				desc;
	private CustomJob			customJob;

	private Map<String, String>	properties;
	private Map<String, Object>	preProperties;

	public TimerConfig(String scheduled, String service, boolean sync, boolean onExceptionBreak, String desc, CustomJob customJob,
			Map<String, String> properties, Map<String, Object> preProperties) {
		this.scheduled = scheduled;
		this.service = service;
		this.sync = sync;
		this.onExceptionBreak = onExceptionBreak;
		this.desc = desc;
		this.customJob = customJob;

		this.properties = properties;
		this.preProperties = preProperties;
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

	public Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * 非customJob专用
	 */
	public Object getArg(Object arg) {
		if (null == arg) {
			arg = new XCO();
		}
		if (null != this.preProperties) {
			XCO xco = (XCO) arg;
			for (Entry<String, Object> entry : this.preProperties.entrySet()) {
				Object value = entry.getValue();
				if (java.util.Date.class == value) {
					xco.setDateTimeValue(entry.getKey(), new java.util.Date());
				} else if (java.sql.Date.class == value) {
					xco.setDateValue(entry.getKey(), new java.sql.Date(new java.util.Date().getTime()));
				} else if (java.sql.Time.class == value) {
					xco.setTimeValue(entry.getKey(), new java.sql.Time(new java.util.Date().getTime()));
				} else if (java.sql.Timestamp.class == value) {
					xco.setTimestampValue(entry.getKey(), new java.sql.Timestamp(new java.util.Date().getTime()));
				} else {
					xco.setObjectValue(entry.getKey(), entry.getValue());
				}
			}
		}
		return arg;
	}
}
