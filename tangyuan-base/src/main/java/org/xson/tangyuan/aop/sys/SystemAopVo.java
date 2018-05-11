package org.xson.tangyuan.aop.sys;

import java.util.Map;

public class SystemAopVo {

	private SystemAopHandler	handler;

	private Map<String, String>	properties;

	private String				className;

	public SystemAopVo(SystemAopHandler handler, Map<String, String> properties, String className) {
		this.handler = handler;
		this.properties = properties;
		this.className = className;
	}

	public SystemAopHandler getHandler() {
		return handler;
	}

	public String getClassName() {
		return className;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

}
