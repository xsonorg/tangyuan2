package org.xson.tangyuan.cache.apply;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.task.AsyncTask;

public class CacheUseVo extends CacheBase {

	private Long expiry;

	public CacheUseVo(TangYuanCache cache, String key, Long expiry, String service) {
		super(cache);
		this.expiry = expiry;
		parseKey(service, key);// 预处理key
	}

	public void putObject(final Object arg, final Object value) {
		TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
			@Override
			public void run() {
				String key = buildKey(arg);
				cache.put(key, value, expiry);
			}
		});
	}

	public Object getObject(Object arg) {
		String key = buildKey(arg);
		return cache.get(key);
	}

}
