package org.xson.tangyuan.es.xml.node;

import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.EsServiceContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;
import org.xson.tangyuan.xml.node.ForNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class EsForNode extends ForNode {

	public EsForNode(TangYuanNode sqlNode, String index, Object start, Object end, String open, String close, String separator) {
		this.sqlNode = sqlNode;
		this.index = index;
		this.start = start;
		this.end = end;
		this.open = open;
		this.close = close;
		this.separator = separator;
	}

	protected void append(ActuatorContext ac, String str) {
		EsServiceContext context = (EsServiceContext) ac.getServiceContext(TangYuanServiceType.ES);
		if (null != str && str.length() > 0) {
			context.addSql(str);
		}
	}

}
