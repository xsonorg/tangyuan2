package org.xson.tangyuan.hive.xml.node;

import org.xson.tangyuan.hive.transaction.XTransactionDefinition;
import org.xson.tangyuan.runtime.RuntimeContext;
import org.xson.tangyuan.runtime.trace.TrackingManager;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public abstract class AbstractSqlNode extends AbstractServiceNode {

	protected TangYuanNode				sqlNode;

	protected String					dsKey;

	protected XTransactionDefinition	txDef;

	protected boolean					simple;

	protected AbstractSqlNode() {
		this.serviceType = TangYuanServiceType.HIVE;
	}

	public XTransactionDefinition getTxDef() {
		return txDef;
	}

	public String getDsKey() {
		return dsKey;
	}

	public boolean isSimple() {
		return simple;
	}

	protected boolean isTraceCommand() {
		return RuntimeContext.isTraceCommand(TrackingManager.SERVICE_TYPE_HIVE);
	}
}
