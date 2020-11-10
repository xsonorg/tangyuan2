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
 * 内部的SelectOneNode
 */
public class InternalSelectOneNode extends AbstractSqlNode {

	private static Log	log			= LogFactory.getLog(InternalSelectOneNode.class);

	private String		resultKey	= null;
	private MappingVo	resultMap	= null;

	public InternalSelectOneNode(String dsKey, String resultKey, TangYuanNode sqlNode, Class<?> resultType, MappingVo resultMap,
			CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
		this.resultType = resultType;
		this.resultMap = resultMap;

		this.cacheUse = cacheUse;

		this.simple = false;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, SqlServiceContext sqlContext, Object temp) throws Throwable {
		// 只是打开连接
		sqlContext.beforeExecute(this, true);
		Object result = sqlContext.executeSelectOneXCO(this, this.resultMap, null);
		sqlContext.afterExecute(this);
		if (null != this.resultKey) {
			Ognl.setValue(temp, this.resultKey, result);
		}
		return result;
	}

}
