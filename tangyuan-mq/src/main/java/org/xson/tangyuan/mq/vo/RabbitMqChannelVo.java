package org.xson.tangyuan.mq.vo;

import java.util.Map;

import org.xson.tangyuan.xml.XmlParseException;

import com.rabbitmq.client.BuiltinExchangeType;

public class RabbitMqChannelVo extends ChannelVo {

	public final static String	RABBITMQ_DURABLE			= "rabbitmq.durable".toUpperCase();
	public final static String	RABBITMQ_EXCLUSIVE			= "rabbitmq.exclusive".toUpperCase();
	public final static String	RABBITMQ_AUTODELETE			= "rabbitmq.autoDelete".toUpperCase();
	public final static String	RABBITMQ_EXCHANGE_TYPE		= "rabbitmq.exchangeType".toUpperCase();

	public final static String	RABBITMQ_C_AUTOACK			= "rabbitmq.c.autoAck".toUpperCase();
	public final static String	RABBITMQ_C_PREFETCHCOUNT	= "rabbitmq.c.prefetchCount".toUpperCase();
	public final static String	RABBITMQ_C_ASYNRECEIVE		= "rabbitmq.c.asynReceive".toUpperCase();

	// C+P
	private boolean				durable;
	private boolean				exclusive;
	private boolean				autoDelete;
	private String				exchangeType;

	// C
	private boolean				autoAck;
	private int					prefetchCount;
	private boolean				asynReceive;

	public RabbitMqChannelVo(String id, String name, String msKey, ChannelType type, String senderKey, Map<String, String> properties) {
		this.id = id;
		this.name = name;
		this.msKey = msKey;
		this.type = type;
		this.senderKey = senderKey;
		parse(properties);
	}

	public boolean isDurable() {
		return durable;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public boolean isAutoDelete() {
		return autoDelete;
	}

	public String getExchangeType() {
		return exchangeType;
	}

	public boolean isAutoAck() {
		return autoAck;
	}

	public int getPrefetchCount() {
		return prefetchCount;
	}

	public boolean isAsynReceive() {
		return asynReceive;
	}

	private void parse(Map<String, String> properties) {

		if (properties.containsKey(RABBITMQ_DURABLE)) {
			String value = properties.get(RABBITMQ_DURABLE);
			this.durable = Boolean.parseBoolean(value);
		} else {
			this.durable = true;
		}

		if (properties.containsKey(RABBITMQ_EXCLUSIVE)) {
			String value = properties.get(RABBITMQ_EXCLUSIVE);
			this.exclusive = Boolean.parseBoolean(value);
		} else {
			this.exclusive = false;
		}

		if (properties.containsKey(RABBITMQ_AUTODELETE)) {
			String value = properties.get(RABBITMQ_AUTODELETE);
			this.autoDelete = Boolean.parseBoolean(value);
		} else {
			this.autoDelete = false;
		}

		if (properties.containsKey(RABBITMQ_EXCHANGE_TYPE)) {
			String value = properties.get(RABBITMQ_EXCHANGE_TYPE);
			if (!"fanout".equalsIgnoreCase(value) && !"direct".equalsIgnoreCase(value) && !"topic".equalsIgnoreCase(value)) {
				throw new XmlParseException("Invalid attribute[rabbitmq.exchangeType] value: " + value);
			}
			this.exchangeType = value;
		} else {
			this.exchangeType = BuiltinExchangeType.FANOUT.getType();
		}

		if (properties.containsKey(RABBITMQ_C_AUTOACK)) {
			String value = properties.get(RABBITMQ_C_AUTOACK);
			this.autoAck = Boolean.parseBoolean(value);
		} else {
			this.autoAck = false;
		}
		if (properties.containsKey(RABBITMQ_C_PREFETCHCOUNT)) {
			String value = properties.get(RABBITMQ_C_PREFETCHCOUNT);
			this.prefetchCount = Integer.parseInt(value);
		} else {
			this.prefetchCount = 1;
		}

		if (properties.containsKey(RABBITMQ_C_ASYNRECEIVE)) {
			String value = properties.get(RABBITMQ_C_ASYNRECEIVE);
			this.asynReceive = Boolean.parseBoolean(value);
		} else {
			this.asynReceive = true;
		}
	}

}
