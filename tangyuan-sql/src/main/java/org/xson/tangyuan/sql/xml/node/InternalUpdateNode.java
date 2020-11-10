package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的UpdateNode
 */
public class InternalUpdateNode extends AbstractSqlNode {

	private static Log	log	= LogFactory.getLog(InternalUpdateNode.class);

	// 返回结果的key
	private String		resultKey;

	public InternalUpdateNode(String dsKey, String rowCount, TangYuanNode sqlNode, CacheCleanVo cacheClean) {
		this.dsKey = dsKey;
		this.resultKey = rowCount;
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
		Object result = sqlContext.executeUpdate(this);
		sqlContext.afterExecute(this);
		if (null != this.resultKey) {
			Ognl.setValue(temp, this.resultKey, result);
		}
		return null;// 这里返回对于后续缓存的处理无异议，所以对于cacheClean统一返回null
	}

}
