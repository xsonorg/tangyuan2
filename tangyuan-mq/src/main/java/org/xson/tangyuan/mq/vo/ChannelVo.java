package org.xson.tangyuan.mq.vo;

import org.xson.tangyuan.mq.MqComponent;
import org.xson.tangyuan.mq.executor.Sender;

public class ChannelVo {

	public enum ChannelType {
		Queue, Topic
	}

	protected String      id;
	protected String      name;
	protected String      msKey;
	protected ChannelType type;
	protected String      senderKey;

	// 序列化方式

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getMsKey() {
		return msKey;
	}

	public ChannelType getType() {
		return type;
	}

	public Sender getSender() {
		return MqComponent.getInstance().getSender(senderKey);
	}

	// public ChannelVo(String id, String name, String msKey, ChannelType type, String senderKey, Map<String, String> properties) {
	// this.id = id;
	// this.name = name;
	// this.msKey = msKey;
	// this.type = type;
	// this.senderKey = senderKey;
	// }
}
