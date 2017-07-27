package org.xson.tangyuan.mq.vo;

import java.util.Map;

public class ServiceVo {

	private String					id;

	private String					ns;

	private String					service;

	private String[]				channels;

	private boolean					useTx;

	private Map<String, RoutingVo>	routingMap;

	public ServiceVo(String id, String ns, String service, String[] channels, boolean useTx, Map<String, RoutingVo> routingMap) {
		this.id = id;
		this.ns = ns;
		this.service = service;
		this.channels = channels;
		this.useTx = useTx;
		this.routingMap = routingMap;
	}

	public String getService() {
		return service;
	}

	public String[] getChannels() {
		return channels;
	}

	public boolean isUseTx() {
		return useTx;
	}

	public RoutingVo getRouting(String channel) {
		if (null == routingMap) {
			return null;
		}
		return routingMap.get(channel);
	}

	public String getId() {
		return id;
	}

	public String getNs() {
		return ns;
	}

}
