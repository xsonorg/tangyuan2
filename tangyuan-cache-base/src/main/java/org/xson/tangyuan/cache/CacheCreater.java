package org.xson.tangyuan.cache;

public interface CacheCreater {

	AbstractCache newInstance(CacheVo cacheVo);
	
}
