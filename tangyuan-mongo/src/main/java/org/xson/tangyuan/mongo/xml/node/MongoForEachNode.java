package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.mongo.executor.MongoServiceContext;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.TangYuanNode;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class MongoForEachNode extends ForEachNode {

	public MongoForEachNode(TangYuanNode sqlNode, Variable collection, String index, String open, String close, String separator) {
		this.sqlNode = sqlNode;
		this.collection = collection;
		this.index = index;
		this.open = open;
		this.close = close;
		this.separator = separator;
	}

	protected void append(ServiceContext context, String str) {
		if (null != str && str.length() > 0) {
			MongoServiceContext mongoServiceContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);
			mongoServiceContext.addSql(str);
		}
	}
}
