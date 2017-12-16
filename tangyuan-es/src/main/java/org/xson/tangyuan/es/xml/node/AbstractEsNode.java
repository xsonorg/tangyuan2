package org.xson.tangyuan.es.xml.node;

import org.xson.tangyuan.es.ResultConverter;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public abstract class AbstractEsNode extends AbstractServiceNode {

	protected TangYuanNode		sqlNode		= null;

	protected String			dsKey		= null;
	protected boolean			simple		= true;

	protected ResultConverter	converter	= null;

	protected AbstractEsNode() {
		this.serviceType = TangYuanServiceType.ES;
	}

	public String getDsKey() {
		return dsKey;
	}

	public boolean isSimple() {
		return simple;
	}
}
