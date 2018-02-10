package org.xson.tangyuan.bootstrap;

import java.util.Map;

public class StartupAndShutdownVo {

	private StartupAndShutdownHandler	handler;

	private Map<String, String>			properties;

	private String						className;

	public StartupAndShutdownVo(StartupAndShutdownHandler handler, Map<String, String> properties, String className) {
		this.handler = handler;
		this.properties = properties;
		this.className = className;
	}

	public StartupAndShutdownHandler getHandler() {
		return handler;
	}

	public String getClassName() {
		return className;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

}
