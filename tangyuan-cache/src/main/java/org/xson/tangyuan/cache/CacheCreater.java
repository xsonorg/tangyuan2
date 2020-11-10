package org.xson.tangyuan.cache;

import org.xson.tangyuan.cache.xml.vo.CacheVo;

public interface CacheCreater {

	AbstractCache newInstance(CacheVo cacheVo) throws Throwable;

}
