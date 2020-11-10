package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class TransGroupNode extends AbstractSqlNode {

	private static Log log = LogFactory.getLog(TransGroupNode.class);

	public TransGroupNode(XTransactionDefinition txDef, TangYuanNode sqlNode) {
		this.sqlNode = sqlNode;
		this.txDef = txDef;
	}

	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
		SqlServiceContext sqlContext = (SqlServiceContext) ac.getServiceContext(this.serviceType);

		try {
			log.info("start transGroup: " + this.txDef.getId());
			// 1. 开启事务, 这里只是创建事务
			sqlContext.beforeExecute(this, false);
			// 2. 执行服务
			sqlNode.execute(ac, arg, temp);
			sqlContext.afterExecute(this);
			// 3. 提交:确定的提交
			sqlContext.commit(true);
		} catch (Throwable e) {
			sqlContext.rollbackCurrentTransaction();
			log.error("transGroup exception", e);
		}
		return true;
	}

}
