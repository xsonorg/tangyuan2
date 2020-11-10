package org.xson.tangyuan.mongo.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoInsertNode2 extends AbstractMongoNode {

	private static Log log = LogFactory.getLog(MongoInsertNode2.class);

	// 返回影响行数的key
	private String     rowCount;
	// 返回的自增key, 有可能是多个
	private String     incrementKey;

	public MongoInsertNode2(String id, String ns, String serviceKey, String rowCount, String incrementKey, String dsKey, TangYuanNode sqlNode, CacheCleanVo cacheClean, String desc,
			String[] groups) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.rowCount = rowCount;
		this.incrementKey = incrementKey;

		this.dsKey = dsKey;
		this.sqlNode = sqlNode;

		this.simple = true;

		this.cacheClean = cacheClean;

		this.desc = desc;
		this.groups = groups;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, MongoServiceContext context, Object acArg) throws Throwable {

		InsertReturn ir = (InsertReturn) context.executeInsert(this, acArg);

		if (null == this.rowCount && null == this.incrementKey) {
			ac.setResult(ir.getRowCount());
			return null;
		}

		XCO result = new XCO();
		if (null != this.rowCount && null == this.incrementKey) {
			result.setIntegerValue(this.rowCount, ir.getRowCount());
		} else {
			if (null != this.rowCount) {
				result.setIntegerValue(this.rowCount, ir.getRowCount());
			}
			result.setObjectValue(this.incrementKey, ir.getColumns());
		}

		// 7. 设置结果
		ac.setResult(result);
		return null;

		//		Object       result = context.executeInsert(this, temp);
		// 7. 设置结果
		//		ac.setResult(result);

		//		if (result instanceof InsertReturn) {
		//			ac.setResult(((InsertReturn) result).getColumns());
		//		} else {
		//			ac.setResult(result);
		//		}
		//		return result;
	}

	// // // // // // // // // // // // // // // // // // // // // // // // // // // 

	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) throws Throwable {
	//
	//		MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);
	//
	//		// 2. 清理和重置执行环境
	//		mongoContext.resetExecEnv();
	//
	//		long startTime = System.currentTimeMillis();
	//		sqlNode.execute(context, arg); // 获取sql
	//		if (log.isInfoEnabled()) {
	//			log.info(mongoContext.getSql());
	//		}
	//
	//		Object result = mongoContext.executeInsert(this, arg);
	//		context.setResult(result);
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
