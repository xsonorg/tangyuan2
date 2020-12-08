package org.xson.tangyuan.mq.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.mq.datasource.MqSourceVo;
import org.xson.tangyuan.mq.vo.ChannelVo;
import org.xson.tangyuan.mq.vo.ListenerVo;
import org.xson.tangyuan.mq.vo.ServiceVo;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;

public class XmlMqContext2 implements XmlContext {

	private XmlGlobalContext		xmlContext		= null;

	private Map<String, MqSourceVo>	mqSourceMap		= new HashMap<String, MqSourceVo>();
	private String					defaultMqSource	= null;

	private List<ListenerVo>		listenerVoList	= new ArrayList<ListenerVo>();
	private List<ServiceVo>			serviceVoList	= new ArrayList<ServiceVo>();
	private Map<String, ChannelVo>	channelVoMap	= new HashMap<String, ChannelVo>();

	public void clean() {
		// TODO
	}

	public XmlGlobalContext getXmlContext() {
		return xmlContext;
	}

	public void setXmlContext(XmlGlobalContext xmlContext) {
		this.xmlContext = xmlContext;
	}

	public Map<String, MqSourceVo> getMqSourceMap() {
		return mqSourceMap;
	}

	public String getDefaultMqSource() {
		return defaultMqSource;
	}

	public void setDefaultMqSource(String defaultMqSource) {
		this.defaultMqSource = defaultMqSource;
	}

	public List<ListenerVo> getListenerVoList() {
		return listenerVoList;
	}

	public List<ServiceVo> getServiceVoList() {
		return serviceVoList;
	}

	public Map<String, ChannelVo> getChannelVoMap() {
		return channelVoMap;
	}

	// private Map<String, ChannelVo> queueVoMap = new HashMap<String, ChannelVo>();
	// public Map<String, ChannelVo> getQueueVoMap() {
	// return queueVoMap;
	// }

}
