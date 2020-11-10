package org.xson.tangyuan.manager;

import org.xson.common.object.XCO;

/**
 * 承载Launcher之后的数据
 */
public class ManagerLauncherContext {

	private String       nodeIp       = null;
	private String       nodeName     = null;
	private String       nodePort     = null;
	private String       appName      = null;
	private XCO          loginResult  = null;
	private LocalStorage localStorage = null;

	public ManagerLauncherContext(String nodeIp, String nodeName, String nodePort, String appName, XCO loginResult,
			LocalStorage localStorage) {
		this.nodeIp = nodeIp;
		this.nodeName = nodeName;
		this.nodePort = nodePort;
		this.appName = appName;
		this.loginResult = loginResult;
		this.localStorage = localStorage;
	}

	public String getNodeIp() {
		return nodeIp;
	}

	public String getNodeName() {
		return nodeName;
	}

	public String getNodePort() {
		return nodePort;
	}

	public String getAppName() {
		return appName;
	}

	public XCO getLoginResult() {
		return loginResult;
	}

	public LocalStorage getLocalStorage() {
		return localStorage;
	}

	//	public void setLocalStorage(LocalStorage localStorage) {
	//		this.localStorage = localStorage;
	//	}
	//
	//	public void setLoginResult(XCO loginResult) {
	//		this.loginResult = loginResult;
	//	}
	//
	//	public void setAppInfo(String nodeIp, String nodeName, String nodePort, String appName) {
	//		this.nodeIp = nodeIp;
	//		this.nodeName = nodeName;
	//		this.nodePort = nodePort;
	//		this.appName = appName;
	//	}
}
