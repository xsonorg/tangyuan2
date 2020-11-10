package org.xson.tangyuan.cache.xml;

import org.xson.tangyuan.xml.DefaultXmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;

/**
 * SQL组件解析上下文
 */
public class XmlCacheContext extends DefaultXmlContext {

	private XmlGlobalContext xmlContext = null;

	@Override
	public void clean() {
		this.xmlContext = null;
	}

	public XmlGlobalContext getXmlContext() {
		return xmlContext;
	}

	public void setXmlContext(XmlGlobalContext xmlContext) {
		this.xmlContext = xmlContext;
	}

}
