package org.xson.tangyuan.mongo.xml.node;

import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.mongo.executor.MongoServiceContext;
import org.xson.tangyuan.ognl.bean.OgnlBean;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoSelectOneNode extends AbstractMongoNode {

	private static Log	log			= LogFactory.getLog(MongoSelectOneNode.class);

	private MappingVo	resultMap	= null;

	private CacheUseVo	cacheUse;

	public MongoSelectOneNode(String id, String ns, String serviceKey, Class<?> resultType, MappingVo resultMap, String dsKey, TangYuanNode sqlNode,
			CacheUseVo cacheUse) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.resultType = resultType;
		this.resultMap = resultMap;

		this.dsKey = dsKey;
		this.sqlNode = sqlNode;

		this.cacheUse = cacheUse;

		this.simple = true;
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
		if (XCO.class == resultType) {
			result = mongoContext.executeSelectOneXCO(this, this.resultMap, null, arg);
		} else {
			result = mongoContext.executeSelectOneMap(this, this.resultMap, null, arg);
		}
		context.setResult(result);

		if (log.isInfoEnabled()) {
			log.info("mongo execution time: " + getSlowServiceLog(startTime));
		}

		if (null != cacheUse) {
			cacheUse.putObject(arg, result);
		}

		return true;
	}

	@Override
	public Class<?> getResultType() {
		if (null != this.resultType) {
			return this.resultType;
		}
		return this.resultMap.getBeanClass();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object getResult(ServiceContext context) {
		Object value = context.getResult();
		context.setResult(null);
		if (null == value) {
			return value;
		}
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
}
