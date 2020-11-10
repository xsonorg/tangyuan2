package org.xson.tangyuan.es.xml;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.es.EsResultConverter;
import org.xson.tangyuan.xml.DefaultXmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;

public class XmlEsContext extends DefaultXmlContext {

	private XmlGlobalContext               xmlContext   = null;

	private Map<String, EsResultConverter> converterMap = new HashMap<String, EsResultConverter>();

	@Override
	public void clean() {
		this.xmlContext = null;
		// TODO clean
		this.converterMap = null;
	}

	public XmlGlobalContext getXmlContext() {
		return xmlContext;
	}

	public void setXmlContext(XmlGlobalContext xmlContext) {
		this.xmlContext = xmlContext;
	}

	public void addConverter(String key, EsResultConverter converter) {
		this.converterMap.put(key.toUpperCase(), converter);
	}

	public EsResultConverter getConverter(String key) {
		return this.converterMap.get(key.toUpperCase());
	}

}
