package org.xson.tangyuan.java.xml;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.java.JavaComponent;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;

public class XmlJavaComponentBuilder extends DefaultXmlComponentBuilder {

	private XmlJavaContext componentContext = null;

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		this.componentContext = (XmlJavaContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "java-component", true);
		this.configurationElement();
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();
		this.componentContext = null;
	}

	private void configurationElement() throws Throwable {
		// 解析配置项目
		buildConfigNode(this.root.evalNodes("config-property"), JavaComponent.getInstance());
		// 解析JAVA服务插件
		buildPluginNode(this.root.evalNodes("plugin"));
	}

	private void buildPluginNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		String tagName = "plugin";
		List<XmlJavaPluginBuilder> pluginBuilders = new ArrayList<XmlJavaPluginBuilder>();
		// 扫描所有的plugin
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
			XmlJavaPluginBuilder builder = new XmlJavaPluginBuilder();
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

}
