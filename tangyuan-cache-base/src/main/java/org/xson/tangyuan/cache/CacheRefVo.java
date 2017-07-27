package org.xson.tangyuan.cache;

public class CacheRefVo {

	private CacheVo		cacheVo;

	private String[]	include;

	private String[]	exclude;

	public CacheRefVo(CacheVo cacheVo, String[] include, String[] exclude) {
		this.cacheVo = cacheVo;
		this.include = include;
		this.exclude = exclude;
	}

	public CacheVo getCacheVo() {
		return cacheVo;
	}

	public String[] getInclude() {
		return include;
	}

	public String[] getExclude() {
		return exclude;
	}

}
