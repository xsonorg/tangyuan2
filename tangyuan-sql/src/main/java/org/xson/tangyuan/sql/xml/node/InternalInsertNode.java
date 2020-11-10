package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的InsertNode
 */
public class InternalInsertNode extends AbstractSqlNode {

	private static Log	log	= LogFactory.getLog(InternalInsertNode.class);

	// 返回影响行数的key
	private String		resultKey;
	// 返回的自增key, 有可能是多个
	private String		incrementKey;

	public InternalInsertNode(String dsKey, String rowCount, String incrementKey, TangYuanNode sqlNode, CacheCleanVo cacheClean) {
		this.dsKey = dsKey;
		this.resultKey = rowCount;
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
	protected Object executeSql(ActuatorContext ac, SqlServiceContext sqlContext, Object temp) throws Throwable {
		// 只是打开连接
		sqlContext.beforeExecute(this, true);

		if (null == this.resultKey && null == this.incrementKey) {
			sqlContext.executeInsert(this);
			sqlContext.afterExecute(this);
		} else if (null != this.resultKey && null == this.incrementKey) {
			int rowCount = sqlContext.executeInsert(this);
			sqlContext.afterExecute(this);
			Ognl.setValue(temp, this.resultKey, rowCount);
		} else {
			InsertReturn insertReturn = sqlContext.executeInsertReturn(this);
			sqlContext.afterExecute(this);
			if (null != this.resultKey) {
				Ognl.setValue(temp, this.resultKey, insertReturn.getRowCount());
			}
			Ognl.setValue(temp, this.incrementKey, insertReturn.getColumns());
		}
		return null;
	}

}
