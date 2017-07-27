package org.xson.tangyuan.mq.vo;

import java.util.Map;

import javax.jms.DeliveryMode;
import javax.jms.Session;

import org.xson.tangyuan.xml.XmlParseException;

public class ActiveMqChannelVo extends ChannelVo {

	public final static String	ACTIVEMQ_P_DELIVERYMODE			= "activemq.p.deliveryMode".toUpperCase();
	public final static String	ACTIVEMQ_P_TIMETOLIVE			= "activemq.p.timeToLive".toUpperCase();

	public final static String	ACTIVEMQ_C_TRANSACTED			= "activemq.c.transacted".toUpperCase();
	public final static String	ACTIVEMQ_C_ACKNOWLEDGEMODE		= "activemq.c.acknowledgeMode".toUpperCase();
	public final static String	ACTIVEMQ_C_CLIENTID				= "activemq.c.clientID".toUpperCase();
	public final static String	ACTIVEMQ_C_DURABLESUBSCRIBERS	= "activemq.c.durableSubscribers".toUpperCase();
	public final static String	ACTIVEMQ_C_RECEIVETIMEOUT		= "activemq.c.receiveTimeout".toUpperCase();
	public final static String	ACTIVEMQ_C_ASYNRECEIVE			= "activemq.c.asynReceive".toUpperCase();

	////////////////////////////////// P

	private int					deliveryMode;
	private long				timeToLive;

	////////////////////////////////// C
	private boolean				transacted;
	private int					acknowledgeMode;
	private String				clientID;
	private boolean				durableSubscribers;
	private long				receiveTimeout;
	private boolean				asynReceive;

	public ActiveMqChannelVo(String id, String name, String msKey, ChannelType type, String senderKey, Map<String, String> properties) {
		this.id = id;
		this.name = name;
		this.msKey = msKey;
		this.type = type;
		this.senderKey = senderKey;
		parse(properties);
	}

	public int getDeliveryMode() {
		return deliveryMode;
	}

	public long getTimeToLive() {
		return timeToLive;
	}

	public boolean isTransacted() {
		return transacted;
	}

	public int getAcknowledgeMode() {
		return acknowledgeMode;
	}

	public String getClientID() {
		return clientID;
	}

	public boolean isDurableSubscribers() {
		return durableSubscribers;
	}

	public long getReceiveTimeout() {
		return receiveTimeout;
	}

	public boolean isAsynReceive() {
		return asynReceive;
	}

	private void parse(Map<String, String> properties) {

		if (properties.containsKey(ACTIVEMQ_P_DELIVERYMODE)) {
			String value = properties.get(ACTIVEMQ_P_DELIVERYMODE);
			if ("PERSISTENT".equalsIgnoreCase(value)) {
				this.deliveryMode = DeliveryMode.PERSISTENT;
			} else if ("NON_PERSISTENT".equalsIgnoreCase(value)) {
				this.deliveryMode = DeliveryMode.NON_PERSISTENT;
			} else {
				throw new XmlParseException("Invalid attribute[activemq.p.deliveryMode] value: " + value);
			}
		} else {
			this.deliveryMode = DeliveryMode.PERSISTENT;
		}

		if (properties.containsKey(ACTIVEMQ_P_TIMETOLIVE)) {
			String value = properties.get(ACTIVEMQ_P_TIMETOLIVE);
			this.timeToLive = Long.parseLong(value);
		} else {
			this.timeToLive = 0L;
		}

		if (properties.containsKey(ACTIVEMQ_C_TRANSACTED)) {
			String value = properties.get(ACTIVEMQ_C_TRANSACTED);
			this.transacted = Boolean.parseBoolean(value);
		} else {
			this.transacted = true;
		}

		if (properties.containsKey(ACTIVEMQ_C_ACKNOWLEDGEMODE)) {
			String value = properties.get(ACTIVEMQ_C_ACKNOWLEDGEMODE);
			if ("AUTO_ACKNOWLEDGE".equalsIgnoreCase(value)) {
				this.acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
			} else if ("CLIENT_ACKNOWLEDGE".equalsIgnoreCase(value)) {
				this.acknowledgeMode = Session.CLIENT_ACKNOWLEDGE;
			} else if ("DUPS_OK_ACKNOWLEDGE".equalsIgnoreCase(value)) {
				this.acknowledgeMode = Session.DUPS_OK_ACKNOWLEDGE;
			} else if ("SESSION_TRANSACTED".equalsIgnoreCase(value)) {
				this.acknowledgeMode = Session.SESSION_TRANSACTED;
			} else {
				throw new XmlParseException("Invalid attribute[activemq.c.acknowledgeMode] value: " + value);
			}
		} else {
			this.acknowledgeMode = Session.SESSION_TRANSACTED;
		}

		if (properties.containsKey(ACTIVEMQ_C_CLIENTID)) {
			String value = properties.get(ACTIVEMQ_C_CLIENTID);
			this.clientID = value;
		}

		if (properties.containsKey(ACTIVEMQ_C_DURABLESUBSCRIBERS)) {
			String value = properties.get(ACTIVEMQ_C_DURABLESUBSCRIBERS);
			this.durableSubscribers = Boolean.parseBoolean(value);
		} else {
			this.durableSubscribers = false;
		}

		if (properties.containsKey(ACTIVEMQ_C_ASYNRECEIVE)) {
			String value = properties.get(ACTIVEMQ_C_ASYNRECEIVE);
			this.asynReceive = Boolean.parseBoolean(value);
		} else {
			this.asynReceive = true;
		}

		if (properties.containsKey(ACTIVEMQ_C_RECEIVETIMEOUT)) {
			String value = properties.get(ACTIVEMQ_C_RECEIVETIMEOUT);
			long receiveTimeout = Long.parseLong(value);
			// TODO max receiveTimeout
			if (receiveTimeout <= 0) {
				receiveTimeout = 1000L;
			}
			// this.receiveTimeout = receiveTimeout * 1000L;
			this.receiveTimeout = receiveTimeout;
		} else {
			// properties.put(ACTIVEMQ_C_RECEIVETIMEOUT, 1000L);
			// this.receiveTimeout = 1000L;
			this.receiveTimeout = 1000L;
		}
	}

}
