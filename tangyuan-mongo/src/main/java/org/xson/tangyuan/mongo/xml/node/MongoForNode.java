package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;
import org.xson.tangyuan.xml.node.ForNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoForNode extends ForNode {

	public MongoForNode(TangYuanNode sqlNode, String index, Object start, Object end, String open, String close, String separator) {
		this.sqlNode = sqlNode;
		this.index = index;
		this.start = start;
		this.end = end;
		this.open = open;
		this.close = close;
		this.separator = separator;
	}

	protected void append(ActuatorContext ac, String str) {
		MongoServiceContext context = (MongoServiceContext) ac.getServiceContext(TangYuanServiceType.MONGO);
		if (null != str && str.length() > 0) {
			context.addSql(str);
		}
	}

}
