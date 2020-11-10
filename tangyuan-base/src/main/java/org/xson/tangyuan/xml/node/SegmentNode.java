package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;

public class SegmentNode implements TangYuanNode {

	private XmlNodeWrapper node;

	public SegmentNode(XmlNodeWrapper node) {
		this.node = node;
	}

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
		return false;
	}

	public XmlNodeWrapper getNode() {
		return node;
	}
}
