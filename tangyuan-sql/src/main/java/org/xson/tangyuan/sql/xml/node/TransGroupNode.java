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

	/////////////////////////////////////////////////////////////////////////////////////

	//	public boolean execute(ServiceContext sc, Object arg, Object temp) throws Throwable {
	//		SqlServiceContext sqlContext         = (SqlServiceContext) sc.getSqlServiceContext();
	//
	//		boolean           createdTranscation = false;
	//		try {
	//			log.info("start transGroup: " + this.txDef.getId());
	//			// 1. 开启事务, 这里只是创建事务
	//			sqlContext.beforeExecute(this, false);
	//			createdTranscation = true;
	//			// 2. 执行服务
	//			sqlNode.execute(sc, arg, temp);
	//			// 3. 提交:确定的提交
	//			sqlContext.commit(true);
	//			createdTranscation = false;
	//			sqlContext.afterExecute(this);
	//		} catch (Throwable e) {
	//			if (createdTranscation) {
	//				// 只是回滚当前的事务
	//				sqlContext.rollbackCurrentTransaction();
	//			}
	//			log.error("transGroup exception", e);
	//		}
	//		return true;
	//	}

	//	@Override
	//	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
	//		SqlServiceContext sqlContext = (SqlServiceContext) serviceContext.getSqlServiceContext();
	//
	//		boolean createdTranscation = false;
	//		try {
	//			log.info("start transGroup: " + this.txDef.getId());
	//			// 1. 开启事务, 这里只是创建事务
	//			sqlContext.beforeExecute(this, false);
	//			createdTranscation = true;
	//			// 2. 执行服务
	//			sqlNode.execute(serviceContext, arg);
	//			// 3. 提交:确定的提交
	//			sqlContext.commit(true);
	//			createdTranscation = false;
	//			sqlContext.afterExecute(this);
	//		} catch (Throwable e) {
	//			if (createdTranscation) {
	//				// 只是回滚当前的事务
	//				sqlContext.rollbackCurrentTransaction();
	//			}
	//			log.error("transGroup exception", e);
	//		}
	//		return true;
	//	}
}
