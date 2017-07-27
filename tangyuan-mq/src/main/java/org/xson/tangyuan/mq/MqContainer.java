package org.xson.tangyuan.mq;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.mq.datasource.MqSourceManager;
import org.xson.tangyuan.mq.executor.MqServiceContextFactory;
import org.xson.tangyuan.mq.executor.Sender;
import org.xson.tangyuan.mq.executor.activemq.ActiveMqSender;
import org.xson.tangyuan.mq.executor.rabbitmq.RabbitMqSender;
import org.xson.tangyuan.mq.vo.ChannelVo;
import org.xson.tangyuan.mq.xml.XmlMqContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

/**
 * MQ容器
 */
public class MqContainer {

	private static MqContainer		instance		= new MqContainer();

	private XmlMqContext			myContext		= new XmlMqContext();

	private MqSourceManager			mqSourceManager	= null;

	private Map<String, Sender>		senderMap		= null;

	private Map<String, ChannelVo>	channelMap		= null;

	static {
		// 注册上下文工厂
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.MQ, new MqServiceContextFactory());
		// mq 50 30
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(MqServiceComponent.getInstance(), "mq-service", 50, 30));
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(MqListenerComponent.getInstance(), "mq-listener", 50, 30));
	}

	private MqContainer() {
		senderMap = new HashMap<String, Sender>();
		senderMap.put("ActiveMQ".toUpperCase(), new ActiveMqSender());
		senderMap.put("RabbitMQ".toUpperCase(), new RabbitMqSender());
	}

	public static MqContainer getInstance() {
		return instance;
	}

	public XmlMqContext getMyContext() {
		return myContext;
	}

	public MqSourceManager getMqSourceManager() {
		return mqSourceManager;
	}

	public void setMqSourceManager(MqSourceManager mqSourceManager) {
		this.mqSourceManager = mqSourceManager;
	}

	public Sender getSender(String key) {
		return senderMap.get(key);
	}

	public ChannelVo getChannel(String id) {
		return channelMap.get(id);
	}

	public void setChannelMap(Map<String, ChannelVo> channelMap) {
		this.channelMap = channelMap;
	}

}
