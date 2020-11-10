package org.xson.tangyuan.cache.xml.vo;

import java.util.Map;

import org.xson.tangyuan.cache.AbstractCache;
import org.xson.tangyuan.cache.CacheCreaterFactory;
import org.xson.tangyuan.cache.CacheSerializer;

public class CacheVo {

	public enum CacheType {
		LOCAL, EHCACHE, MEMCACHE, REDIS
	}

	protected String			id;
	protected boolean			group;
	protected AbstractCache		cache;
	private CacheType			type;
	private String				resource;
	private Map<String, String>	properties;
	/** 节点默认过期时间 */
	private Long				expiry;
	/** 节点默认序列化处理器 */
	private CacheSerializer		serializer;

	public CacheVo(String id) {
		this.id = id;
	}

	public CacheVo(String id, CacheType type, AbstractCache cache, String resource, Map<String, String> properties, Long expiry,
			CacheSerializer serializer) {
		this.id = id;
		this.type = type;
		this.cache = cache;
		this.resource = resource;
		this.properties = properties;

		this.expiry = expiry;
		this.serializer = serializer;
	}

	public String getId() {
		return id;
	}

	public AbstractCache getCache() {
		return cache;
	}

	public boolean isGroup() {
		return group;
	}

	public CacheType getType() {
		return type;
	}

	public String getResource() {
		return resource;
	}

	public Long getExpiry() {
		return expiry;
	}

	public CacheSerializer getSerializer() {
		return serializer;
	}

	public void start() throws Throwable {
		if (null != cache) {
			cache.start(this);
		} else {
			cache = new CacheCreaterFactory().newInstance(type).newInstance(this);
		}
		if (null != properties) {
			properties.clear();
			properties = null;
		}
	}

}
