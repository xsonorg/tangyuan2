package org.xson.tangyuan.trace;

import java.util.Properties;

public class TrackingConfig {

	private Properties properties = null;

	public TrackingConfig(Properties p) {
		this.properties = p;
	}

	public void init() throws Throwable {
		// TODO 忽略的设置
	}

	public Properties getProperties() {
		return properties;
	}
}
