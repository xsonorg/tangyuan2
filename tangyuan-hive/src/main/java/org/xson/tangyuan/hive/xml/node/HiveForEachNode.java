package org.xson.tangyuan.hive.xml.node;

import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.hive.executor.HiveServiceContext;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.TangYuanNode;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class HiveForEachNode extends ForEachNode {

	public HiveForEachNode(TangYuanNode sqlNode, Variable collection, String index, String open, String close, String separator, Object start,
			Object end, Object pLen, boolean ignoreIOOB, int indexMode) {
		this.sqlNode = sqlNode;
		this.collection = collection;
		this.index = index;
		this.open = open;
		this.close = close;
		this.separator = separator;

		this.start = start;
		this.end = end;
		this.pLen = pLen;
		this.ignoreIOOB = ignoreIOOB;
		this.indexMode = indexMode;
	}

	protected void append(ServiceContext serviceContext, String str) {

		HiveServiceContext sqlContext = (HiveServiceContext) serviceContext.getServiceContext(TangYuanServiceType.HIVE);

		if (null != str && str.length() > 0) {
			sqlContext.addSql(str);
		}
	}
}
