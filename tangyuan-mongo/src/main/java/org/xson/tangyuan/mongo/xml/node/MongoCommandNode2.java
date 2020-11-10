package org.xson.tangyuan.mongo.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoCommandNode2 extends AbstractMongoNode {

	private static Log log = LogFactory.getLog(MongoCommandNode2.class);

	private MappingVo  resultMap;

	// 返回影响行数的key
	private String     rowCount;
	// 返回的自增key, 有可能是多个
	private String     incrementKey;

	public MongoCommandNode2(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode, CacheUseVo cacheUse, CacheCleanVo cacheClean, Class<?> resultType,
			MappingVo resultMap, String rowCount, String incrementKey, String desc, String[] groups) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.sqlNode = sqlNode;

		this.simple = true;

		this.cacheUse = cacheUse;
		this.cacheClean = cacheClean;

		this.resultType = resultType;
		this.resultMap = resultMap;

		this.rowCount = rowCount;
		this.incrementKey = incrementKey;

		this.desc = desc;
		this.groups = groups;
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
				ac.setResult(ir.getRowCount());
				return null;
			}

			XCO insertResult = new XCO();
			if (null != this.rowCount && null == this.incrementKey) {
				insertResult.setIntegerValue(this.rowCount, ir.getRowCount());
			} else {
				if (null != this.rowCount) {
					insertResult.setIntegerValue(this.rowCount, ir.getRowCount());
				}
				insertResult.setObjectValue(this.incrementKey, ir.getColumns());
			}

			ac.setResult(insertResult);
			return null;
		}

		// 7. 设置结果
		ac.setResult(result);
		return result;
	}

	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) throws Throwable {
	//
	//		MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);
	//
	//		// 1. cache使用
	//		if (null != cacheUse) {
	//			Object result = cacheUse.getObject(arg);
	//			if (null != result) {
	//				context.setResult(result);
	//				return true;
	//			}
	//		}
	//
	//		// 2. 清理和重置执行环境
	//		mongoContext.resetExecEnv();
	//
	//		long   startTime = System.currentTimeMillis();
	//		Object result    = null;
	//
	//		sqlNode.execute(context, arg); // 获取sql
	//		if (log.isInfoEnabled()) {
	//			log.info(mongoContext.getSql());
	//		}
	//
	//		result = mongoContext.executeCommand(this, arg, resultType, resultMap);
	//
	//		context.setResult(result);
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("mongo execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		if (null != cacheUse) {
	//			cacheUse.putObject(arg, context.getResult());
	//		}
	//		if (null != cacheClean) {
	//			cacheClean.removeObject(arg);
	//		}
	//
	//		return true;
	//	}
	//
	//	@Override
	//	public Class<?> getResultType() {
	//		if (null != this.resultType) {
	//			return this.resultType;
	//		}
	//
	//		if (null != this.resultMap) {
	//			return this.resultMap.getBeanClass();
	//		}
	//
	//		return resultType;
	//	}
	//
	//	@Override
	//	public Object getResult(ServiceContext context) {
	//		Class<?> resultType = getResultType();
	//		if (null == resultType || XCO.class == resultType || Map.class == resultType) {
	//			return super.getResult(context);
	//		}
	//		Object value = super.getResult(context);
	//		if (null == value) {
	//			return value;
	//		}
	//		// parent.isAssignableFrom(child)
	//		if (List.class.isAssignableFrom(value.getClass())) {
	//			return getResultBeans(context);
	//		} else {
	//			return getResultBean(context);
	//		}
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	public Object getResultBean(Object value) {
	//		if (null == resultMap) {
	//			if (XCO.class == resultType || Map.class == resultType) {
	//				return value;// 原始的
	//			} else {
	//				return OgnlBean.mapToBean((Map<String, Object>) value, resultType);
	//			}
	//		} else {
	//			Class<?> beanClass = resultMap.getBeanClass();
	//			if (null == beanClass && XCO.class != resultType && Map.class != resultType) {
	//				beanClass = resultType;
	//			}
	//			if (null != beanClass) {
	//				return OgnlBean.mapToBean((Map<String, Object>) value, beanClass);
	//			}
	//			return value;
	//		}
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	public Object getResultBeans(Object value) {
	//		if (null == resultMap) {
	//			if (XCO.class == resultType || Map.class == resultType) {
	//				// List<XCO>, List<Map>
	//				return value;
	//			} else {
	//				// List<Bean>, 最基本的bean:属性名和列名一直, 从数据库以Map接受方便
	//				List<Object>              result   = new ArrayList<Object>();
	//				List<Map<String, Object>> dataList = (List<Map<String, Object>>) value;
	//				int                       size     = dataList.size();
	//				for (int i = 0; i < size; i++) {
	//					Map<String, Object> data = dataList.get(i);
	//					result.add(OgnlBean.mapToBean(data, resultType));
	//				}
	//				return result;
	//			}
	//		} else {
	//			Class<?> beanClass = resultMap.getBeanClass();
	//			if (null == beanClass && XCO.class != resultType && Map.class != resultType) {
	//				beanClass = resultType;
	//			}
	//			// 在数据库查询的时候就需要做列的映射, 这里只做toBean(如果需要的话)的映射
	//			if (null != beanClass) {
	//				List<Object>              result   = new ArrayList<Object>();
	//				List<Map<String, Object>> dataList = (List<Map<String, Object>>) value;
	//				int                       size     = dataList.size();
	//				for (int i = 0; i < size; i++) {
	//					Map<String, Object> data = dataList.get(i);
	//					result.add(OgnlBean.mapToBean(data, beanClass));
	//				}
	//				return result;
	//			}
	//			// List<XCO>, List<Map>
	//			return value;
	//		}
	//	}
}
