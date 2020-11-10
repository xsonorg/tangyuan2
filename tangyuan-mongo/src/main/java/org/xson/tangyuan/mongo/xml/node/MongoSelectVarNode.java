package org.xson.tangyuan.mongo.xml.node;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.service.context.MongoServiceContext;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class MongoSelectVarNode extends AbstractMongoNode {

	private static Log	log			= LogFactory.getLog(MongoSelectVarNode.class);

	private MappingVo	resultMap	= null;

	public MongoSelectVarNode(String id, String ns, String serviceKey, String dsKey, TangYuanNode sqlNode, CacheUseVo cacheUse, String desc,
			String[] groups) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.dsKey = dsKey;
		this.sqlNode = sqlNode;

		this.cacheUse = cacheUse;

		this.simple = true;

		this.desc = desc;
		this.groups = groups;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected Object executeSql(ActuatorContext ac, MongoServiceContext context, Object temp) throws Throwable {
		Object result = context.executeSelectVar(this, this.resultMap, temp);
		// 7. 设置结果
		ac.setResult(result);
		return result;
	}

}
