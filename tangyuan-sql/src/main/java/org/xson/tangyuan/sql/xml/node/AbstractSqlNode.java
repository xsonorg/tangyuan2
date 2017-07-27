package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public abstract class AbstractSqlNode extends AbstractServiceNode {

	protected TangYuanNode				sqlNode;

	protected String					dsKey;

	protected XTransactionDefinition	txDef;

	protected boolean					simple;

	protected AbstractSqlNode() {
		this.serviceType = TangYuanServiceType.SQL;
	}

	public XTransactionDefinition getTxDef() {
		return txDef;
	}

	public String getDsKey() {
		return dsKey;
	}

	public boolean isSimple() {
		return simple;
	}

	// public Object getResult(SqlServiceContext context, Map<String, Object> arg) {
	// Object result = context.getResult();
	// context.setResult(null);// 清理
	// return result;
	// }

}
