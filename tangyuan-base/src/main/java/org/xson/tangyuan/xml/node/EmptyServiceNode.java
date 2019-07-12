package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.executor.ServiceContext;

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

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		return false;
	}

}
