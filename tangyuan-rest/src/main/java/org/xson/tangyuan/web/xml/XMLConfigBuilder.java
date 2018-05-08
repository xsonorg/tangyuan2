package org.xson.tangyuan.web.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.util.ResourceManager;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.validate.RuleDataConverter;
import org.xson.tangyuan.web.WebComponent;
import org.xson.tangyuan.web.convert.JSONDataConverter;
import org.xson.tangyuan.web.convert.NothingConverter;
import org.xson.tangyuan.web.convert.XCODataConverter;
import org.xson.tangyuan.web.convert.XCORESTURIBodyDataConverter;
import org.xson.tangyuan.web.convert.XCORESTURIDataConverter;
import org.xson.tangyuan.web.rest.RESTContainer;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XMLConfigBuilder {

	private Log				log		= LogFactory.getLog(getClass());
	private XPathParser		parser	= null;
	private XmlNodeWrapper	root	= null;
	private XMLWebContext	context	= new XMLWebContext();

	public XMLConfigBuilder(InputStream inputStream) {
		this.parser = new XPathParser(inputStream);
		this.root = this.parser.evalNode("/web-component");
	}

	public void parseNode() throws Throwable {
		buildConfigNodes(this.root.evalNodes("config-property"));
		// 初始化DataConverter
		initDataConverter();
		// 处理自动映射模式
		if (WebComponent.getInstance().isMappingServiceName()) {
			new AutoMappingBuilder(this.context).build();
		}

		buildPluginNodes(this.root.evalNodes("plugin"));

		if (WebComponent.getInstance().isRestMode()) {
			RESTContainer restContainer = new RESTContainer();
			restContainer.build(this.context);
			WebComponent.getInstance().setRestContainer(restContainer);
		}
		// else {
		// WebComponent.getInstance().setControllerMap(this.context.getControllerMap());
		// }
		WebComponent.getInstance().setControllerMap(this.context.getControllerMap());
		this.context.clear();
	}

	private void initDataConverter() throws Throwable {

		this.context.getConverterIdMap().put("@no", NothingConverter.instance);

		this.context.getConverterIdMap().put("@xco", XCODataConverter.instance);
		this.context.getConverterIdMap().put("@json", JSONDataConverter.instance);
		this.context.getConverterIdMap().put("@rule", RuleDataConverter.instance);

		this.context.getConverterIdMap().put("@xco_rest_uri", XCORESTURIDataConverter.instance);
		this.context.getConverterIdMap().put("@xco_rest_uri_body", XCORESTURIBodyDataConverter.instance);

		// this.context.getConverterIdMap().put("@json_rest_uri", JSONRESTURIDataConverter.instance);
		// this.context.getConverterIdMap().put("@json_rest_uri_body", JSONRESTURIBodyDataConverter.instance);
	}

	private void buildConfigNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		// <config-property name="A" value="B" />
		Map<String, String> configMap = new HashMap<String, String>();
		for (XmlNodeWrapper context : contexts) {
			String name = StringUtils.trim(context.getStringAttribute("name"));
			String value = StringUtils.trim(context.getStringAttribute("value"));
			if (null == name || null == value) {
				throw new XmlParseException("<config-property> missing names or value");
			}
			configMap.put(name.toUpperCase(), value);
		}
		if (configMap.size() > 0) {
			WebComponent.getInstance().config(configMap);
		}
	}

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
			//InputStream inputStream = Resources.getResourceAsStream(resource);
			InputStream inputStream = ResourceManager.getInputStream(resource, false);
			builders[i] = new XMLPluginBuilder(inputStream, this.context);
			builders[i].parseRef();
			i++;
		}

		i = 0;
		for (String resource : resourceList) {
			// log.info("Start parsing(controller): " + resource);
			resource.length();
			builders[i].parseRef2();
			i++;
		}

		i = 0;
		for (String resource : resourceList) {
			log.info("Start parsing(controller): " + resource);
			builders[i].parseControllerNode();
			i++;
		}
	}

}
