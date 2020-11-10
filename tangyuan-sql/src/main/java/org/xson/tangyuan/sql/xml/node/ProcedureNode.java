package org.xson.tangyuan.sql.xml.node;

import java.sql.SQLException;

import org.xson.tangyuan.service.ActuatorContext;

public class ProcedureNode extends AbstractSqlNode {

	public ProcedureNode() {

	}

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws SQLException {
		return true;
	}

}
