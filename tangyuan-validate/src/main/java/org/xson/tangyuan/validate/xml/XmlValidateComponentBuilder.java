package org.xson.tangyuan.validate.xml;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.validate.Checker;
import org.xson.tangyuan.validate.ValidateComponent;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlValidateComponentBuilder extends DefaultXmlComponentBuilder {

	protected XmlValidateContext componentContext = null;

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		this.componentContext = (XmlValidateContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "validate-component", true);
		this.configurationElement();
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();
		this.componentContext = null;
	}

	private void configurationElement() throws Throwable {
		buildConfigNode(this.root.evalNodes("config-property"), ValidateComponent.getInstance());
		buildCheckerNode(this.root.evalNodes("checker"));
		buildPluginNode(this.root.evalNodes("plugin"));
	}

	private void buildCheckerNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "checker";
		for (XmlNodeWrapper xNode : contexts) {

			String id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String className = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));

			if (this.componentContext.getCustomCheckerMap().containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			Checker checker = getInstanceForName(className, Checker.class, lang("xml.class.impl.interface", className, Checker.class.getName()));
			this.componentContext.getCustomCheckerMap().put(id, checker);

			log.info(lang("add.tag.class"), tagName, className);
		}
	}

	private void buildPluginNode(List<XmlNodeWrapper> contexts) throws Throwable {
		// <plugin resource="xxx.xml" />
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		String                         tagName        = "plugin";
		List<XmlValidatePluginBuilder> pluginBuilders = new ArrayList<XmlValidatePluginBuilder>();
		// 扫描所有的plugin
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper           xNode    = contexts.get(i);
			String                   resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
			XmlValidatePluginBuilder builder  = new XmlValidatePluginBuilder();
			builder.setContext(resource, this.componentContext);
			// first
			builder.parseRef();
			pluginBuilders.add(builder);
		}
		// 注册所有的服务
		//		for (int i = 0; i < size; i++) {
		//			pluginBuilders.get(i).parseService();
		//			pluginBuilders.get(i).clean();
		//		}
		for (int i = 0; i < size; i++) {
			pluginBuilders.get(i).parseService();
		}
		// 注册服务
		if (size > 0) {
			pluginBuilders.get(0).registerValidateAsService();
		}
		for (int i = 0; i < size; i++) {
			pluginBuilders.get(i).clean();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	@Override
	//	public void clean() {
	//		super.clean();
	//		this.xmlContext = null;
	//	}
	//	@Override
	//	public void parse(XmlContext xmlContext, String resource) throws Throwable {
	//		log.info("*** Start parsing: " + resource);
	//		this.xmlContext = (XmlValidateContext) xmlContext;
	//		InputStream inputStream = ResourceManager.getInputStream(resource, true);
	//		try {
	//			this.parser = new XPathParser(inputStream);
	//			this.root = this.parser.evalNode("/validate-component");
	//		} catch (Throwable e) {
	//			throw new XmlParseException("load resource error: " + resource, e);
	//		}
	//		parseNode();
	//		inputStream.close();
	//	}

	//	public void parseNode() throws Throwable {
	//		buildConfigNodes(this.root.evalNodes("config-property"));
	//		buildCheckerNodes(this.root.evalNodes("checker"));
	//		buildPluginNodes(this.root.evalNodes("plugin"));
	//	}

	//	private void buildConfigNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		// <config-property name="A" value="B" />
	//		Map<String, String> configMap = new HashMap<String, String>();
	//		for (XmlNodeWrapper context : contexts) {
	//			String name  = StringUtils.trim(context.getStringAttribute("name"));
	//			String value = StringUtils.trim(context.getStringAttribute("value"));
	//			if (null == name || null == value) {
	//				throw new XmlParseException("<config-property> missing name or value");
	//			}
	//			configMap.put(name.toUpperCase(), value);
	//		}
	//		if (configMap.size() > 0) {
	//			ValidateComponent.getInstance().config(configMap);
	//		}
	//	}

	//	private Log						log			= LogFactory.getLog(getClass());

	//	private void buildCheckerNode(List<XmlNodeWrapper> contexts) throws Throwable {
	//		String tagName = "checker";
	//		for (XmlNodeWrapper context : contexts) {
	//			String id        = StringUtils.trim(context.getStringAttribute("id"));
	//			String className = StringUtils.trim(context.getStringAttribute("class"));
	//			// if (customCheckerMap.containsKey(id)) {
	//			// throw new XmlParseException("Duplicate <checker> node");
	//			// }
	//			xvUtil.containsKey(this.xmlContext.customCheckerMap, id, "Duplicate <checker> node");
	//
	//			Class<?> clazz = Class.forName(className);
	//			if (!clazz.isAssignableFrom(Checker.class)) {
	//				throw new XmlParseException("Custom checker must implement the interface Checker: " + className);
	//			}
	//			// customCheckerMap.put(id, (Checker) clazz.newInstance());
	//			this.xmlContext.customCheckerMap.put(id, (Checker) TangYuanUtil.newInstance(clazz));
	//
	//			log.info("Add <checker> :" + className);
	//		}
	//	}

	//	List<String> resourceList = new ArrayList<String>();
	//	for (XmlNodeWrapper context : contexts) {
	//		String resource = StringUtils.trim(context.getStringAttribute("resource"));
	//		xvUtil.stringEmpty(resource, "<plugin> missing resource");
	//		resourceList.add(resource);
	//	}
	//	XMLRuleBuilder[] builders = new XMLRuleBuilder[resourceList.size()];
	//	for (int i = 0; i < builders.length; i++) {
	//		builders[i] = new XMLRuleBuilder();
	//		builders[i].setContext(resourceList.get(i), xmlContext);
	//		builders[i].parseRef();
	//	}
	//	for (int i = 0; i < builders.length; i++) {
	//		builders[i].parseService();
	//		builders[i].clean();
	//	}
	//	builders = null;

	//		List<String> resourceList = new ArrayList<String>();
	//		for (XmlNodeWrapper context : contexts) {
	//			String resource = StringUtils.trim(context.getStringAttribute("resource"));
	//			xvUtil.stringEmpty(resource, "<plugin> missing resource");
	//			resourceList.add(resource);
	//		}
	//		XMLRuleBuilder[] builders = new XMLRuleBuilder[resourceList.size()];
	//		for (int i = 0; i < builders.length; i++) {
	//			builders[i] = new XMLRuleBuilder();
	//			builders[i].setContext(resourceList.get(i), xmlContext);
	//			builders[i].parseRef();
	//		}
	//		for (int i = 0; i < builders.length; i++) {
	//			builders[i].parseService();
	//			builders[i].clean();
	//		}
	//		builders = null;
}
