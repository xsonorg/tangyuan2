package org.xson.tangyuan.sql.xml.node;

import java.sql.SQLException;

import org.xson.tangyuan.executor.ServiceContext;

public class ProcedureNode extends AbstractSqlNode {

	public ProcedureNode() {

	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws SQLException {
		return true;
	}

}
