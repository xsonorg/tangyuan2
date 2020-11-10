package org.xson.tangyuan.mongo.xml;

import org.xson.tangyuan.xml.DefaultMapperBuilder;
import org.xson.tangyuan.xml.XmlContext;

public class XmlMongoMapperBuilder extends DefaultMapperBuilder {

	private XmlMongoContext componentContext = null;

	@Override
	protected void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing.type", "mapper", resource));
		this.componentContext = (XmlMongoContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "mapper", true);
		this.configurationElement();
		this.componentContext.setMappingVoMap(mappingVoMap);
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();

		this.componentContext = null;
	}

	private void configurationElement() throws Throwable {
		buildMappingClassNode(this.root.evalNodes("mappingClass"));
		buildColumnHandlerClassNode(this.root.evalNodes("columnHandlerClass"));
		buildResultMapNode(this.root.evalNodes("resultMap"));
	}

}
