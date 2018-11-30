package org.xson.tangyuan.tools.xml;

import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;

public class XmlToolsContext implements XmlContext {

	private XmlGlobalContext xmlContext = null;

	@Override
	public void clean() {
		xmlContext = null;
	}

	public XmlGlobalContext getXmlContext() {
		return xmlContext;
	}

	public void setXmlContext(XmlGlobalContext xmlContext) {
		this.xmlContext = xmlContext;
	}

}
