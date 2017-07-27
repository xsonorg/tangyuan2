package org.xson.tangyuan.cache;

import java.util.Map;

public class CacheVo {

	public enum CacheType {
		LOCAL, EHCACHE, MEMCACHE, REDIS, SHARE
	}

	public enum CacheStrategyType {
		LRU, FIFO, SOFT, WEAK, TIME
	}

	protected String			id;
	protected boolean			group;
	protected AbstractCache		cache;

	protected String			jndiName;
	protected String			creator;

	private CacheType			type;
	private String				resource;
	private Map<String, String>	properties;

	public CacheVo(String id, String creator) {
		this.id = id;
		this.creator = creator;
	}

	public CacheVo(String id, CacheType type, AbstractCache cache, String resource, Map<String, String> properties, String jndiName, String creator) {
		this.id = id;
		this.type = type;
		this.cache = cache;
		this.resource = resource;
		this.properties = properties;
		// this.group = group;
		this.jndiName = jndiName;
		this.creator = creator;
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

	public String getJndiName() {
		return jndiName;
	}

	public String getCreator() {
		return creator;
	}

	public void start() {
		if (null != cache) {
			cache.start(resource, properties);
		} else {
			cache = new CacheCreaterFactory().newInstance(type).newInstance(this);
		}
		if (null != properties) {
			properties.clear();
			properties = null;
		}
	}
}
