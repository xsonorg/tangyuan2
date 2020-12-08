package org.xson.tangyuan.mq.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.modelmbean.XMLParseException;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mq.datasource.MqSourceVo.MqSourceType;
import org.xson.tangyuan.mq.vo.BindingPattern;
import org.xson.tangyuan.mq.vo.BindingVo;
import org.xson.tangyuan.mq.vo.ChannelVo;
import org.xson.tangyuan.mq.vo.ChannelVo.ChannelType;
import org.xson.tangyuan.mq.vo.ListenerVo;
import org.xson.tangyuan.mq.vo.RoutingVo;
import org.xson.tangyuan.mq.vo.ServiceVo;
import org.xson.tangyuan.mq.xml.XmlMqContext;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlMqPluginBuilder extends XmlNodeBuilder {

	private Log				log		= LogFactory.getLog(getClass());
	private XmlNodeWrapper	root	= null;
	private XmlMqContext	context	= null;

	@Override
	public Log getLog() {
		return this.log;
	}

	@Override
	public void setContext(XmlNodeWrapper root, XmlContext context) {
		this.context = (XmlMqContext) context;
		this.root = root;
		this.ns = this.root.getStringAttribute("ns", "");
		// TODO 需要增加版本号
		if (this.ns.length() > 0) {
			this.context.getXmlContext().checkNs(this.ns);
		}
	}

	@Override
	public void parseRef() {
	}

	@Override
	public void parseService() {
		try {
			configurationElement(this.root);
		} catch (Throwable e) {
			throw new TangYuanException(e);
		}
	}

	protected String getFullId(String id) {
		return TangYuanUtil.getQualifiedName(this.ns, id, null, TangYuanContainer.getInstance().getNsSeparator());
	}

	private void existingService(String id) {
		if (null == id || 0 == id.length()) {
			throw new XmlParseException("Service ID can not be empty.");
		}
		String fullId = getFullId(id);
		if (null != this.context.getXmlContext().getIntegralServiceMap().get(fullId)) {
			throw new XmlParseException("Duplicate service nodes: " + fullId);
		}
		if (null != this.context.getXmlContext().getIntegralRefMap().get(fullId)) {
			throw new XmlParseException("Duplicate service nodes: " + fullId);
		}
		this.context.getXmlContext().getIntegralServiceMap().put(fullId, 1);
	}

	private void existingListenerService(String service) {
		if (null == service || 0 == service.length()) {
			throw new XmlParseException("Service ID can not be empty.");
		}
		if (null == this.context.getXmlContext().getIntegralServiceMap().get(service)) {
			throw new XmlParseException("Non-existent service: " + service);
		}
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildServiceNodes(context.evalNodes("mq-service"));
		buildListenerNodes(context.evalNodes("mq-listener"));
	}

	private void buildServiceNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		for (XmlNodeWrapper xNode : contexts) {
			String id = StringUtils.trim(xNode.getStringAttribute("id")); // xml v
			String channel = StringUtils.trim(xNode.getStringAttribute("channels"));
			String _useTx = StringUtils.trim(xNode.getStringAttribute("useTx"));
			// check id
			existingService(id);

			Map<String, RoutingVo> routingMap = new HashMap<String, RoutingVo>();
			String[] mChannels = getChannels(channel);
			if (null != mChannels) {
				for (int i = 0; i < mChannels.length; i++) {
					if (!context.getChannelVoMap().containsKey(mChannels[i])) {
						throw new XmlParseException("Invalid attribute channel: " + mChannels[i]);
					}
					routingMap.put(mChannels[i], null);
				}
			}

			boolean useTx = true;
			if (null != _useTx) {
				useTx = Boolean.parseBoolean(_useTx);
			}

			// <routing channel="q0,q1,q2" key="{x}" pattern="a,b,c" />
			List<XmlNodeWrapper> innerNodeList = xNode.evalNodes("routing");
			for (XmlNodeWrapper innerNode : innerNodeList) {
				String routingChannel = StringUtils.trim(innerNode.getStringAttribute("channels"));
				String routingKey = StringUtils.trim(innerNode.getStringAttribute("key"));
				String routingPattern = StringUtils.trim(innerNode.getStringAttribute("pattern"));

				String[] sChannels = getChannels(routingChannel);
				if (null == sChannels) {
					throw new XmlParseException("the channel is not allowed to be empty in <routing> node. mq-service: " + id);
				}

				if ("".equals(routingKey)) {
					routingKey = null;
				}
				if ("".equals(routingPattern)) {
					routingPattern = null;
				}

				if (null == routingKey && null == routingPattern) {
					throw new XmlParseException("<routing> node key and pattern is empty. mq-service: " + id);
				}

				Object vKey = null;
				if (null != routingKey) {
					if (!checkVar(routingKey)) {
						throw new XMLParseException("If key attribute exists, it must be a variable, such as {x}. mq-service:" + id);
					}
					vKey = new NormalParser().parse(getRealVal(routingKey));
				}

				boolean patternMatch = false;
				if (null != routingPattern && routingPattern.indexOf("*") > -1) {
					patternMatch = true;
				}

				boolean keyAndPattern = true;
				if (null == vKey || null == routingPattern) {
					keyAndPattern = false;
				}

				int exchangeCount = 0;
				for (int i = 0; i < sChannels.length; i++) {
					if (!context.getChannelVoMap().containsKey(sChannels[i])) {
						throw new XmlParseException("Invalid attribute channel: " + sChannels[i]);
					}

					// exchange和非exchange不能再一个routing中
					ChannelVo qVo = context.getChannelVoMap().get(sChannels[i]);
					if (ChannelType.Topic == qVo.getType() && MqSourceType.RabbitMQ == context.getMqSourceMap().get(qVo.getMsKey()).getType()) {
						exchangeCount++;
					}

					RoutingVo rVo = routingMap.get(sChannels[i]);
					if (null != rVo) {
						throw new XmlParseException("Duplicate routing settings for channel: " + sChannels[i]);
					}
					rVo = new RoutingVo(sChannels[i], vKey, routingPattern, patternMatch);
					routingMap.put(sChannels[i], rVo);
				}

				// exchange和非exchange不能再一个routing中
				if (exchangeCount > 0 && exchangeCount != sChannels.length) {
					throw new XMLParseException("exchange and non-exchange can no longer be in a routing node. mq-service:" + id);
				}

				// 对于RabbitMQ.exchange: key和pattern，只能存在一个
				if (exchangeCount > 0 && keyAndPattern) {
					throw new XMLParseException("for exchange, key and pattern can only exist one. mq-service:" + id);
				}

				// 对于其他:key和pattern必须都存在,key必须为变量
				if (0 == exchangeCount && !keyAndPattern) {
					throw new XMLParseException("for non-exchange, key and pattern must exist, and key must be a variable. mq-service:" + id);
				}
			}

			// check channel
			if (0 == routingMap.size()) {
				throw new XmlParseException("<mq-service> node channel attribute can not be empty. mq-service:" + id);
			}

			String[] channels = routingMap.keySet().toArray(new String[routingMap.size()]);

			if (0 == innerNodeList.size()) {
				routingMap = null;
			}

			ServiceVo sVo = new ServiceVo(id, ns, getFullId(id), channels, useTx, routingMap);
			context.getServiceVoList().add(sVo);
		}
	}

	private void buildListenerNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		for (XmlNodeWrapper xNode : contexts) {
			String service = StringUtils.trim(xNode.getStringAttribute("service"));
			String channel = StringUtils.trim(xNode.getStringAttribute("channel"));
			// check id
			existingListenerService(service);

			if (!context.getChannelVoMap().containsKey(channel)) {
				throw new XmlParseException("Invalid attribute channel: " + channel);
			}

			BindingVo binding = null;

			List<XmlNodeWrapper> innerNodeList = xNode.evalNodes("binding");

			if (innerNodeList.size() > 1) {
				throw new XMLParseException("the <binding> node can have at most one.");
			}

			if (1 == innerNodeList.size()) {
				XmlNodeWrapper innerNode = innerNodeList.get(0);

				String bindingKey = StringUtils.trim(innerNode.getStringAttribute("key"));
				String bindingPattern = StringUtils.trim(innerNode.getStringAttribute("pattern"));
				String bindingSeparator = StringUtils.trim(innerNode.getStringAttribute("separator"));

				if ("".equals(bindingKey)) {
					bindingKey = null;
				}
				if ("".equals(bindingPattern)) {
					bindingPattern = null;
				}
				if ("".equals(bindingSeparator)) {
					bindingSeparator = null;
				}
				if (null == bindingSeparator) {
					bindingSeparator = ",";
				}

				if (null == bindingKey && null == bindingPattern) {
					throw new XmlParseException("<binding> node key and pattern is empty. mq-listener: " + service);
				}

				// <binding key="{x}" pattern="abc,ef,g" separator=","/>

				Object vKey = null;
				if (null != bindingKey) {
					if (!checkVar(bindingKey)) {
						throw new XMLParseException("If key attribute exists, it must be a variable, such as {x}. mq-listener: " + service);
					}
					vKey = new NormalParser().parse(getRealVal(bindingKey));
				}

				List<BindingPattern> bindingPatternList = null;
				String[] patterns = split(bindingPattern, bindingSeparator);
				if (null != patterns) {
					bindingPatternList = new ArrayList<BindingPattern>();
					for (int i = 0; i < patterns.length; i++) {
						boolean patternValueMatch = false;
						if (patterns[i].indexOf("*") > -1) {
							patternValueMatch = true;
						}
						bindingPatternList.add(new BindingPattern(patterns[i], patternValueMatch));
					}
				}

				boolean keyAndPattern = true;
				if (null == vKey || null == bindingPatternList) {
					keyAndPattern = false;
				}

				boolean exchange = false;
				ChannelVo qVo = context.getChannelVoMap().get(channel);
				if (ChannelType.Topic == qVo.getType() && MqSourceType.RabbitMQ == context.getMqSourceMap().get(qVo.getMsKey()).getType()) {
					exchange = true;
				}

				// 对于RabbitMQ.exchange: key和pattern，只能存在一个
				if (exchange && null == bindingPatternList) {
					throw new XMLParseException("for exchange, pattern can not be empty. mq-listener: " + service);
				}

				// 对于其他:key和pattern必须都存在,key必须为变量
				if (!exchange && !keyAndPattern) {
					throw new XMLParseException("for non-exchange, key and pattern must exist, and key must be a variable. mq-listener: " + service);
				}

				binding = new BindingVo(channel, vKey, bindingPatternList);
			}

			ListenerVo lVo = new ListenerVo(service, channel, binding);
			this.context.getListenerVoList().add(lVo);
		}
	}

	private String[] getChannels(String channel) {
		if (null == channel || 0 == channel.length()) {
			return null;
		}
		String[] temp = channel.split(",");
		String[] channels = new String[temp.length];
		for (int i = 0; i < channels.length; i++) {
			channels[i] = temp[i].trim();
		}
		return channels;
	}

	private String[] split(String str, String separator) {
		if (null == str || 0 == str.length()) {
			return null;
		}
		String[] temp = str.split(separator);
		String[] channels = new String[temp.length];
		for (int i = 0; i < channels.length; i++) {
			channels[i] = temp[i].trim();
		}
		return channels;
	}

}
