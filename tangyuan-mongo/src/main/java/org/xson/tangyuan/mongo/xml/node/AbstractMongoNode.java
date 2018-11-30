package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public abstract class AbstractMongoNode extends AbstractServiceNode {

	protected TangYuanNode	sqlNode;

	protected String		dsKey;

	protected boolean		simple;

	protected AbstractMongoNode() {
		this.serviceType = TangYuanServiceType.MONGO;
	}

	public String getDsKey() {
		return dsKey;
	}

	public boolean isSimple() {
		return simple;
	}

	// protected boolean isTraceCommand() {
	// return RuntimeContext.isTraceCommand(TrackingManager.SERVICE_TYPE_MONGO);
	// }
}
