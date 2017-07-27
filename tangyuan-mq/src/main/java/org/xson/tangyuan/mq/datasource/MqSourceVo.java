package org.xson.tangyuan.mq.datasource;

import java.util.Map;

public class MqSourceVo {

	// MQ type
	public enum MqSourceType {
		RabbitMQ, ActiveMQ
	}

	private String				id;

	private MqSourceType		type;

	private Map<String, String>	properties;

	public MqSourceVo(String id, MqSourceType type, Map<String, String> properties) {
		this.id = id;
		this.type = type;
		this.properties = properties;
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

}
