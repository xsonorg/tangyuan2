package org.xson.tangyuan.hive.xml.node;

import org.xson.tangyuan.hive.service.context.HiveServiceContext;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;
import org.xson.tangyuan.xml.node.ForNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class HiveForNode extends ForNode {

	public HiveForNode(TangYuanNode sqlNode, String index, Object start, Object end, String open, String close, String separator) {
		this.sqlNode = sqlNode;
		this.index = index;
		this.start = start;
		this.end = end;
		this.open = open;
		this.close = close;
		this.separator = separator;
	}

	protected void append(ActuatorContext ac, String str) {
		HiveServiceContext serviceContext = (HiveServiceContext) ac.getServiceContext(TangYuanServiceType.HIVE);
		if (null != str && str.length() > 0) {
			serviceContext.addSql(str);
		}
	}

}
