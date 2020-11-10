package org.xson.tangyuan.manager.conf;

import org.xson.tangyuan.util.TangYuanUtil;

public class ResourceReloaderVo implements Comparable<ResourceReloaderVo> {

	public static int               defaultOrder = 10;

	private String                  resource;
	private ResourceReloader        instance;
	private Class<ResourceReloader> clazz;
	private int                     order;

	public ResourceReloaderVo(String resource, ResourceReloader instance) {
		this(resource, instance, null, null);
	}

	public ResourceReloaderVo(String resource, ResourceReloader instance, Integer order) {
		this(resource, instance, null, order);
	}

	public ResourceReloaderVo(String resource, Class<ResourceReloader> clazz) {
		this(resource, null, clazz, null);
	}

	public ResourceReloaderVo(String resource, Class<ResourceReloader> clazz, Integer order) {
		this(resource, null, clazz, order);
	}

	public ResourceReloaderVo(String resource, ResourceReloader instance, Class<ResourceReloader> clazz, Integer order) {
		this.resource = resource;
		this.instance = instance;
		this.clazz = clazz;
		if (null == order) {
			order = defaultOrder;
		}
		this.order = order;
	}

	public void initInstance() {
		if (null == this.instance) {
			this.instance = TangYuanUtil.newInstance(this.clazz);
		}
	}

	public String getResource() {
		return resource;
	}

	public ResourceReloader getInstance() {
		return instance;
	}

	public int getOrder() {
		return order;
	}

	@Override
	public int compareTo(ResourceReloaderVo o) {
		return this.order - o.getOrder();
	}
}
