package org.xson.tangyuan.manager.app;

import org.xson.common.object.XCO;

public class AppManagerVo {

	private boolean running = true;

	public void init(XCO data) {
		if (null == data) {
			return;
		}
		String state = data.getStringValue("state");
		if (null == state || "RUNNING".equalsIgnoreCase(state)) {
			this.running = true;
		}
		this.running = false;
	}

	public boolean isAccessed(String service) {
		return this.running;
	}
}
