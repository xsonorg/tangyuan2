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

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
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
		String tagName = "plugin";
		List<XmlValidatePluginBuilder> pluginBuilders = new ArrayList<XmlValidatePluginBuilder>();
		// 扫描所有的plugin
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
			XmlValidatePluginBuilder builder = new XmlValidatePluginBuilder();
			builder.setContext(resource, this.componentContext);
			// first
			builder.parseRef();
			pluginBuilders.add(builder);
		}
		// 注册所有的服务
		// for (int i = 0; i < size; i++) {
		// pluginBuilders.get(i).parseService();
		// pluginBuilders.get(i).clean();
		// }
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

}
