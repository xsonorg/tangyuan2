package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;

public class SegmentNode implements TangYuanNode {

	private XmlNodeWrapper node;

	public SegmentNode(XmlNodeWrapper node) {
		this.node = node;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		return false;
	}

	public XmlNodeWrapper getNode() {
		return node;
	}
}
