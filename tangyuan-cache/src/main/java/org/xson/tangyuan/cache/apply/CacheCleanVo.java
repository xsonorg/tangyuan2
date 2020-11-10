package org.xson.tangyuan.cache.apply;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.CacheComponent;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.service.pool.AsyncTask;
import org.xson.tangyuan.xml.XmlParseException;

public class CacheCleanVo extends CacheBase {

	public CacheCleanVo(TangYuanCache cache, String key, String service, boolean encodeKey) {
		super(cache);
		this.encodeKey = encodeKey;
		parseKey(service, key);
	}

	public void removeObject(String key) {
		TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
			@Override
			public void run() {
				cache.remove(key);
			}
		});
	}

	/**
	 * 解析: ID:xxx; key:xxx; encodeKey:true;
	 */
	public static CacheCleanVo parseCacheClean(String cacheUse, String service) {
		if (null == cacheUse) {
			return null;
		}
		CacheCleanVo cacheCleanVo = null;
		String[] array = cacheUse.split(";");
		if (array.length > 0) {
			Map<String, String> map = new HashMap<String, String>();
			for (int i = 0; i < array.length; i++) {
				String[] item = array[i].split(":");
				map.put(item[0].trim().toUpperCase(), item[1].trim());
			}
			TangYuanCache cache = CacheComponent.getInstance().getCache(map.get("id".toUpperCase()));
			if (null == cache) {
				throw new XmlParseException(TangYuanLang.get("cacheClean.expr.reference.invalid", map.get("id".toUpperCase()), cacheUse, service));
			}
			String key = map.get("key".toUpperCase());
			if (null == key) {
				throw new XmlParseException(TangYuanLang.get("cacheClean.expr.key.invalid", "null", cacheUse, service));
			}

			boolean encodeKey = true;
			if (map.containsKey("encodeKey".toUpperCase())) {
				encodeKey = Boolean.parseBoolean(map.get("encodeKey".toUpperCase()));
			}

			cacheCleanVo = new CacheCleanVo(cache, key, service, encodeKey);
		}
		return cacheCleanVo;
	}

}
