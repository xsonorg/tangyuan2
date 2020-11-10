package org.xson.tangyuan.sql.xml.node;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.sql.service.cmd.SQLCommandContext;
import org.xson.tangyuan.sql.service.context.SqlServiceContext;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class SelectOneNode extends AbstractSqlNode {

	private static Log	log			= LogFactory.getLog(SelectOneNode.class);

	private MappingVo	resultMap	= null;

	public SelectOneNode(String id, String ns, String serviceKey, Class<?> resultType, MappingVo resultMap, String dsKey,
			XTransactionDefinition txDef, TangYuanNode sqlNode, CacheUseVo cacheUse, String desc, String[] groups) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.resultType = resultType;
		this.resultMap = resultMap;

		this.dsKey = dsKey;
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
	protected Object executeSql(ActuatorContext ac, SqlServiceContext sqlContext, Object temp) throws Throwable {
		if (ac instanceof SQLCommandContext) {
			sqlContext.beforeExecute(this, true);
		} else {
			sqlContext.beforeExecute(this);
		}
		Object result = sqlContext.executeSelectOneXCO(this, this.resultMap, null);
		sqlContext.afterExecute(this);
		// 7. 设置结果
		ac.setResult(result);
		return result;
	}

}
