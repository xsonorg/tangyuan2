package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;
import org.xson.tangyuan.xml.node.ForNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class SqlForNode extends ForNode {

	public SqlForNode(TangYuanNode sqlNode, String index, Object start, Object end, String open, String close, String separator) {
		this.sqlNode = sqlNode;
		this.index = index;
		this.start = start;
		this.end = end;
		this.open = open;
		this.close = close;
		this.separator = separator;
	}

	protected void append(ActuatorContext ac, String str) {
		SqlServiceContext sqlContext = (SqlServiceContext) ac.getServiceContext(TangYuanServiceType.SQL);
		if (null != str && str.length() > 0) {
			sqlContext.addSql(str);
		}
	}

	// if (null != str && str.length() > 0) {
	// context.getSqlServiceContext().addSql(str);
	// }
	//		SqlServiceContext sqlContext = (SqlServiceContext) serviceContext.getSqlServiceContext();

	//	public SqlForEachNode(TangYuanNode sqlNode, Variable collection, String index, String open, String close, String separator, Object start,
	//			Object end, Object pLen, boolean ignoreIOOB, int indexMode) {
	//		this.sqlNode = sqlNode;
	//		this.collection = collection;
	//		this.index = index;
	//		this.open = open;
	//		this.close = close;
	//		this.separator = separator;
	//
	//		this.start = start;
	//		this.end = end;
	//		this.pLen = pLen;
	//		this.ignoreIOOB = ignoreIOOB;
	//		this.indexMode = indexMode;
	//	}
}
