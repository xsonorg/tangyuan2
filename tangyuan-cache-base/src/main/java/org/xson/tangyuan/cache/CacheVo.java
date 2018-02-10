package org.xson.tangyuan.cache;

import java.util.Map;

public class CacheVo {

	public enum CacheType {
		LOCAL, EHCACHE, MEMCACHE, REDIS, SHARE
	}

	// public enum CacheStrategyType {
	// LRU, FIFO, SOFT, WEAK, TIME
	// }

	protected String			id;
	protected boolean			group;
	protected AbstractCache		cache;

	protected String			sharedUse;
	protected String			creator;

	private CacheType			type;
	private String				resource;
	private Map<String, String>	properties;

	/** 节点默认过期时间 */
	private Long				expiry;
	/** 节点默认序列化处理器 */
	private CacheSerializer		serializer;

	private Map<String, String>	placeholderMap;

	public CacheVo(String id, String creator) {
		this.id = id;
		this.creator = creator;
	}

	// public CacheVo(String id, CacheType type, AbstractCache cache, String resource, Map<String, String> properties, String sharedUse,
	// String creator) {
	// this.id = id;
	// this.type = type;
	// this.cache = cache;
	// this.resource = resource;
	// this.properties = properties;
	// this.sharedUse = sharedUse;
	// this.creator = creator;
	// }

	public CacheVo(String id, CacheType type, AbstractCache cache, String resource, Map<String, String> properties, String sharedUse, String creator,
			Long expiry, CacheSerializer serializer, Map<String, String> placeholderMap) {
		this.id = id;
		this.type = type;
		this.cache = cache;
		this.resource = resource;
		this.properties = properties;
		this.sharedUse = sharedUse;
		this.creator = creator;

		this.expiry = expiry;
		this.serializer = serializer;
		this.placeholderMap = placeholderMap;
	}

	public String getId() {
		return id;
	}

	public AbstractCache getCache() {
		return cache;
	}

	public Map<String, String> getProperties() {
		return properties;
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

	public String getSharedUse() {
		return sharedUse;
	}

	public String getCreator() {
		return creator;
	}

	public Long getExpiry() {
		return expiry;
	}

	public CacheSerializer getSerializer() {
		return serializer;
	}

	public Map<String, String> getPlaceholderMap() {
		return placeholderMap;
	}

	public void start() {
		if (null != cache) {
			// cache.start(resource, properties);
			cache.start(this);
		} else {
			cache = new CacheCreaterFactory().newInstance(type).newInstance(this);
		}
		if (null != properties) {
			properties.clear();
			properties = null;
		}

		placeholderMap = null;
	}
}
