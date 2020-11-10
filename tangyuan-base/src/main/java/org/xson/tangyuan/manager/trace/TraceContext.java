package org.xson.tangyuan.manager.trace;

import org.xson.common.object.XCO;

/**
 * 临时上下文
 */
public class TraceContext {

	private XCO parent;

	private XCO current;

	public TraceContext(XCO parent, XCO current) {
		this.parent = parent;
		this.current = current;
	}

	public XCO getCurrent() {
		return current;
	}

	public XCO getParent() {
		return parent;
	}

	//	private String	component;
	//	
	//	public void setComponent(String	component){
	//		this.component = component;
	//	}
	//	public void setTracking(XCO parent, XCO current) {
	//		this.trackingParent = parent;
	//		this.trackingCurrent = current;
	//	}

}
