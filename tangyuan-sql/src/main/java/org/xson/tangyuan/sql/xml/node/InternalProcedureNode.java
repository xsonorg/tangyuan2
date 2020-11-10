package org.xson.tangyuan.sql.xml.node;

import java.sql.SQLException;

import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的ProcedureNode
 */
public class InternalProcedureNode extends AbstractSqlNode {

	public InternalProcedureNode(String dsKey, String rowCount, TangYuanNode sqlNode) {
	}

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws SQLException {
		return true;
	}

}
