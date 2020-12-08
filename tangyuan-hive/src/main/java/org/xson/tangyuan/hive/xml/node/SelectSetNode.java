package org.xson.tangyuan.hive.xml.node;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.hive.service.cmd.HIVECommandContext;
import org.xson.tangyuan.hive.service.context.HiveServiceContext;
import org.xson.tangyuan.hive.transaction.XTransactionDefinition;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class SelectSetNode extends AbstractHiveNode {

	private static Log log       = LogFactory.getLog(SelectSetNode.class);

	private MappingVo  resultMap = null;
	private Integer    fetchSize = null;

	public SelectSetNode(String id, String ns, String serviceKey, Class<?> resultType, MappingVo resultMap, String dsKey, Integer fetchSize, XTransactionDefinition txDef,
			TangYuanNode sqlNode, CacheUseVo cacheUse, String desc, String[] groups) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.resultType = resultType;
		this.resultMap = resultMap;

		this.dsKey = dsKey;
		this.fetchSize = fetchSize;
		this.sqlNode = sqlNode;
		this.txDef = txDef;

		this.cacheUse = cacheUse;

		this.desc = desc;
		this.groups = groups;

		this.simple = true;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, HiveServiceContext sqlContext, Object acArg) throws Throwable {
		if (ac instanceof HIVECommandContext) {
			sqlContext.beforeExecute(this, true);
		} else {
			sqlContext.beforeExecute(this);
		}
		Object result = sqlContext.executeSelectSetListXCO(this, this.resultMap, fetchSize);
		sqlContext.afterExecute(this);
		// 7. 设置结果
		ac.setResult(result);
		return result;
	}

	//	public boolean execute1(ServiceContext serviceContext, Object arg) throws Throwable {
	//
	//		HiveServiceContext sqlContext = (HiveServiceContext) serviceContext.getServiceContext(TangYuanServiceType.HIVE);
	//
	//		// 1. cache使用
	//		if (null != cacheUse) {
	//			Object result = cacheUse.getObject(arg);
	//			if (null != result) {
	//				serviceContext.setResult(result);
	//				return true;
	//			}
	//		}
	//
	//		// 1. 清理和重置执行环境
	//		sqlContext.resetExecEnv();
	//
	//		Object result    = null;
	//		long   startTime = 0L;
	//
	//		// 2. 解析SQL
	//		sqlNode.execute(serviceContext, arg); // 获取sql
	//		if (log.isInfoEnabled() || isTraceCommand()) {
	//			sqlContext.parseSqlLog();
	//		}
	//
	//		// 3. 开启事务
	//		startTime = System.currentTimeMillis();
	//		sqlContext.beforeExecute(this); // 开启事务异常, 可认为是事务之前的异常
	//
	//		// 4. 执行SQL
	//		if (XCO.class == resultType) {
	//			result = sqlContext.executeSelectSetListXCO(this, this.resultMap, fetchSize);
	//		} else {
	//			result = sqlContext.executeSelectSetListMap(this, this.resultMap, fetchSize);
	//		}
	//
	//		// 5. 提交:这里做不确定的提交
	//		// sqlContext.commit(false);
	//		sqlContext.afterExecute(this);
	//
	//		// 6. 设置结果
	//		serviceContext.setResult(result);
	//		if (log.isInfoEnabled()) {
	//			log.info("hql execution time: " + getSlowServiceLog(startTime));
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
	//
	//	@Override
	//	@SuppressWarnings("unchecked")
	//	public Object getResult(ServiceContext context) {
	//		Object value = context.getResult();
	//		context.setResult(null);
	//		if (null == value) {
	//			return value;
	//		}
	//		try {
	//			if (null == resultMap) {
	//				if (XCO.class == resultType || Map.class == resultType) {
	//					// List<XCO>, List<Map>
	//					return value;
	//				} else {
	//					// List<Bean>, 最基本的bean:属性名和列名一直, 从数据库以Map接受方便
	//					List<Object>              result   = new ArrayList<Object>();
	//					List<Map<String, Object>> dataList = (List<Map<String, Object>>) value;
	//					int                       size     = dataList.size();
	//					for (int i = 0; i < size; i++) {
	//						Map<String, Object> data = dataList.get(i);
	//						result.add(OgnlBean.mapToBean(data, resultType));
	//					}
	//					return result;
	//				}
	//			} else {
	//				Class<?> beanClass = resultMap.getBeanClass();
	//				if (null == beanClass && XCO.class != resultType && Map.class != resultType) {
	//					beanClass = resultType;
	//				}
	//				// 在数据库查询的时候就需要做列的映射, 这里只做toBean(如果需要的话)的映射
	//				if (null != beanClass) {
	//					List<Object>              result   = new ArrayList<Object>();
	//					List<Map<String, Object>> dataList = (List<Map<String, Object>>) value;
	//					int                       size     = dataList.size();
	//					for (int i = 0; i < size; i++) {
	//						Map<String, Object> data = dataList.get(i);
	//						result.add(OgnlBean.mapToBean(data, beanClass));
	//					}
	//					return result;
	//				}
	//				// List<XCO>, List<Map>
	//				return value;
	//			}
	//		} catch (Throwable e) {
	//			// 这里认为是没有没开启, 因为只有独立事务,执行中才需要回滚当前，其他都是回滚所有
	//			// context.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), false));
	//			throw e;
	//		}
	//	}
}
