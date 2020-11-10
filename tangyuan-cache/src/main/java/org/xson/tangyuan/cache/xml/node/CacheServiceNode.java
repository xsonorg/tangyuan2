package org.xson.tangyuan.cache.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

/**
 * Cache服务
 */
public class CacheServiceNode extends AbstractServiceNode {

	private static Log    log    = LogFactory.getLog(CacheServiceNode.class);

	private TangYuanCache cache  = null;
	/**
	 * 操作类型: [1:put, 2:get, 3:remove]
	 */
	private int           opType = 0;

	public CacheServiceNode(String id, String ns, String serviceKey, TangYuanCache cache, int opType, String desc, String[] groups) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.serviceType = TangYuanServiceType.CACHE;

		this.cache = cache;
		this.opType = opType;

		this.desc = desc;
		this.groups = groups;
	}

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {

		long   startTime = System.currentTimeMillis();
		Object result    = null;

		if (1 == opType) {
			put(arg);
		} else if (2 == opType) {
			result = get(arg);
		} else if (3 == opType) {
			result = remove(arg);
		}
		ac.setResult(result);

		if (log.isInfoEnabled()) {
			log.info("cache execution time: " + getSlowServiceLog(startTime));
		}

		return true;
	}

	private void put(Object arg) {
		Object key    = getCacheKey(arg);
		Object value  = getCacheValue(arg);
		Long   expiry = getCacheExpiry(arg);
		this.cache.put(key, value, expiry);
	}

	private Object get(Object arg) {
		Object key = getCacheKey(arg);
		return this.cache.get(key);
	}

	private Object remove(Object arg) {
		Object key = getCacheKey(arg);
		return this.cache.remove(key);
	}

	private Object getCacheKey(Object arg) {
		return ((XCO) arg).get("key");
	}

	private Object getCacheValue(Object arg) {
		return ((XCO) arg).get("value");
	}

	private Long getCacheExpiry(Object arg) {
		return ((XCO) arg).getLong("expiry");
	}
}
