package org.xson.tangyuan.timer.xml.vo;

import java.util.Map;

import org.xson.tangyuan.timer.TimerComponentSingleLiveController;

public class SingleLiveControllerVo {

	private TimerComponentSingleLiveController instance;

	private Map<String, Object>                properties;

	public SingleLiveControllerVo(TimerComponentSingleLiveController instance, Map<String, Object> properties) {
		this.instance = instance;
		this.properties = properties;
	}

	public TimerComponentSingleLiveController start() throws Throwable {
		this.instance.start(properties);
		return this.instance;
	}
}
