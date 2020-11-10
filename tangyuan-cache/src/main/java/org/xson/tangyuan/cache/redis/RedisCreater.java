package org.xson.tangyuan.cache.redis;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheCreater;
import org.xson.tangyuan.cache.xml.vo.CacheVo;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.xml.XmlParseException;

public class RedisCreater implements CacheCreater {

	@Override
	public AbstractCache newInstance(CacheVo cacheVo) throws Throwable {
		RedisCache cache = new RedisCache(cacheVo.getId());
		if (null == cacheVo.getResource()) {
			throw new XmlParseException(TangYuanLang.get("cache.resource.empty", "resource", cacheVo.getId()));
		}
		cache.start(cacheVo);
		return cache;
	}

}
