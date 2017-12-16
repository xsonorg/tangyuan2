package org.xson.tangyuan.hbase.xml.node;

import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.hbase.executor.HBaseServiceContext;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class HBaseForEachNode extends ForEachNode {

	public HBaseForEachNode(TangYuanNode sqlNode, Variable collection, String index, String open, String close, String separator) {
		this.sqlNode = sqlNode;
		this.collection = collection;
		this.index = index;
		this.open = open;
		this.close = close;
		this.separator = separator;
	}

	protected void append(ServiceContext context, String str) {
		if (null != str && str.length() > 0) {
			HBaseServiceContext xServiceContext = (HBaseServiceContext) context.getServiceContext(TangYuanServiceType.HBASE);
			xServiceContext.addSql(str);
		}
	}
}
