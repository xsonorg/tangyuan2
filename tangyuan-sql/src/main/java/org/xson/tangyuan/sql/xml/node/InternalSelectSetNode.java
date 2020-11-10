package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

/**
 * 内部的SelectSetNode
 */
public class InternalSelectSetNode extends AbstractSqlNode {

	private static Log	log			= LogFactory.getLog(InternalSelectSetNode.class);

	private Integer		fetchSize	= null;
	private String		resultKey	= null;
	private MappingVo	resultMap	= null;

	public InternalSelectSetNode(String dsKey, String resultKey, TangYuanNode sqlNode, Class<?> resultType, MappingVo resultMap, Integer fetchSize,
			CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
		this.fetchSize = fetchSize;

		this.resultType = resultType;
		this.resultMap = resultMap;

		this.cacheUse = cacheUse;

		this.simple = false;
	}

	public int getFetchSize() {
		return fetchSize;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, SqlServiceContext sqlContext, Object temp) throws Throwable {
		// 只是打开连接
		sqlContext.beforeExecute(this, true);
		Object result = sqlContext.executeSelectSetListXCO(this, this.resultMap, this.fetchSize);
		sqlContext.afterExecute(this);
		if (null != this.resultKey) {
			Ognl.setValue(temp, this.resultKey, result);
		}
		return result;
	}

}
