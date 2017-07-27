package org.xson.tangyuan.sql.xml.node;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.sql.executor.SqlServiceContext;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class TransGroupNode extends AbstractSqlNode {

	private static Log log = LogFactory.getLog(TransGroupNode.class);

	public TransGroupNode(XTransactionDefinition txDef, TangYuanNode sqlNode) {
		this.sqlNode = sqlNode;
		this.txDef = txDef;
	}

	@Override
	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
		SqlServiceContext sqlContext = (SqlServiceContext) serviceContext.getSqlServiceContext();

		boolean createdTranscation = false;
		try {
			log.info("start transGroup: " + this.txDef.getId());
			// 1. 开启事务, 这里只是创建事务
			sqlContext.beforeExecute(this, false);
			createdTranscation = true;
			// 2. 执行服务
			sqlNode.execute(serviceContext, arg);
			// 3. 提交:确定的提交
			sqlContext.commit(true);
			createdTranscation = false;
			sqlContext.afterExecute(this);
		} catch (Throwable e) {
			if (createdTranscation) {
				// 只是回滚当前的事务
				sqlContext.rollbackCurrentTransaction();
			}
			log.error("transGroup exception", e);
		}
		return true;
	}
}
