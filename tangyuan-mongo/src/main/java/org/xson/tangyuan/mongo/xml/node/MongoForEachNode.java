package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoForEachNode extends ForEachNode {

	public MongoForEachNode(TangYuanNode sqlNode, Variable collection, String index, String open, String close, String separator) {
		this.sqlNode = sqlNode;
		this.collection = collection;
		this.index = index;
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
