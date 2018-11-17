package org.xson.tangyuan.es.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.es.EsComponent;
import org.xson.tangyuan.es.ResultConverter;
import org.xson.tangyuan.es.converters.HitsConverter;
import org.xson.tangyuan.es.converters.JSONConverter;
import org.xson.tangyuan.es.converters.XCOConverter;
import org.xson.tangyuan.es.datasource.EsSourceManager;
import org.xson.tangyuan.es.datasource.EsSourceVo;
import org.xson.tangyuan.es.xml.node.XMLEsNodeBuilder;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.ResourceManager;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlExtendBuilder;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlNodeBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlConfigBuilder implements XmlExtendBuilder {

	private Log				log			= LogFactory.getLog(getClass());
	private XPathParser		xPathParser	= null;
	private XmlEsContext	context		= new XmlEsContext();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		//		InputStream inputStream = Resources.getResourceAsStream(resource);
		//		inputStream = PlaceholderResourceSupport.processInputStream(inputStream,
		//				TangYuanContainer.getInstance().getXmlGlobalContext().getPlaceholderMap());

		InputStream inputStream = ResourceManager.getInputStream(resource, true);

		this.xPathParser = new XPathParser(inputStream);
		this.context.setXmlContext((XmlGlobalContext) xmlContext);
		configurationElement(xPathParser.evalNode("/es-component"));
		context.clean();

		inputStream.close();
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildConfigNodes(context.evalNodes("config-property"));// 解析配置项目
		buildEsSourceNodes(context.evalNodes("esSource"));
		buildConverterNodes(context.evalNodes("converter"));
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
			EsComponent.getInstance().config(configMap);
		}
	}

	private void buildEsSourceNodes(List<XmlNodeWrapper> contexts) {
		// <esSource id="es" host="http://192.168.0.251:9200" />
		EsSourceVo defaultVo = null;
		Map<String, EsSourceVo> esSourceMap = new HashMap<String, EsSourceVo>();
		for (XmlNodeWrapper context : contexts) {
			String id = StringUtils.trim(context.getStringAttribute("id"));
			String host = StringUtils.trim(context.getStringAttribute("host"));
			testStringEmpty(id, "the id attribute in <esSource> node is not empty.");
			testStringEmpty(host, "the host attribute in <resSource> node is not empty.");

			if (host.endsWith("/")) {
				host = host.substring(0, host.length() - 1);
			}

			EsSourceVo essVo = new EsSourceVo(id, host);
			defaultVo = essVo;
			esSourceMap.put(id, essVo);
		}
		if (null == defaultVo) {
			throw new XmlParseException("Missing <esSource> node.");
		}
		if (esSourceMap.size() == 1) {
			EsSourceManager.setEsSourceVoMap(defaultVo, null);
		} else {
			EsSourceManager.setEsSourceVoMap(null, esSourceMap);
		}
	}

	private void buildConverterNodes(List<XmlNodeWrapper> contexts) throws Exception {
		// 添加系统的
		this.context.addConverter("@json", new JSONConverter());
		this.context.addConverter("@xco", new XCOConverter());
		this.context.addConverter("@hits", new HitsConverter());

		for (XmlNodeWrapper context : contexts) {
			String id = StringUtils.trim(context.getStringAttribute("id"));
			String clazz = StringUtils.trim(context.getStringAttribute("class"));
			Class<?> converterClass = ClassUtils.forName(clazz);
			if (!ResultConverter.class.isAssignableFrom(converterClass)) {
				throw new XmlParseException("Converter class not implement the ResultConverter interface: " + clazz);
			}
			Object instance = TangYuanUtil.newInstance(converterClass);
			this.context.addConverter(id, (ResultConverter) instance);
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
			//InputStream inputStream = Resources.getResourceAsStream(resource);
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
		if (null != (_root = parser.evalNode("/esservices"))) {
			XmlNodeBuilder nodeBuilder = new XMLEsNodeBuilder();
			nodeBuilder.setContext(_root, this.context);
			return nodeBuilder;
		} else {
			throw new XmlParseException("Unsupported root node in the service plug-in");
		}
	}

	private void testStringEmpty(String str, String message) {
		if (null == str || 0 == str.length()) {
			throw new XmlParseException(message);
		}
	}
}
