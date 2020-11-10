package org.xson.tangyuan.xml.node;

/**
 * 空服务
 */
public class EmptyServiceNode extends AbstractServiceNode {

	public final static EmptyServiceNode instance = new EmptyServiceNode();

	private EmptyServiceNode() {
		this.id = "NULL";
		this.ns = "NULL";
		this.serviceKey = "NULL/NULL";
		this.serviceType = TangYuanServiceType.NO;
	}

}
