package org.xson.tangyuan.cache.apply;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.xson.tangyuan.cache.CacheComponent;
import org.xson.tangyuan.cache.CacheException;
import org.xson.tangyuan.cache.CacheKeyBuilder;

public class DefaultMD5CacheKeyBuilder implements CacheKeyBuilder {

	@Override
	public String build(Object originalKey) {
		String defaultCacheKeyPrefix = CacheComponent.getInstance().getDefaultCacheKeyPrefix();
		if (null != defaultCacheKeyPrefix) {
			return defaultCacheKeyPrefix + MD5(originalKey.toString());
		}
		return MD5(originalKey.toString());
	}

	private char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private String MD5(String s) {
		try {
			return MD5(s.getBytes(StandardCharsets.UTF_8));
		} catch (Throwable e) {
			throw new CacheException("Unsupported Encoding Exception: " + s, e);
		}
	}

	private String MD5(byte[] btInput) {
		try {
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md    = mdInst.digest();
			int    j     = md.length;
			char   str[] = new char[j * 2];
			int    k     = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str).substring(8, 24);// 16位
		} catch (Throwable e) {
			throw new CacheException("MD5 Encoding Exception.", e);
		}
	}
}
