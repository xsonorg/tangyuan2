package org.xson.tangyuan.sql.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.cmd.SQLCommandContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class InsertNode extends AbstractSqlNode {

	private static Log	log	= LogFactory.getLog(InsertNode.class);

	// 插入后的返回类型, 这里使用resultType, long, array, list, 空认为返回行数

	// 返回影响行数的key
	private String		resultKey;
	// 返回的自增key, 有可能是多个
	private String		incrementKey;

	public InsertNode(String id, String ns, String serviceKey, String resultKey, String incrementKey, String dsKey, XTransactionDefinition txDef,
			TangYuanNode sqlNode, CacheCleanVo cacheClean, String desc, String[] groups) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.resultKey = resultKey;
		this.incrementKey = incrementKey;

		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.txDef = txDef;

		this.cacheClean = cacheClean;

		this.desc = desc;
		this.groups = groups;

		this.simple = true;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, SqlServiceContext sqlContext, Object temp) throws Throwable {
		if (ac instanceof SQLCommandContext) {
			sqlContext.beforeExecute(this, true);
		} else {
			sqlContext.beforeExecute(this);
		}

		if (null == this.resultKey && null == this.incrementKey) {
			Object result = sqlContext.executeInsert(this);
			sqlContext.afterExecute(this);
			ac.setResult(result);
			return null;
		}
		XCO result = new XCO();
		if (null != this.resultKey && null == this.incrementKey) {
			int rowCount = sqlContext.executeInsert(this);
			sqlContext.afterExecute(this);
			result.setIntegerValue(this.resultKey, rowCount);
		} else {
			InsertReturn insertReturn = sqlContext.executeInsertReturn(this);
			sqlContext.afterExecute(this);
			if (null != this.resultKey) {
				result.setIntegerValue(this.resultKey, insertReturn.getRowCount());
			}
			result.setObjectValue(this.incrementKey, insertReturn.getColumns());
		}
		// 7. 设置结果
		ac.setResult(result);
		return null;
	}

}
