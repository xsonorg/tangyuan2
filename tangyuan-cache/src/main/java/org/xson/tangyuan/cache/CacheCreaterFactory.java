package org.xson.tangyuan.cache;

import org.xson.tangyuan.cache.encache.EhCacheCreater;
import org.xson.tangyuan.cache.local.LocalCacheCreater;
import org.xson.tangyuan.cache.memcache.MemcacheCreater;
import org.xson.tangyuan.cache.redis.RedisCreater;
import org.xson.tangyuan.cache.xml.vo.CacheVo.CacheType;

public class CacheCreaterFactory {

	public CacheCreater newInstance(CacheType type) {
		if (CacheType.LOCAL == type) {
			return new LocalCacheCreater();
		} else if (CacheType.EHCACHE == type) {
			return new EhCacheCreater();
		} else if (CacheType.MEMCACHE == type) {
			return new MemcacheCreater();
		} else if (CacheType.REDIS == type) {
			return new RedisCreater();
		}
		//		else if (CacheType.SHARE == type) {
		//			return new ShareCacheCreater();
		//		} 
		else {
			return null;
		}
	}

}
