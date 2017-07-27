package org.xson.tangyuan.cache;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.cache.local.MixedCache;

public class CacheGroupVo extends CacheVo {

	private List<CacheRefVo> cacheRefList;

	public CacheGroupVo(String id, List<CacheRefVo> cacheRefList, String creator) {
		super(id, creator);
		this.cacheRefList = cacheRefList;
		this.group = true;
	}

	public void start() {
		List<AbstractCache> cacheList = new ArrayList<AbstractCache>();
		for (CacheRefVo refVo : cacheRefList) {
			cacheList.add(refVo.getCacheVo().getCache());
		}
		this.cache = new MixedCache(id, cacheList);
	}

}
