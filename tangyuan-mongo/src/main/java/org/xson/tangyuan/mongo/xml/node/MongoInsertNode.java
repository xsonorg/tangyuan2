package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoInsertNode extends AbstractMongoNode {

	private static Log log = LogFactory.getLog(MongoInsertNode.class);

	public MongoInsertNode(String id, String ns, String serviceKey, String rowCount, String incrementKey, String dsKey, TangYuanNode sqlNode,
			CacheCleanVo cacheClean, String desc, String[] groups) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.dsKey = dsKey;
		this.sqlNode = sqlNode;

		this.simple = true;

		this.cacheClean = cacheClean;

		this.desc = desc;
		this.groups = groups;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, MongoServiceContext context, Object acArg) throws Throwable {
		InsertReturn ir = (InsertReturn) context.executeInsert(this, acArg);
		// 7. 设置结果
		ac.setResult(ir.getColumns());
		return null;
	}

}
