package org.xson.tangyuan.sql.xml.node;

import java.sql.SQLException;

import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的ProcedureNode
 */
public class InternalProcedureNode extends AbstractSqlNode {

	// private static Log log = LogFactory.getLog(InternalProcedureNode.class);

	public InternalProcedureNode(String dsKey, String rowCount, TangYuanNode sqlNode) {
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws SQLException {
		return true;
	}

}
