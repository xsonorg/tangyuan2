package org.xson.tangyuan.web.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.web.WebComponent;
import org.xson.tangyuan.web.xml.modeimpl.AutoMapping;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XMLConfigBuilder {

	private Log				log		= LogFactory.getLog(getClass());
	private XPathParser		parser	= null;
	private XmlNodeWrapper	root	= null;
	private BuilderContext	bc		= new BuilderContext();

	public XMLConfigBuilder(InputStream inputStream) {
		this.parser = new XPathParser(inputStream);
		this.root = this.parser.evalNode("/web-component");
	}

	public void parseNode() throws Throwable {
		buildConfigNodes(this.root.evalNodes("config-property"));
		// 去除demain元素
		// buildDomainNodes(this.root.evalNodes("domain"));
		if (WebComponent.getInstance().isMappingServiceName()) {
			new AutoMapping(this.bc).build();
		}
		buildPluginNodes(this.root.evalNodes("plugin"));
		WebComponent.getInstance().setControllerMap(this.bc.getControllerMap());
		this.bc.clear();
	}

	private void buildConfigNodes(List<XmlNodeWrapper> contexts) throws Exception {
		// <config-property name="A" value="B" />
		Map<String, String> configMap = new HashMap<String, String>();
		for (XmlNodeWrapper context : contexts) {
			String name = StringUtils.trim(context.getStringAttribute("name"));
			String value = StringUtils.trim(context.getStringAttribute("value"));
			if (null == name || null == value) {
				throw new RuntimeException("<config-property> missing names or value");
			}
			configMap.put(name.toUpperCase(), value);
		}
		if (configMap.size() > 0) {
			WebComponent.getInstance().config(configMap);
		}
	}

	// 忽略web中的domain
	// private void buildDomainNodes(List<XmlNodeWrapper> contexts) throws Exception {
	// // <domain id="xxx" base="http://www.baidu.com" />
	// for (XmlNodeWrapper context : contexts) {
	// String id = StringUtils.trim(context.getStringAttribute("id"));
	// String base = StringUtils.trim(context.getStringAttribute("base"));
	// if (null == id || null == base) {
	// throw new RuntimeException("<domain> missing id or base");
	// }
	// if (this.bc.getDomainMap().containsKey(id)) {
	// throw new RuntimeException("Duplicate domain: " + id);
	// }
	// this.bc.getDomainMap().put(id, base);
	// }
	// }

	private void buildPluginNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		// <plugin resource="xxx.xml" />
		if (0 == contexts.size()) {
			return;
		}

		List<String> resourceList = new ArrayList<String>();
		for (XmlNodeWrapper context : contexts) {
			String resource = StringUtils.trim(context.getStringAttribute("resource"));
			if (null == resource) {
				throw new XmlParseException("<plugin> missing resource");
			}
			resourceList.add(resource);
		}

		int i = 0;
		XMLPluginBuilder[] builders = new XMLPluginBuilder[resourceList.size()];
		for (String resource : resourceList) {
			log.info("Start parsing(bean|intercept): " + resource);
			InputStream inputStream = Resources.getResourceAsStream(resource);
			builders[i] = new XMLPluginBuilder(inputStream, this.bc);
			// builders[i].parseBeanNode();
			// builders[i].parseInterceptNode();
			builders[i].parseRef();
			i++;
		}
		i = 0;
		for (String resource : resourceList) {
			log.info("Start parsing(controller): " + resource);
			builders[i].parseControllerNode();
			i++;
		}
		// WebComponent.getInstance().setControllerMap(this.bc.getControllerMap());
	}

	// /** 解析插件, 自动映射模式下 */
	// private void buildPluginNodesWithAutoMappingMode(List<XmlNodeWrapper> contexts) throws Exception {
	// List<String> resourceList = new ArrayList<String>();
	// for (XmlNodeWrapper context : contexts) {
	// String resource = StringUtils.trim(context.getStringAttribute("resource"));
	// if (null != resource) {
	// resourceList.add(resource);
	// }
	// }
	//
	// XMLPluginBuilder builder = null;
	// for (String resource : resourceList) {
	// log.info("Start parsing(bean|intercept): " + resource);
	// InputStream inputStream = Resources.getResourceAsStream(resource);
	// builder = new XMLPluginBuilder(inputStream, this.bc);
	// builder.parseBeanNode();
	// builder.parseInterceptNode();
	// }
	//
	// if (null == builder) {
	// builder = new XMLPluginBuilder(this.bc);
	// }
	//
	// log.info("Start url auto mapping......");
	// builder.parseControllerNodeAutoMapping();
	//
	// WebComponent.getInstance().setControllerMap(this.bc.getControllerMap());
	// }
	//
	// /** 解析插件, 自动映射模式+混合模式 */
	// private void buildPluginNodesWithMixMode(List<XmlNodeWrapper> contexts) throws Exception {
	// List<String> resourceList = new ArrayList<String>();
	// for (XmlNodeWrapper context : contexts) {
	// String resource = StringUtils.trim(context.getStringAttribute("resource"));
	// if (null != resource) {
	// resourceList.add(resource);
	// }
	// }
	//
	// int i = -1;
	// XMLPluginBuilder[] builders = new XMLPluginBuilder[resourceList.size()];
	// for (String resource : resourceList) {
	// log.info("Start parsing(bean|intercept): " + resource);
	// i++;
	// InputStream inputStream = Resources.getResourceAsStream(resource);
	// builders[i] = new XMLPluginBuilder(inputStream, this.bc);
	// builders[i].parseBeanNode();
	// builders[i].parseInterceptNode();
	// }
	//
	// XMLPluginBuilder builder = null;
	// if (i > -1) {
	// builder = builders[i];
	// } else {
	// builder = new XMLPluginBuilder(this.bc);
	// }
	//
	// log.info("Start url auto mapping......");
	// builder.parseControllerNodeAutoMapping();
	//
	// WebComponent.getInstance().setControllerMap(this.bc.getControllerMap());
	// }

}
