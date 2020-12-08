package org.xson.tangyuan.hive.service.cmd;

import org.xson.tangyuan.hive.service.context.HiveServiceContext;
import org.xson.tangyuan.hive.transaction.XTransactionDefinition;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class HIVECommandContext extends ActuatorContext {

	private XTransactionDefinition txDef = null;

	public HIVECommandContext(XTransactionDefinition txDef) {
		this.txDef = txDef;
	}

	protected void begin() throws Throwable {
		HiveServiceContext sqlContext = (HiveServiceContext) getServiceContext(TangYuanServiceType.HIVE);
		sqlContext.beforeExecuteForCommandBegin(txDef);
	}

	protected void commitAndRollBack(Throwable ex) throws Throwable {
		HiveServiceContext sqlContext = (HiveServiceContext) getServiceContext(TangYuanServiceType.HIVE);
		Throwable          throwEx    = null;
		boolean            success    = false;
		try {
			success = sqlContext.commitAndRollBack(ex); // 这里也可能开启异常
		} catch (Throwable e) {
			throwEx = e;
		}
		// 处理后置事件和总计数
		this.sc = null;

		this.post(success);
		// 抛出新异常
		if (null != throwEx) {
			throw throwEx;
		}

	}

	public void onException(Object info) {
		HiveServiceContext sqlContext = (HiveServiceContext) getServiceContext(TangYuanServiceType.HIVE);
		sqlContext.onException(info);
	}
}
