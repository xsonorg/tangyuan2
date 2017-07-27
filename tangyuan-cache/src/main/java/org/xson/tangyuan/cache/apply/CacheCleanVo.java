package org.xson.tangyuan.cache.apply;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.task.AsyncTask;

public class CacheCleanVo extends CacheBase {

	public CacheCleanVo(TangYuanCache cache, String key, String service) {
		super(cache);
		parseKey(service, key);
	}

	public void removeObject(final Object arg) {
		TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
			@Override
			public void run() {
				String key = buildKey(arg);
				cache.remove(key);
			}
		});
	}

}
