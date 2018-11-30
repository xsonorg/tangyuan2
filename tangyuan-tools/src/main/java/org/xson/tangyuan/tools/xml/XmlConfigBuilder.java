package org.xson.tangyuan.tools.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xson.tangyuan.httpclient.HttpClientManager;
import org.xson.tangyuan.httpclient.HttpClientVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.tools.ToolsComponent;
import org.xson.tangyuan.util.ResourceManager;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanAssert;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlExtendBuilder;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlConfigBuilder implements XmlExtendBuilder {

	private Log				log			= LogFactory.getLog(getClass());
	private XPathParser		xPathParser	= null;
	private XmlToolsContext	context		= new XmlToolsContext();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = ResourceManager.getInputStream(resource, true);
		this.xPathParser = new XPathParser(inputStream);
		this.context.setXmlContext((XmlGlobalContext) xmlContext);
		configurationElement(xPathParser.evalNode("/tools-component"));
		context.clean();
		inputStream.close();
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildConfigNodes(context.evalNodes("config-property"));// 解析配置项目
		buildHttpClientNodes(context.evalNodes("httpclient"));
		// buildPluginNodes(context.evalNodes("plugin"));
	}

	private void buildConfigNodes(List<XmlNodeWrapper> contexts) {
		Map<String, String> configMap = new HashMap<String, String>();
		for (XmlNodeWrapper context : contexts) {
			String name = StringUtils.trim(context.getStringAttribute("name"));
			String value = StringUtils.trim(context.getStringAttribute("value"));
			if (null == name || null == value) {
				throw new XmlParseException("<config-property> missing name or value");
			}
			configMap.put(name.toUpperCase(), value);
		}
		if (configMap.size() > 0) {
			ToolsComponent.getInstance().config(configMap);
		}
	}

	private void buildHttpClientNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		// <httpclient id="client1" resource="http.client.properties"/>
		Map<String, Integer> hcMap = new HashMap<String, Integer>();
		List<HttpClientVo> voList = new ArrayList<HttpClientVo>();
		for (XmlNodeWrapper context : contexts) {
			String id = StringUtils.trim(context.getStringAttribute("id"));
			String resource = StringUtils.trim(context.getStringAttribute("resource"));

			TangYuanAssert.stringEmpty(id, "the 'id' attribute in <httpclient> node is not empty.");
			TangYuanAssert.stringEmpty(resource, "the 'resource' attribute in <httpclient> node is not empty.");

			if (hcMap.containsKey(id)) {
				throw new XmlParseException("Duplicate <httpclient> node: " + id);
			}
			hcMap.put(id, 1);

			Properties p = ResourceManager.getProperties(resource, true);
			HttpClientVo hcVo = new HttpClientVo(id, p);
			voList.add(hcVo);

			log.info("add httpclient [{}]", id);
		}
		HttpClientManager.init(voList);
	}

}
