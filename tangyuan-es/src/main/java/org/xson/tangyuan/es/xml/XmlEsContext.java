package org.xson.tangyuan.es.xml;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.es.ResultConverter;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;

public class XmlEsContext implements XmlContext {

	private XmlGlobalContext				xmlContext		= null;

	private Map<String, ResultConverter>	converterMap	= new HashMap<String, ResultConverter>();

	@Override
	public void clean() {
		xmlContext = null;
		converterMap = null;
	}

	public XmlGlobalContext getXmlContext() {
		return xmlContext;
	}

	public void setXmlContext(XmlGlobalContext xmlContext) {
		this.xmlContext = xmlContext;
	}

	public void addConverter(String key, ResultConverter converter) {
		converterMap.put(key.toUpperCase(), converter);
	}

	public ResultConverter getConverter(String key) {
		return this.converterMap.get(key.toUpperCase());
	}

}
