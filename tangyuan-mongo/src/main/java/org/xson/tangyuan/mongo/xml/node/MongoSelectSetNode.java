package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoSelectSetNode extends AbstractMongoNode {

	private static Log log       = LogFactory.getLog(MongoSelectSetNode.class);
	private MappingVo  resultMap = null;
	//	private Integer    fetchSize = null;

	public MongoSelectSetNode(String id, String ns, String serviceKey, Class<?> resultType, MappingVo resultMap, String dsKey, Integer fetchSize, TangYuanNode sqlNode,
			CacheUseVo cacheUse, String desc, String[] groups) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.resultType = resultType;
		this.resultMap = resultMap;

		this.dsKey = dsKey;
		//		this.fetchSize = fetchSize;
		this.sqlNode = sqlNode;
		this.cacheUse = cacheUse;

		this.simple = true;

		this.desc = desc;
		this.groups = groups;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, MongoServiceContext context, Object temp) throws Throwable {
		Object result = context.executeSelectSetListXCO(this, resultMap, temp);
		// 7. 设置结果
		ac.setResult(result);
		return result;
	}

	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) throws Throwable {
	//		MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);
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
	//		long   startTime = System.currentTimeMillis();
	//		Object result    = null;
	//
	//		sqlNode.execute(context, arg); // 获取sql
	//		if (log.isInfoEnabled()) {
	//			log.info(mongoContext.getSql());
	//		}
	//		if (XCO.class == resultType) {
	//			result = mongoContext.executeSelectSetListXCO(this, resultMap, fetchSize, arg);
	//		} else {
	//			result = mongoContext.executeSelectSetListMap(this, resultMap, fetchSize, arg);
	//		}
	//		context.setResult(result);
	//
	//		if (log.isInfoEnabled()) {
	//			log.info("mongo execution time: " + getSlowServiceLog(startTime));
	//		}
	//
	//		if (null != cacheUse) {
	//			cacheUse.putObject(arg, result);
	//		}
	//
	//		return true;
	//	}

	//	@Override
	//	public Class<?> getResultType() {
	//		if (null != this.resultType) {
	//			return this.resultType;
	//		}
	//		return this.resultMap.getBeanClass();
	//	}

	//	@Override
	//	@SuppressWarnings("unchecked")
	//	public Object getResult(ServiceContext context) {
	//		Object value = context.getResult();
	//		context.setResult(null);
	//		if (null == value) {
	//			return value;
	//		}
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
