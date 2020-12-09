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

public class SelectVarNode extends AbstractHiveNode {

	private static Log log       = LogFactory.getLog(SelectVarNode.class);

	private MappingVo  resultMap = null;

	//	public SelectVarNode(String id, String ns, String serviceKey, String dsKey, XTransactionDefinition txDef, TangYuanNode sqlNode, CacheUseVo cacheUse, String desc,
	//			String[] groups) {
	//
	//		this.id = id;
	//		this.ns = ns;
	//		this.serviceKey = serviceKey;
	//
	//		this.dsKey = dsKey;
	//		this.sqlNode = sqlNode;
	//		this.txDef = txDef;
	//
	//		this.cacheUse = cacheUse;
	//
	//		this.desc = desc;
	//		this.groups = groups;
	//
	//		this.simple = true;
	//	}

	public SelectVarNode(String id, String ns, String serviceKey, String dsKey, XTransactionDefinition txDef, TangYuanNode sqlNode, MappingVo resultMap, CacheUseVo cacheUse,
			String desc, String[] groups) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.txDef = txDef;

		this.resultMap = resultMap;
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
		Object result = sqlContext.executeSelectVar(this, this.resultMap);
		sqlContext.afterExecute(this);
		// 7. 设置结果
		ac.setResult(result);
		return result;
	}

	//	@Override
	//	public boolean execute(ServiceContext serviceContext, Object arg) throws Throwable {
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
	//		result = sqlContext.executeSelectVar(this);
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

}
