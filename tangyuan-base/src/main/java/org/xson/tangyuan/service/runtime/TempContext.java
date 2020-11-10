package org.xson.tangyuan.service.runtime;

import org.xson.common.object.XCO;

/**
 * 临时上下文
 */
public class TempContext {

	private XCO		trackingParent;

	private XCO		trackingCurrent;

	private String	component;
	
	public void setComponent(String	component){
		this.component = component;
	}

	public void setTracking(XCO parent, XCO current) {
		this.trackingParent = parent;
		this.trackingCurrent = current;
	}

	public XCO getTrackingCurrent() {
		return trackingCurrent;
	}

	public XCO getTrackingParent() {
		return trackingParent;
	}
	
	public String getComponent() {
		return component;
	}
}
