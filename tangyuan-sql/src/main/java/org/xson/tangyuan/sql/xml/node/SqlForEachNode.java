package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class SqlForEachNode extends ForEachNode {

	public SqlForEachNode(TangYuanNode sqlNode, Variable collection, String index, String open, String close, String separator) {
		this.sqlNode = sqlNode;
		this.collection = collection;
		this.index = index;
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

}
