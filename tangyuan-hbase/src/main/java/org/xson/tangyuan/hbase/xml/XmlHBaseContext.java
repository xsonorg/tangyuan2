package org.xson.tangyuan.hbase.xml;

import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;

public class XmlHBaseContext implements XmlContext {

	private XmlGlobalContext xmlContext = null;

	@Override
	public void clean() {
		xmlContext = null;
		// converterMap = null;
	}

	public XmlGlobalContext getXmlContext() {
		return xmlContext;
	}

	public void setXmlContext(XmlGlobalContext xmlContext) {
		this.xmlContext = xmlContext;
	}

}
