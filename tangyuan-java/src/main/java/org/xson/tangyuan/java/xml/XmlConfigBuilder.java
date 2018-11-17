package org.xson.tangyuan.java.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.java.JavaComponent;
import org.xson.tangyuan.java.xml.node.XMLJavaNodeBuilder;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.util.ResourceManager;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlExtendBuilder;
import org.xson.tangyuan.xml.XmlNodeBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlConfigBuilder implements XmlExtendBuilder {

	private Log			log			= LogFactory.getLog(getClass());
	private XPathParser	xPathParser	= null;
	private XmlContext	context		= null;

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		this.context = xmlContext;
		//		InputStream inputStream = Resources.getResourceAsStream(resource);

		InputStream inputStream = ResourceManager.getInputStream(resource, true);

		this.xPathParser = new XPathParser(inputStream);
		configurationElement(xPathParser.evalNode("/java-component"));

		inputStream.close();
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildConfigNodes(context.evalNodes("config-property"));// 解析配置项目
		buildPluginNodes(context.evalNodes("plugin"));
	}

	private void buildConfigNodes(List<XmlNodeWrapper> contexts) {
		// <config-property name="A" value="B" />
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
			JavaComponent.getInstance().config(configMap);
		}
	}

	private void buildPluginNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		List<String> resourceList = new ArrayList<String>();
		List<XmlNodeBuilder> xmlNodeBuilderList = new ArrayList<XmlNodeBuilder>();

		// 扫描所有的<SQL>
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String resource = StringUtils.trim(xNode.getStringAttribute("resource"));

			if (null == resource || 0 == resource.length()) {
				throw new XmlParseException("<plugin> node resource property is empty.");
			}

			// log.info("*** Start parsing(ref): " + resource);
			//			InputStream inputStream = Resources.getResourceAsStream(resource);
			InputStream inputStream = ResourceManager.getInputStream(resource, false);
			XPathParser parser = new XPathParser(inputStream);
			XmlNodeBuilder nodeBuilder = getXmlNodeBuilder(parser);
			// nodeBuilder.parseRef();

			xmlNodeBuilderList.add(nodeBuilder);
			resourceList.add(resource);
		}

		// 注册所有的服务
		for (int i = 0; i < size; i++) {
			log.info("*** Start parsing(service): " + resourceList.get(i));
			xmlNodeBuilderList.get(i).parseService();
		}
	}

	private XmlNodeBuilder getXmlNodeBuilder(XPathParser parser) {
		XmlNodeWrapper _root = null;
		if (null != (_root = parser.evalNode("/javaservices"))) {
			XmlNodeBuilder nodeBuilder = new XMLJavaNodeBuilder();
			nodeBuilder.setContext(_root, this.context);
			return nodeBuilder;
		} else {
			throw new XmlParseException("Unsupported root node in the service plug-in");
		}
	}

}
