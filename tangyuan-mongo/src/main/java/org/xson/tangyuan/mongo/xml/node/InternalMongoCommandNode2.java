package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的UpdateNode
 */
public class InternalMongoCommandNode2 extends AbstractMongoNode {

	private static Log log          = LogFactory.getLog(InternalMongoCommandNode2.class);

	private MappingVo  resultMap    = null;

	// 返回结果的key
	private String     resultKey    = null;
	// 如果是insert操作,对应影响行数
	private String     rowCount     = null;
	// 返回的自增key, 有可能是多个
	private String     incrementKey = null;

	public InternalMongoCommandNode2(String dsKey, String resultKey, String rowCount, String incrementKey, TangYuanNode sqlNode, Class<?> resultType, MappingVo resultMap,
			CacheUseVo cacheUse, CacheCleanVo cacheClean) {
		this.dsKey = dsKey;

		this.resultKey = resultKey;
		this.rowCount = rowCount;
		this.incrementKey = incrementKey;

		this.sqlNode = sqlNode;
		this.resultType = resultType;
		this.resultMap = resultMap;
		this.simple = false;
		this.cacheUse = cacheUse;
		this.cacheClean = cacheClean;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, MongoServiceContext context, Object acArg) throws Throwable {
		Object result = context.executeCommand(this, acArg, resultType, resultMap);

		if (result instanceof InsertReturn) {

			InsertReturn ir = (InsertReturn) result;

			if (null == this.rowCount && null == this.incrementKey) {
				// do nothing
			} else if (null != this.rowCount && null == this.incrementKey) {
				Ognl.setValue(acArg, this.rowCount, ir.getRowCount());
			} else {
				if (null != this.rowCount) {
					Ognl.setValue(acArg, this.rowCount, ir.getRowCount());
				}
				Ognl.setValue(acArg, this.incrementKey, ir.getColumns());
			}

			return null;
		}

		// 7. 设置结果
		if (null != this.resultKey) {
			Ognl.setValue(acArg, this.resultKey, result);
		}
		return result;
	}

	//////////////////////////////////////////////////////////////////////////

	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) throws Throwable {
	//
	//		MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);
	//
	//		if (null != cacheUse) {
	//			Object result = cacheUse.getObject(arg);
	//			if (null != result) {
	//				context.setResult(result);
	//				return true;
	//			}
	//		}
	//
	//		mongoContext.resetExecEnv();
	//
	//		long startTime = System.currentTimeMillis();
	//		sqlNode.execute(context, arg); // 获取sql
	//		if (log.isInfoEnabled()) {
	//			log.info(mongoContext.getSql());
	//		}
	//
	//		Object result = mongoContext.executeCommand(this, arg, resultType, null);
	//
	//		if (null != this.resultKey) {
	//			Ognl.setValue(arg, this.resultKey, result);
	//		}
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("mongo execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		if (null != cacheUse) {
	//			cacheUse.putObject(arg, result);
	//		}
	//
	//		if (null != cacheClean) {
	//			cacheClean.removeObject(arg);
	//		}
	//
	//		return true;
	//	}

}
