package org.xson.tangyuan.mq.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xson.tangyuan.mq.MqComponent;
import org.xson.tangyuan.mq.datasource.MqManagerCreater;
import org.xson.tangyuan.mq.datasource.MqSourceManager;
import org.xson.tangyuan.mq.datasource.MqSourceVo;
import org.xson.tangyuan.mq.datasource.MqSourceVo.MqSourceType;
import org.xson.tangyuan.mq.vo.ActiveMqChannelVo;
import org.xson.tangyuan.mq.vo.ChannelVo;
import org.xson.tangyuan.mq.vo.ChannelVo.ChannelType;
import org.xson.tangyuan.mq.vo.RabbitMqChannelVo;
import org.xson.tangyuan.util.MixedResourceManager;
import org.xson.tangyuan.util.PropertyUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlMqComponentBuilder extends DefaultXmlComponentBuilder {

	private XmlMqContext         componentContext  = null;
	private String               defaultMqSource   = null;
	private Map<String, Integer> duplicateQueueMap = new HashMap<String, Integer>();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		this.componentContext = (XmlMqContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "mq-component", true);
		this.configurationElement();
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();
		this.componentContext = null;
		this.duplicateQueueMap = null;
	}

	private void configurationElement() throws Throwable {
		// 解析配置项目
		buildConfigNode(this.root.evalNodes("config-property"), MqComponent.getInstance());

		buildHostNode(this.root.evalNodes("mqSource"));

		buildQueueNode(this.root.evalNodes("queue"));
		buildTopicNode(this.root.evalNodes("topic"));

		MqComponent.getInstance().setChannelMap(this.componentContext.getChannelVoMap());

		buildPluginNode(this.root.evalNodes("plugin"));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void buildHostNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "mqSource";
		if (0 == contexts.size()) {
			throw new XmlParseException(lang("xml.tag.miss", tagName, this.resource));
		}

		String _defaultMqSource = null;

		for (XmlNodeWrapper xNode : contexts) {

			String  id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String  _type     = getStringFromAttr(xNode, "type", lang("xml.tag.attribute.empty", "type", tagName, this.resource));
			boolean defaultDs = getBoolFromAttr(xNode, "isDefault", false);
			String  resource  = getStringFromAttr(xNode, "resource");

			if (this.componentContext.getMqSourceMap().containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			MqSourceType type = getMqSourceType(_type);
			if (null == type) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid", "type", tagName, this.resource));
			}

			_defaultMqSource = id;
			if (defaultDs) {
				if (null != this.defaultMqSource) {
					throw new XmlParseException(lang("xml.tag.mostone.default", tagName));
				}
				this.defaultMqSource = id;
			}

			Map<String, String> data = null;
			if (null != resource) {
				Properties p = MixedResourceManager.getProperties(resource, true, true);
				data = (Map) p;
			} else {
				data = new HashMap<String, String>();
				List<XmlNodeWrapper> properties = xNode.evalNodes("property");
				for (XmlNodeWrapper propertyNode : properties) {
					String name  = getStringFromAttr(propertyNode, "name", lang("xml.tag.attribute.empty", "name", tagName + ".property", this.resource));
					String value = getStringFromAttr(propertyNode, "value", lang("xml.tag.attribute.empty", "value", tagName + ".property", this.resource));
					data.put(name, value);
				}
			}
			// key转大写
			data = PropertyUtils.keyToUpperCase(data);

			MqSourceVo hostVo = new MqSourceVo(id, type, data, resource);
			this.componentContext.getMqSourceMap().put(id, hostVo);

			log.info(lang("add.tag"), tagName, id);
		}

		if (1 == contexts.size()) {
			this.defaultMqSource = _defaultMqSource;
		}
		this.componentContext.setDefaultMqSource(defaultMqSource);

		MqSourceManager manager = new MqManagerCreater().create(defaultMqSource, this.componentContext.getMqSourceMap());
		MqComponent.getInstance().setMqSourceManager(manager);
	}

	private void buildQueueNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "queue";
		for (XmlNodeWrapper xNode : contexts) {

			String id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String queueName = getStringFromAttr(xNode, "queueName");
			String msKey     = getStringFromAttr(xNode, "msKey");

			if (this.componentContext.getChannelVoMap().containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			if (StringUtils.isEmptySafe(queueName)) {
				queueName = id;
			}
			if (StringUtils.isEmptySafe(msKey)) {
				msKey = defaultMqSource;
			}
			if (null == msKey || !this.componentContext.getMqSourceMap().containsKey(msKey)) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid", "msKey", tagName, this.resource));
			}

			if (duplicateQueueMap.containsKey(queueName + "_" + msKey)) {
				throw new XmlParseException(lang("xml.tag.repeated", queueName + "_" + msKey, tagName, this.resource));
			}
			duplicateQueueMap.put(queueName + "_" + msKey, 1);

			Map<String, String>  data       = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				String name  = getStringFromAttr(propertyNode, "name", lang("xml.tag.attribute.empty", "name", tagName + ".property", this.resource));
				String value = getStringFromAttr(propertyNode, "value", lang("xml.tag.attribute.empty", "value", tagName + ".property", this.resource));
				data.put(name.toUpperCase(), value);
			}

			String    senderKey = null;
			ChannelVo qVo       = null;
			if (MqSourceType.ActiveMQ == this.componentContext.getMqSourceMap().get(msKey).getType()) {
				senderKey = MqSourceType.ActiveMQ.toString().toUpperCase();
				qVo = new ActiveMqChannelVo(id, queueName, msKey, ChannelType.Queue, senderKey, data);
			} else if (MqSourceType.RabbitMQ == this.componentContext.getMqSourceMap().get(msKey).getType()) {
				senderKey = MqSourceType.RabbitMQ.toString().toUpperCase();
				qVo = new RabbitMqChannelVo(id, queueName, msKey, ChannelType.Queue, senderKey, data);
			}

			this.componentContext.getChannelVoMap().put(id, qVo);

			log.info(lang("add.tag"), tagName, id);
		}
	}

	private void buildTopicNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "topic";
		for (XmlNodeWrapper xNode : contexts) {

			String id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String queueName = getStringFromAttr(xNode, "topicName");
			String msKey     = getStringFromAttr(xNode, "msKey");

			if (this.componentContext.getChannelVoMap().containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			if (StringUtils.isEmptySafe(queueName)) {
				queueName = id;
			}
			if (StringUtils.isEmptySafe(msKey)) {
				msKey = defaultMqSource;
			}
			if (null == msKey || !this.componentContext.getMqSourceMap().containsKey(msKey)) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid", "msKey", tagName, this.resource));
			}

			if (duplicateQueueMap.containsKey(queueName + "_" + msKey)) {
				throw new XmlParseException(lang("xml.tag.repeated", queueName + "_" + msKey, tagName, this.resource));
			}
			duplicateQueueMap.put(queueName + "_" + msKey, 1);

			Map<String, String>  data       = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				String name  = getStringFromAttr(propertyNode, "name", lang("xml.tag.attribute.empty", "name", tagName + ".property", this.resource));
				String value = getStringFromAttr(propertyNode, "value", lang("xml.tag.attribute.empty", "value", tagName + ".property", this.resource));
				data.put(name.toUpperCase(), value);
			}

			String    senderKey = null;
			ChannelVo qVo       = null;
			if (MqSourceType.ActiveMQ == this.componentContext.getMqSourceMap().get(msKey).getType()) {
				senderKey = MqSourceType.ActiveMQ.toString().toUpperCase();
				qVo = new ActiveMqChannelVo(id, queueName, msKey, ChannelType.Topic, senderKey, data);
			} else if (MqSourceType.RabbitMQ == this.componentContext.getMqSourceMap().get(msKey).getType()) {
				senderKey = MqSourceType.RabbitMQ.toString().toUpperCase();
				qVo = new RabbitMqChannelVo(id, queueName, msKey, ChannelType.Topic, senderKey, data);
			}

			this.componentContext.getChannelVoMap().put(id, qVo);

			log.info(lang("add.tag"), tagName, id);
		}
	}

	private void buildPluginNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		String                   tagName        = "plugin";
		List<XmlMqPluginBuilder> pluginBuilders = new ArrayList<XmlMqPluginBuilder>();
		// 扫描所有的plugin
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper     xNode    = contexts.get(i);
			String             resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
			XmlMqPluginBuilder builder  = new XmlMqPluginBuilder();
			builder.setContext(resource, this.componentContext);
			// first
			builder.parseRef();
			pluginBuilders.add(builder);
		}
		// 注册所有的服务
		for (int i = 0; i < size; i++) {
			pluginBuilders.get(i).parseService();
			pluginBuilders.get(i).clean();
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

	//	private void buildPluginNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		int size = contexts.size();
	//		if (size == 0) {
	//			return;
	//		}
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode    = contexts.get(i);
	//			String         resource = StringUtils.trim(xNode.getStringAttribute("resource"));
	//			// log.info("Start parsing(1): " + resource);
	//			log.info("Start parsing: " + resource);
	//			//InputStream inputStream = Resources.getResourceAsStream(resource);
	//			InputStream    inputStream    = ResourceManager.getInputStream(resource, false);
	//			XPathParser    parser         = new XPathParser(inputStream);
	//			XmlNodeBuilder xmlNodeBuilder = getXmlNodeBuilder(parser);
	//			xmlNodeBuilder.parseService();
	//		}
	//	}

	//	private XmlNodeBuilder getXmlNodeBuilder(XPathParser parser) {
	//		XmlNodeWrapper _root = null;
	//		if (null != (_root = parser.evalNode("/mqservices"))) {
	//			XmlNodeBuilder nodeBuilder = new XMLMqNodeBuilder();
	//			nodeBuilder.setContext(_root, this.context);
	//			return nodeBuilder;
	//		} else {
	//			throw new XmlParseException("Unsupported root node in the service plug-in");
	//		}
	//	}

	/////////////////////

	//			Map<String, String> data = new HashMap<String, String>();
	//			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
	//			for (XmlNodeWrapper propertyNode : properties) {
	//				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
	//						StringUtils.trim(propertyNode.getStringAttribute("value")));
	//			}
	//	String  id        = StringUtils.trim(xNode.getStringAttribute("id"));
	//			MqSourceType type = null;
	//			//			String       _type = StringUtils.trim(xNode.getStringAttribute("type"));
	//			type = getMqSourceType(_type);
	//			if (null == type) {
	//				throw new XmlParseException("Unsupported MQ types in <mqSource>: " + id);
	//			}
	//			boolean defaultMs  = false;
	//			String  _isDefault = StringUtils.trim(xNode.getStringAttribute("isDefault"));

	//	@SuppressWarnings({ "unchecked", "rawtypes" })
	//	private void buildHostNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//
	//		if (0 == contexts.size()) {
	//			throw new XmlParseException("Missing <mqSource> node.");
	//		}
	//
	//		String _defaultMqSource = null;
	//
	//		for (XmlNodeWrapper xNode : contexts) {
	//			String id = StringUtils.trim(xNode.getStringAttribute("id"));
	//			if (context.getMqSourceMap().containsKey(id)) {
	//				throw new XmlParseException("Duplicate <mqSource> id: " + id);
	//			}
	//			_defaultMqSource = id;
	//
	//			MqSourceType type  = null;
	//			String       _type = StringUtils.trim(xNode.getStringAttribute("type"));
	//			type = getMqSourceType(_type);
	//			if (null == type) {
	//				throw new XmlParseException("Unsupported MQ types in <mqSource>: " + id);
	//			}
	//
	//			boolean defaultMs  = false;
	//			String  _isDefault = StringUtils.trim(xNode.getStringAttribute("isDefault"));
	//			if (null != _isDefault) {
	//				defaultMs = Boolean.parseBoolean(_isDefault);
	//				if (defaultMs) {
	//					if (null != this.defaultMqSource) {
	//						throw new XMLParseException("The default mqSource can only have one");
	//					}
	//					this.defaultMqSource = id;
	//				}
	//			}
	//
	//			//			Map<String, String> data = new HashMap<String, String>();
	//			//			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
	//			//			for (XmlNodeWrapper propertyNode : properties) {
	//			//				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
	//			//						StringUtils.trim(propertyNode.getStringAttribute("value")));
	//			//			}
	//
	//			Map<String, String> data     = null;
	//			String              resource = StringUtils.trim(xNode.getStringAttribute("resource"));
	//			if (null != resource) {
	//				data = (Map) ResourceManager.getProperties(resource, true);
	//				data = PropertyUtils.keyToUpperCase(data); // 还需要将Key全变成大写
	//			} else {
	//				data = new HashMap<String, String>();
	//				List<XmlNodeWrapper> properties = xNode.evalNodes("property");
	//				for (XmlNodeWrapper propertyNode : properties) {
	//					data.put(StringUtils.trim(propertyNode.getStringAttribute("name")), StringUtils.trim(propertyNode.getStringAttribute("value")));
	//				}
	//				PlaceholderResourceSupport.processMap(data); // 占位替换
	//				data = PropertyUtils.keyToUpperCase(data); // key转大写
	//			}
	//
	//			MqSourceVo hostVo = new MqSourceVo(id, type, data, resource);
	//			context.getMqSourceMap().put(id, hostVo);
	//			log.info("add mq source: " + id);
	//		}
	//
	//		// fix bug
	//		if (1 == contexts.size()) {
	//			this.defaultMqSource = _defaultMqSource;
	//		}
	//
	//		this.context.setDefaultMqSource(defaultMqSource);
	//
	//		MqSourceManager manager = new MqManagerCreater().create(defaultMqSource, context.getMqSourceMap());
	//		MqContainer.getInstance().setMqSourceManager(manager);
	//	}

	//	private void buildQueueNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		if (0 == contexts.size()) {
	//			return;
	//		}
	//		for (XmlNodeWrapper xNode : contexts) {
	//
	//			String id        = StringUtils.trim(xNode.getStringAttribute("id"));
	//			String queueName = StringUtils.trim(xNode.getStringAttribute("queueName"));
	//			String msKey     = StringUtils.trim(xNode.getStringAttribute("msKey"));
	//
	//			if (context.getChannelVoMap().containsKey(id)) {
	//				throw new XmlParseException("Duplicate queue id: " + id);
	//			}
	//			if (null == queueName || 0 == queueName.length()) {
	//				// throw new XmlParseException("The queue name can not be empty.");
	//				queueName = id;
	//			}
	//			if (null == msKey || 0 == msKey.length()) {
	//				msKey = defaultMqSource;
	//			}
	//			if (null == msKey) {
	//				throw new XmlParseException("The msKey can not be empty.");
	//			}
	//			if (!context.getMqSourceMap().containsKey(msKey)) {
	//				throw new XmlParseException("Invalid msKey: " + msKey);
	//			}
	//			if (duplicateQueueMap.containsKey(queueName + "_" + msKey)) {
	//				throw new XmlParseException("Duplicate queue name: " + queueName);
	//			}
	//			duplicateQueueMap.put(queueName + "_" + msKey, 1);
	//
	//			Map<String, String>  data       = new HashMap<String, String>();
	//			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
	//			for (XmlNodeWrapper propertyNode : properties) {
	//				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(), StringUtils.trim(propertyNode.getStringAttribute("value")));
	//			}
	//
	//			String    senderKey = null;
	//			ChannelVo qVo       = null;
	//			if (MqSourceType.ActiveMQ == context.getMqSourceMap().get(msKey).getType()) {
	//				senderKey = MqSourceType.ActiveMQ.toString().toUpperCase();
	//				qVo = new ActiveMqChannelVo(id, queueName, msKey, ChannelType.Queue, senderKey, data);
	//			} else if (MqSourceType.RabbitMQ == context.getMqSourceMap().get(msKey).getType()) {
	//				senderKey = MqSourceType.RabbitMQ.toString().toUpperCase();
	//				qVo = new RabbitMqChannelVo(id, queueName, msKey, ChannelType.Queue, senderKey, data);
	//			}
	//
	//			context.getChannelVoMap().put(id, qVo);
	//			log.info("add mq queue: " + id);
	//		}
	//	}

	//	private void buildTopicNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		if (0 == contexts.size()) {
	//			return;
	//		}
	//		for (XmlNodeWrapper xNode : contexts) {
	//
	//			String id        = StringUtils.trim(xNode.getStringAttribute("id"));
	//			String queueName = StringUtils.trim(xNode.getStringAttribute("topicName"));
	//			String msKey     = StringUtils.trim(xNode.getStringAttribute("msKey"));
	//
	//			if (context.getChannelVoMap().containsKey(id)) {
	//				throw new XmlParseException("Duplicate topic id: " + id);
	//			}
	//			if (null == queueName || 0 == queueName.length()) {
	//				// throw new XmlParseException("The topic name can not be empty.");
	//				queueName = id;
	//			}
	//			if (null == msKey || 0 == msKey.length()) {
	//				msKey = defaultMqSource;
	//			}
	//			if (null == msKey) {
	//				throw new XmlParseException("The msKey can not be empty.");
	//			}
	//			if (!context.getMqSourceMap().containsKey(msKey)) {
	//				throw new XmlParseException("Invalid msKey: " + msKey);
	//			}
	//			if (duplicateQueueMap.containsKey(queueName + "_" + msKey)) {
	//				throw new XmlParseException("Duplicate topic name: " + queueName);
	//			}
	//			duplicateQueueMap.put(queueName + "_" + msKey, 1);
	//
	//			Map<String, String>  data       = new HashMap<String, String>();
	//			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
	//			for (XmlNodeWrapper propertyNode : properties) {
	//				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(), StringUtils.trim(propertyNode.getStringAttribute("value")));
	//			}
	//
	//			String    senderKey = null;
	//			ChannelVo qVo       = null;
	//			if (MqSourceType.ActiveMQ == context.getMqSourceMap().get(msKey).getType()) {
	//				senderKey = MqSourceType.ActiveMQ.toString().toUpperCase();
	//				qVo = new ActiveMqChannelVo(id, queueName, msKey, ChannelType.Topic, senderKey, data);
	//			} else if (MqSourceType.RabbitMQ == context.getMqSourceMap().get(msKey).getType()) {
	//				senderKey = MqSourceType.RabbitMQ.toString().toUpperCase();
	//				qVo = new RabbitMqChannelVo(id, queueName, msKey, ChannelType.Topic, senderKey, data);
	//			}
	//
	//			context.getChannelVoMap().put(id, qVo);
	//			log.info("add mq topic: " + id);
	//		}
	//	}

	//	private Log                  log               = LogFactory.getLog(getClass());
	//	private XPathParser          xPathParser       = null;
	//	private XmlMqContext         context           = MqContainer.getInstance().getMyContext();
	//	@Override
	//	public void parse(XmlContext xmlContext, String resource) throws Throwable {
	//		context.setXmlContext((XmlGlobalContext) xmlContext);
	//		log.info("*** Start parsing: " + resource);
	//		//		InputStream inputStream = Resources.getResourceAsStream(resource);
	//		//		inputStream = PlaceholderResourceSupport.processInputStream(inputStream,
	//		//				TangYuanContainer.getInstance().getXmlGlobalContext().getPlaceholderMap());
	//
	//		InputStream inputStream = ResourceManager.getInputStream(resource, true);
	//
	//		this.xPathParser = new XPathParser(inputStream);
	//		configurationElement(xPathParser.evalNode("/mq-component"));
	//		context.clean();
	//
	//		inputStream.close();
	//	}

	//	private void configurationElement(XmlNodeWrapper context) throws Throwable {
	//		buildHostNodes(context.evalNodes("mqSource"));
	//		buildQueueNodes(context.evalNodes("queue"));
	//		buildTopicNodes(context.evalNodes("topic"));
	//		// buildTopicNodes(context.evalNodes("exchange"));
	//
	//		MqContainer.getInstance().setChannelMap(this.context.getChannelVoMap());
	//		buildPluginNodes(context.evalNodes("plugin"));
	//	}
}
