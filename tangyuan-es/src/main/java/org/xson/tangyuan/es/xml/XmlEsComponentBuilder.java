package org.xson.tangyuan.es.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.es.EsComponent;
import org.xson.tangyuan.es.EsResultConverter;
import org.xson.tangyuan.es.converters.HitsConverter;
import org.xson.tangyuan.es.converters.JSONStringConverter;
import org.xson.tangyuan.es.converters.XCOConverter;
import org.xson.tangyuan.es.datasource.EsSourceManager;
import org.xson.tangyuan.es.datasource.EsSourceVo;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlEsComponentBuilder extends DefaultXmlComponentBuilder {

	private XmlEsContext componentContext = null;

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		this.componentContext = (XmlEsContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "es-component", true);
		this.configurationElement();
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();
		this.componentContext = null;
	}

	private void configurationElement() throws Throwable {
		buildConfigNode(this.root.evalNodes("config-property"), EsComponent.getInstance());
		buildEsSourceNode(this.root.evalNodes("esSource"));
		buildConverterNode(this.root.evalNodes("converter"));
		buildPluginNode(this.root.evalNodes("plugin"));
	}

	private void buildEsSourceNode(List<XmlNodeWrapper> contexts) {
		// <esSource id="es" host="http://192.168.0.251:9200" />
		String tagName = "esSource";
		if (0 == contexts.size()) {
			throw new XmlParseException(lang("xml.tag.miss", tagName, this.resource));
		}
		EsSourceVo              defaultVo   = null;
		Map<String, EsSourceVo> esSourceMap = new HashMap<String, EsSourceVo>();
		for (XmlNodeWrapper xNode : contexts) {
			String id   = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String host = getStringFromAttr(xNode, "host");
			String usi  = getStringFromAttr(xNode, "usi", lang("xml.tag.attribute.empty", "usi", tagName, this.resource));
			if (esSourceMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			if (host.endsWith("/")) {
				host = host.substring(0, host.length() - 1);
			}
			EsSourceVo essVo = new EsSourceVo(id, host, usi);
			defaultVo = essVo;
			esSourceMap.put(id, essVo);

			log.info(lang("add.tag"), tagName, id);
		}

		EsSourceManager esm = null;
		if (esSourceMap.size() == 1) {
			esm = new EsSourceManager(defaultVo, null);
		} else {
			esm = new EsSourceManager(null, esSourceMap);
		}
		EsComponent.getInstance().setEsSourceManager(esm);
	}

	private void buildConverterNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "converter";

		// 添加系统的
		//		this.componentContext.addConverter("@json", new JSONConverter());
		this.componentContext.addConverter(JSONStringConverter.key, new JSONStringConverter());
		this.componentContext.addConverter(XCOConverter.key, new XCOConverter());
		this.componentContext.addConverter(HitsConverter.key, new HitsConverter());

		// 添加用户的
		for (XmlNodeWrapper xNode : contexts) {
			String id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String className = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));

			if (null != this.componentContext.getConverter(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			EsResultConverter instance = getInstanceForName(className, EsResultConverter.class, lang("xml.class.impl.interface", className, EsResultConverter.class.getName()));
			this.componentContext.addConverter(id, instance);
			log.info(lang("add.tag.class"), tagName, className);
		}
	}

	private void buildPluginNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		String                   tagName        = "plugin";
		List<XmlEsPluginBuilder> pluginBuilders = new ArrayList<XmlEsPluginBuilder>();
		// 扫描所有的plugin
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper     xNode    = contexts.get(i);
			String             resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
			XmlEsPluginBuilder builder  = new XmlEsPluginBuilder();
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

	/////////////////////////////////////////////////////////////////////////////////

	//		if (esSourceMap.size() == 1) {
	//			EsSourceManager.setEsSourceVoMap(defaultVo, null);
	//		} else {
	//			EsSourceManager.setEsSourceVoMap(null, esSourceMap);
	//		}

	//	private XmlNodeBuilder getXmlNodeBuilder(XPathParser parser) {
	//		XmlNodeWrapper _root = null;
	//		if (null != (_root = parser.evalNode("/esservices"))) {
	//			XmlNodeBuilder nodeBuilder = new XMLEsNodeBuilder();
	//			nodeBuilder.setContext(_root, this.context);
	//			return nodeBuilder;
	//		} else {
	//			throw new XmlParseException("Unsupported root node in the service plug-in");
	//		}
	//	}

	//	private void testStringEmpty(String str, String message) {
	//		if (null == str || 0 == str.length()) {
	//			throw new XmlParseException(message);
	//		}
	//	}

	//	private void configurationElement(XmlNodeWrapper context) throws Throwable {
	//		buildConfigNodes(context.evalNodes("config-property"));// 解析配置项目
	//		buildEsSourceNodes(context.evalNodes("esSource"));
	//		buildConverterNodes(context.evalNodes("converter"));
	//		buildPluginNodes(context.evalNodes("plugin"));
	//	}

	//	private Log          log         = LogFactory.getLog(getClass());
	//	private XPathParser  xPathParser = null;
	//	private XmlEsContext context     = new XmlEsContext();
	//	@Override
	//	public void parse(XmlContext xmlContext, String resource) throws Throwable {
	//		log.info("*** Start parsing: " + resource);
	//
	//		InputStream inputStream = ResourceManager.getInputStream(resource, true);
	//
	//		this.xPathParser = new XPathParser(inputStream);
	//		this.context.setXmlContext((XmlGlobalContext) xmlContext);
	//		configurationElement(xPathParser.evalNode("/es-component"));
	//		context.clean();
	//
	//		inputStream.close();
	//	}

	//	private void buildPluginNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//	int size = contexts.size();
	//	if (size == 0) {
	//		return;
	//	}
	//	List<String>         resourceList       = new ArrayList<String>();
	//	List<XmlNodeBuilder> xmlNodeBuilderList = new ArrayList<XmlNodeBuilder>();
	//
	//	// 扫描所有的<SQL>
	//	for (int i = 0; i < size; i++) {
	//		XmlNodeWrapper xNode    = contexts.get(i);
	//		String         resource = StringUtils.trim(xNode.getStringAttribute("resource"));
	//
	//		if (null == resource || 0 == resource.length()) {
	//			throw new XmlParseException("<plugin> node resource property is empty.");
	//		}
	//
	//		// log.info("*** Start parsing(ref): " + resource);
	//		// InputStream inputStream = Resources.getResourceAsStream(resource);
	//		InputStream    inputStream = ResourceManager.getInputStream(resource, false);
	//		XPathParser    parser      = new XPathParser(inputStream);
	//		XmlNodeBuilder nodeBuilder = getXmlNodeBuilder(parser);
	//		// nodeBuilder.parseRef();
	//
	//		xmlNodeBuilderList.add(nodeBuilder);
	//		resourceList.add(resource);
	//	}
	//
	//	// 注册所有的服务
	//	for (int i = 0; i < size; i++) {
	//		log.info("*** Start parsing(service): " + resourceList.get(i));
	//		xmlNodeBuilderList.get(i).parseService();
	//	}
	//}
	//	private void buildConfigNodes(List<XmlNodeWrapper> contexts) {
	//	// <config-property name="A" value="B" />
	//	Map<String, String> configMap = new HashMap<String, String>();
	//	for (XmlNodeWrapper context : contexts) {
	//		String name  = StringUtils.trim(context.getStringAttribute("name"));
	//		String value = StringUtils.trim(context.getStringAttribute("value"));
	//		if (null == name || null == value) {
	//			throw new XmlParseException("<config-property> missing name or value");
	//		}
	//		configMap.put(name.toUpperCase(), value);
	//	}
	//	if (configMap.size() > 0) {
	//		EsComponent.getInstance().config(configMap);
	//	}
	//}

	//	private void buildEsSourceNode(List<XmlNodeWrapper> contexts) {
	//		// <esSource id="es" host="http://192.168.0.251:9200" />
	//		String                  tagName     = "esSource";
	//		EsSourceVo              defaultVo   = null;
	//		Map<String, EsSourceVo> esSourceMap = new HashMap<String, EsSourceVo>();
	//		for (XmlNodeWrapper context : contexts) {
	//			String id   = StringUtils.trim(context.getStringAttribute("id"));
	//			String host = StringUtils.trim(context.getStringAttribute("host"));
	//			String usi  = StringUtils.trim(context.getStringAttribute("usi"));
	//			testStringEmpty(id, "the id attribute in <esSource> node is not empty.");
	//			testStringEmpty(host, "the host attribute in <resSource> node is not empty.");
	//
	//			TangYuanAssert.stringEmpty(usi, "the 'usi' attribute cannot be empty.");
	//
	//			if (host.endsWith("/")) {
	//				host = host.substring(0, host.length() - 1);
	//			}
	//
	//			EsSourceVo essVo = new EsSourceVo(id, host, usi);
	//			defaultVo = essVo;
	//			esSourceMap.put(id, essVo);
	//		}
	//		if (null == defaultVo) {
	//			throw new XmlParseException("Missing <esSource> node.");
	//		}
	//		if (esSourceMap.size() == 1) {
	//			EsSourceManager.setEsSourceVoMap(defaultVo, null);
	//		} else {
	//			EsSourceManager.setEsSourceVoMap(null, esSourceMap);
	//		}
	//	}

	//	private void buildConverterNodes(List<XmlNodeWrapper> contexts) throws Exception {
	//		// 添加系统的
	//		this.componentContext.addConverter("@json", new JSONConverter());
	//		this.componentContext.addConverter("@xco", new XCOConverter());
	//		this.componentContext.addConverter("@hits", new HitsConverter());
	//
	//		for (XmlNodeWrapper context : contexts) {
	//			String   id             = StringUtils.trim(context.getStringAttribute("id"));
	//			String   clazz          = StringUtils.trim(context.getStringAttribute("class"));
	//			Class<?> converterClass = ClassUtils.forName(clazz);
	//			if (!ResultConverter.class.isAssignableFrom(converterClass)) {
	//				throw new XmlParseException("Converter class not implement the ResultConverter interface: " + clazz);
	//			}
	//			Object instance = TangYuanUtil.newInstance(converterClass);
	//			this.componentContext.addConverter(id, (ResultConverter) instance);
	//		}
	//	}
}
