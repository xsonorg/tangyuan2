package org.xson.tangyuan.cache.apply;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.CacheComponent;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.service.pool.AsyncTask;
import org.xson.tangyuan.xml.XmlParseException;

public class CacheUseVo extends CacheBase {

	private Long expiry;

	public CacheUseVo(TangYuanCache cache, String key, Long expiry, String service, boolean encodeKey) {
		super(cache);
		this.expiry = expiry;
		this.encodeKey = encodeKey;
		// 预处理key
		parseKey(service, key);
	}

	public void putObject(String key, final Object value) {
		TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
			@Override
			public void run() {
				cache.put(key, value, expiry);
			}
		});
	}

	public Object getObject(String key) {
		return cache.get(key);
	}

	/**
	 * 解析: ID:xxx; key:xxx; expiry:10(秒); encodeKey:true;
	 */
	public static CacheUseVo parseCacheUse(String cacheUse, String service) {
		if (null == cacheUse) {
			return null;
		}
		CacheUseVo cacheUseVo = null;
		String[] array = cacheUse.split(";");
		if (array.length > 0) {
			Map<String, String> map = new HashMap<String, String>();
			for (int i = 0; i < array.length; i++) {
				String[] item = array[i].split(":");
				map.put(item[0].trim().toUpperCase(), item[1].trim());
			}
			TangYuanCache cache = CacheComponent.getInstance().getCache(map.get("id".toUpperCase()));
			if (null == cache) {
				throw new XmlParseException(TangYuanLang.get("cacheUse.expr.reference.invalid", map.get("id".toUpperCase()), cacheUse, service));
			}
			String key = map.get("key".toUpperCase());
			if (null == key) {
				throw new XmlParseException(TangYuanLang.get("cacheUse.expr.key.invalid", "null", cacheUse, service));
			}
			Long expiry = null;
			if (map.containsKey("expiry".toUpperCase())) {
				expiry = Long.parseLong(map.get("expiry".toUpperCase()));
			}
			boolean encodeKey = true;
			if (map.containsKey("encodeKey".toUpperCase())) {
				encodeKey = Boolean.parseBoolean(map.get("encodeKey".toUpperCase()));
			}

			cacheUseVo = new CacheUseVo(cache, key, expiry, service, encodeKey);
		}
		return cacheUseVo;
	}

}
