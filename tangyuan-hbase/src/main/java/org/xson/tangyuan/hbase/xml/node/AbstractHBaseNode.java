package org.xson.tangyuan.hbase.xml.node;

import org.xson.tangyuan.runtime.RuntimeContext;
import org.xson.tangyuan.runtime.trace.TrackingManager;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public abstract class AbstractHBaseNode extends AbstractServiceNode {

	public enum ResultStruct {
		ROW_CELL, ROW_OBJECT
	}

	protected TangYuanNode	sqlNode	= null;

	protected String		dsKey	= null;
	protected boolean		simple	= true;

	protected AbstractHBaseNode() {
		this.serviceType = TangYuanServiceType.HBASE;
	}

	public String getDsKey() {
		return dsKey;
	}

	public boolean isSimple() {
		return simple;
	}

	protected void appendTracking(Object command) {
		RuntimeContext.appendTracking(TrackingManager.SERVICE_TYPE_HBASE, command);
	}
}
