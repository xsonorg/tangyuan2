package org.xson.tangyuan.mq.xml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.modelmbean.XMLParseException;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.mq.MqContainer;
import org.xson.tangyuan.mq.datasource.MqManagerCreater;
import org.xson.tangyuan.mq.datasource.MqSourceManager;
import org.xson.tangyuan.mq.datasource.MqSourceVo;
import org.xson.tangyuan.mq.datasource.MqSourceVo.MqSourceType;
import org.xson.tangyuan.mq.vo.ActiveMqChannelVo;
import org.xson.tangyuan.mq.vo.ChannelVo;
import org.xson.tangyuan.mq.vo.ChannelVo.ChannelType;
import org.xson.tangyuan.mq.vo.RabbitMqChannelVo;
import org.xson.tangyuan.mq.xml.node.XMLMqNodeBuilder;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlExtendBuilder;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlNodeBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlMqBuilder implements XmlExtendBuilder {

	private Log						log					= LogFactory.getLog(getClass());
	private XPathParser				xPathParser			= null;
	private XmlMqContext			context				= MqContainer.getInstance().getMyContext();
	private String					defaultMqSource		= null;
	private Map<String, Integer>	duplicateQueueMap	= new HashMap<String, Integer>();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		context.setXmlContext((XmlGlobalContext) xmlContext);
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = Resources.getResourceAsStream(resource);
		this.xPathParser = new XPathParser(inputStream);
		configurationElement(xPathParser.evalNode("/mq-component"));
		context.clean();
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildHostNodes(context.evalNodes("mqSource"));
		buildQueueNodes(context.evalNodes("queue"));
		buildTopicNodes(context.evalNodes("topic"));
		// buildTopicNodes(context.evalNodes("exchange"));

		MqContainer.getInstance().setChannelMap(this.context.getChannelVoMap());
		buildPluginNodes(context.evalNodes("plugin"));
	}

	private void buildHostNodes(List<XmlNodeWrapper> contexts) throws Throwable {

		if (0 == contexts.size()) {
			throw new XmlParseException("Missing <mqSource> node.");
		}

		String _defaultMqSource = null;

		for (XmlNodeWrapper xNode : contexts) {
			String id = StringUtils.trim(xNode.getStringAttribute("id"));
			if (context.getMqSourceMap().containsKey(id)) {
				throw new XmlParseException("Duplicate <mqSource> id: " + id);
			}
			_defaultMqSource = id;

			MqSourceType type = null;
			String _type = StringUtils.trim(xNode.getStringAttribute("type"));
			type = getMqSourceType(_type);
			if (null == type) {
				throw new XmlParseException("Unsupported MQ types in <mqSource>: " + id);
			}

			boolean defaultMs = false;
			String _isDefault = StringUtils.trim(xNode.getStringAttribute("isDefault"));
			if (null != _isDefault) {
				defaultMs = Boolean.parseBoolean(_isDefault);
				if (defaultMs) {
					if (null != this.defaultMqSource) {
						throw new XMLParseException("The default mqSource can only have one");
					}
					this.defaultMqSource = id;
				}
			}

			Map<String, String> data = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
						StringUtils.trim(propertyNode.getStringAttribute("value")));
			}
			MqSourceVo hostVo = new MqSourceVo(id, type, data);
			context.getMqSourceMap().put(id, hostVo);
			log.info("add mq source: " + id);
		}

		// fix bug
		if (1 == contexts.size()) {
			this.defaultMqSource = _defaultMqSource;
		}

		this.context.setDefaultMqSource(defaultMqSource);

		MqSourceManager manager = new MqManagerCreater().create(defaultMqSource, context.getMqSourceMap());
		MqContainer.getInstance().setMqSourceManager(manager);
	}

	private void buildQueueNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		if (0 == contexts.size()) {
			return;
		}
		for (XmlNodeWrapper xNode : contexts) {

			String id = StringUtils.trim(xNode.getStringAttribute("id"));
			String queueName = StringUtils.trim(xNode.getStringAttribute("queueName"));
			String msKey = StringUtils.trim(xNode.getStringAttribute("msKey"));

			if (context.getChannelVoMap().containsKey(id)) {
				throw new XmlParseException("Duplicate queue id: " + id);
			}
			if (null == queueName || 0 == queueName.length()) {
				// throw new XmlParseException("The queue name can not be empty.");
				queueName = id;
			}
			if (null == msKey || 0 == msKey.length()) {
				msKey = defaultMqSource;
			}
			if (null == msKey) {
				throw new XmlParseException("The msKey can not be empty.");
			}
			if (!context.getMqSourceMap().containsKey(msKey)) {
				throw new XmlParseException("Invalid msKey: " + msKey);
			}
			if (duplicateQueueMap.containsKey(queueName + "_" + msKey)) {
				throw new XmlParseException("Duplicate queue name: " + queueName);
			}
			duplicateQueueMap.put(queueName + "_" + msKey, 1);

			Map<String, String> data = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
						StringUtils.trim(propertyNode.getStringAttribute("value")));
			}

			String senderKey = null;
			ChannelVo qVo = null;
			if (MqSourceType.ActiveMQ == context.getMqSourceMap().get(msKey).getType()) {
				senderKey = MqSourceType.ActiveMQ.toString().toUpperCase();
				qVo = new ActiveMqChannelVo(id, queueName, msKey, ChannelType.Queue, senderKey, data);
			} else if (MqSourceType.RabbitMQ == context.getMqSourceMap().get(msKey).getType()) {
				senderKey = MqSourceType.RabbitMQ.toString().toUpperCase();
				qVo = new RabbitMqChannelVo(id, queueName, msKey, ChannelType.Queue, senderKey, data);
			}

			context.getChannelVoMap().put(id, qVo);
			log.info("add mq queue: " + id);
		}
	}

	private void buildTopicNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		if (0 == contexts.size()) {
			return;
		}
		for (XmlNodeWrapper xNode : contexts) {

			String id = StringUtils.trim(xNode.getStringAttribute("id"));
			String queueName = StringUtils.trim(xNode.getStringAttribute("topicName"));
			String msKey = StringUtils.trim(xNode.getStringAttribute("msKey"));

			if (context.getChannelVoMap().containsKey(id)) {
				throw new XmlParseException("Duplicate topic id: " + id);
			}
			if (null == queueName || 0 == queueName.length()) {
				// throw new XmlParseException("The topic name can not be empty.");
				queueName = id;
			}
			if (null == msKey || 0 == msKey.length()) {
				msKey = defaultMqSource;
			}
			if (null == msKey) {
				throw new XmlParseException("The msKey can not be empty.");
			}
			if (!context.getMqSourceMap().containsKey(msKey)) {
				throw new XmlParseException("Invalid msKey: " + msKey);
			}
			if (duplicateQueueMap.containsKey(queueName + "_" + msKey)) {
				throw new XmlParseException("Duplicate topic name: " + queueName);
			}
			duplicateQueueMap.put(queueName + "_" + msKey, 1);

			Map<String, String> data = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
						StringUtils.trim(propertyNode.getStringAttribute("value")));
			}

			String senderKey = null;
			ChannelVo qVo = null;
			if (MqSourceType.ActiveMQ == context.getMqSourceMap().get(msKey).getType()) {
				senderKey = MqSourceType.ActiveMQ.toString().toUpperCase();
				qVo = new ActiveMqChannelVo(id, queueName, msKey, ChannelType.Topic, senderKey, data);
			} else if (MqSourceType.RabbitMQ == context.getMqSourceMap().get(msKey).getType()) {
				senderKey = MqSourceType.RabbitMQ.toString().toUpperCase();
				qVo = new RabbitMqChannelVo(id, queueName, msKey, ChannelType.Topic, senderKey, data);
			}

			context.getChannelVoMap().put(id, qVo);
			log.info("add mq topic: " + id);
		}
	}

	private void buildPluginNodes(List<XmlNodeWrapper> contexts) throws Exception {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String resource = StringUtils.trim(xNode.getStringAttribute("resource"));
			// log.info("Start parsing(1): " + resource);
			log.info("Start parsing: " + resource);
			InputStream inputStream = Resources.getResourceAsStream(resource);
			XPathParser parser = new XPathParser(inputStream);
			XmlNodeBuilder xmlNodeBuilder = getXmlNodeBuilder(parser);
			xmlNodeBuilder.parseService();
		}
	}

	private XmlNodeBuilder getXmlNodeBuilder(XPathParser parser) {
		XmlNodeWrapper _root = null;
		if (null != (_root = parser.evalNode("/mqservices"))) {
			XmlNodeBuilder nodeBuilder = new XMLMqNodeBuilder();
			nodeBuilder.setContext(_root, this.context);
			return nodeBuilder;
		} else {
			throw new XmlParseException("Unsupported root node in the service plug-in");
		}
	}

	private MqSourceType getMqSourceType(String str) {
		if ("ActiveMQ".equalsIgnoreCase(str)) {
			return MqSourceType.ActiveMQ;
		} else if ("RabbitMQ".equalsIgnoreCase(str)) {
			return MqSourceType.RabbitMQ;
		}
		return null;
	}
}
