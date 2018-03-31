package org.xson.tangyuan.mongo.xml.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.mongo.executor.MongoServiceContext;
import org.xson.tangyuan.ognl.bean.OgnlBean;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoCommandNode extends AbstractMongoNode {

	private static Log		log	= LogFactory.getLog(MongoCommandNode.class);

	private MappingVo		resultMap;

	private CacheUseVo		cacheUse;

	private CacheCleanVo	cacheClean;

	public MongoCommandNode(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode, CacheUseVo cacheUse, CacheCleanVo cacheClean,
			Class<?> resultType, MappingVo resultMap) {
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
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {

		MongoServiceContext mongoContext = (MongoServiceContext) context.getServiceContext(TangYuanServiceType.MONGO);

		// 1. cache使用
		if (null != cacheUse) {
			Object result = cacheUse.getObject(arg);
			if (null != result) {
				context.setResult(result);
				return true;
			}
		}

		// 2. 清理和重置执行环境
		mongoContext.resetExecEnv();

		long startTime = System.currentTimeMillis();
		Object result = null;

		sqlNode.execute(context, arg); // 获取sql
		if (log.isInfoEnabled()) {
			log.info(mongoContext.getSql());
		}

		result = mongoContext.executeCommand(this, arg, resultType, resultMap);

		context.setResult(result);

		if (log.isInfoEnabled()) {
			log.info("mongo execution time: " + getSlowServiceLog(startTime));
		}

		if (null != cacheUse) {
			cacheUse.putObject(arg, context.getResult());
		}
		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		return true;
	}

	@Override
	public Class<?> getResultType() {
		if (null != this.resultType) {
			return this.resultType;
		}

		if (null != this.resultMap) {
			return this.resultMap.getBeanClass();
		}

		return resultType;
	}

	@Override
	public Object getResult(ServiceContext context) {
		Class<?> resultType = getResultType();
		if (null == resultType || XCO.class == resultType || Map.class == resultType) {
			return super.getResult(context);
		}
		Object value = super.getResult(context);
		if (null == value) {
			return value;
		}
		// parent.isAssignableFrom(child)
		if (List.class.isAssignableFrom(value.getClass())) {
			return getResultBeans(context);
		} else {
			return getResultBean(context);
		}
	}

	@SuppressWarnings("unchecked")
	public Object getResultBean(Object value) {
		if (null == resultMap) {
			if (XCO.class == resultType || Map.class == resultType) {
				return value;// 原始的
			} else {
				return OgnlBean.mapToBean((Map<String, Object>) value, resultType);
			}
		} else {
			Class<?> beanClass = resultMap.getBeanClass();
			if (null == beanClass && XCO.class != resultType && Map.class != resultType) {
				beanClass = resultType;
			}
			if (null != beanClass) {
				return OgnlBean.mapToBean((Map<String, Object>) value, beanClass);
			}
			return value;
		}
	}

	@SuppressWarnings("unchecked")
	public Object getResultBeans(Object value) {
		if (null == resultMap) {
			if (XCO.class == resultType || Map.class == resultType) {
				// List<XCO>, List<Map>
				return value;
			} else {
				// List<Bean>, 最基本的bean:属性名和列名一直, 从数据库以Map接受方便
				List<Object> result = new ArrayList<Object>();
				List<Map<String, Object>> dataList = (List<Map<String, Object>>) value;
				int size = dataList.size();
				for (int i = 0; i < size; i++) {
					Map<String, Object> data = dataList.get(i);
					result.add(OgnlBean.mapToBean(data, resultType));
				}
				return result;
			}
		} else {
			Class<?> beanClass = resultMap.getBeanClass();
			if (null == beanClass && XCO.class != resultType && Map.class != resultType) {
				beanClass = resultType;
			}
			// 在数据库查询的时候就需要做列的映射, 这里只做toBean(如果需要的话)的映射
			if (null != beanClass) {
				List<Object> result = new ArrayList<Object>();
				List<Map<String, Object>> dataList = (List<Map<String, Object>>) value;
				int size = dataList.size();
				for (int i = 0; i < size; i++) {
					Map<String, Object> data = dataList.get(i);
					result.add(OgnlBean.mapToBean(data, beanClass));
				}
				return result;
			}
			// List<XCO>, List<Map>
			return value;
		}
	}
}
