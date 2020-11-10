package org.xson.tangyuan.sql.service.cmd;

import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class SQLCommandContext extends ActuatorContext {

	private XTransactionDefinition txDef = null;

	public SQLCommandContext(XTransactionDefinition txDef) {
		this.txDef = txDef;
	}

	protected void begin() throws Throwable {
		SqlServiceContext sqlContext = (SqlServiceContext) getServiceContext(TangYuanServiceType.SQL);
		sqlContext.beforeExecuteForCommandBegin(txDef);
	}

	protected void commitAndRollBack(Throwable ex) throws Throwable {
		SqlServiceContext sqlContext = (SqlServiceContext) getServiceContext(TangYuanServiceType.SQL);
		Throwable         throwEx    = null;
		boolean           success    = false;
		try {
			success = sqlContext.commitAndRollBack(ex); // 这里也可能开启异常
		} catch (Throwable e) {
			throwEx = e;
		}
		// 处理后置事件和总计数
		this.sc = null;
		//		if (success) {
		//			finish();
		//		} else {
		//			finishOnException();
		//		}
		this.post(success);
		// 抛出新异常
		if (null != throwEx) {
			throw throwEx;
		}

	}

	public void onException(Object info) {
		SqlServiceContext sqlContext = (SqlServiceContext) getServiceContext(TangYuanServiceType.SQL);
		sqlContext.onException(info);
	}
}
