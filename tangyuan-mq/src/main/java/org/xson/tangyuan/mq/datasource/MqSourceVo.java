package org.xson.tangyuan.mq.datasource;

import java.util.Map;

public class MqSourceVo {

	// MQ type
	public enum MqSourceType {
		RabbitMQ, ActiveMQ
	}

	private String				id			= null;

	private MqSourceType		type		= null;

	private Map<String, String>	properties	= null;

	private String				resource	= null;

	public MqSourceVo(String id, MqSourceType type, Map<String, String> properties, String resource) {
		this.id = id;
		this.type = type;
		this.properties = properties;

		this.resource = resource;
	}

	public String getId() {
		return id;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public MqSourceType getType() {
		return type;
	}

	public String getResource() {
		return resource;
	}

}
