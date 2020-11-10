package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoUpdateNode extends AbstractMongoNode {

	private static Log log = LogFactory.getLog(MongoUpdateNode.class);

	public MongoUpdateNode(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode, CacheCleanVo cacheClean, String desc,
			String[] groups) {
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
	protected Object executeSql(ActuatorContext ac, MongoServiceContext context, Object temp) throws Throwable {
		Object result = context.executeUpdate(this, temp);
		// 7. 设置结果
		ac.setResult(result);
		return result;
	}

}
