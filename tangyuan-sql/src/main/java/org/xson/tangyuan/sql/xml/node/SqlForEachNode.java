package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.sql.executor.SqlServiceContext;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class SqlForEachNode extends ForEachNode {

	// public SqlForEachNode(TangYuanNode sqlNode, Variable collection, String index, String open, String close, String separator) {
	// this.sqlNode = sqlNode;
	// this.collection = collection;
	// this.index = index;
	// this.open = open;
	// this.close = close;
	// this.separator = separator;
	// }

	public SqlForEachNode(TangYuanNode sqlNode, Variable collection, String index, String open, String close, String separator, Object start,
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
		// if (null != str && str.length() > 0) {
		// context.getSqlServiceContext().addSql(str);
		// }

		SqlServiceContext sqlContext = (SqlServiceContext) serviceContext.getSqlServiceContext();
		if (null != str && str.length() > 0) {
			sqlContext.addSql(str);
		}
	}
}
