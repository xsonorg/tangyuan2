package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的InsertNode
 */
public class InternalMongoInsertNode extends AbstractMongoNode {

	private static Log log = LogFactory.getLog(InternalMongoInsertNode.class);

	// 返回影响行数的key
	//	private String     rowCount;
	// 返回的自增key, 有可能是多个
	private String     incrementKey;

	public InternalMongoInsertNode(String dsKey, String rowCount, String incrementKey, TangYuanNode sqlNode, CacheCleanVo cacheClean) {
		this.dsKey = dsKey;

		//		this.rowCount = rowCount;
		this.incrementKey = incrementKey;

		this.sqlNode = sqlNode;
		this.simple = false;
		this.cacheClean = cacheClean;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, MongoServiceContext context, Object acArg) throws Throwable {
		InsertReturn ir = (InsertReturn) context.executeInsert(this, acArg);
		if (null != this.incrementKey) {
			Ognl.setValue(acArg, this.incrementKey, ir.getColumns());
		}
		return null;

	}

	//	@Override
	//	protected Object executeSql(ActuatorContext ac, MongoServiceContext context, Object acArg) throws Throwable {
	//
	//		InsertReturn ir = (InsertReturn) context.executeInsert(this, acArg);
	//
	//		if (null == this.rowCount && null == this.incrementKey) {
	//			// do nothing
	//		} else if (null != this.rowCount && null == this.incrementKey) {
	//			Ognl.setValue(acArg, this.rowCount, ir.getRowCount());
	//		} else {
	//			if (null != this.rowCount) {
	//				Ognl.setValue(acArg, this.rowCount, ir.getRowCount());
	//			}
	//			Ognl.setValue(acArg, this.incrementKey, ir.getColumns());
	//		}
	//		return null;
	//
	//	}

	// 这里是返回_id
	//	private String     resultKey;

	//		Object result = context.executeInsert(this, temp);
	//		// 7. 设置结果
	//		if (null != this.resultKey) {
	//			Ognl.setValue(temp, this.resultKey, result);
	//		}
	//		return result;

	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) throws Throwable {
	//
	//		MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);
	//
	//		mongoContext.resetExecEnv();
	//
	//		long startTime = System.currentTimeMillis();
	//		sqlNode.execute(context, arg); // 获取sql
	//
	//		if (log.isInfoEnabled()) {
	//			log.info(mongoContext.getSql());
	//		}
	//
	//		Object result = mongoContext.executeInsert(this, arg);
	//		if (null != this.resultKey) {
	//			Ognl.setValue(arg, this.resultKey, result);
	//		}
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("mongo execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		if (null != cacheClean) {
	//			cacheClean.removeObject(arg);
	//		}
	//
	//		return true;
	//	}

}
