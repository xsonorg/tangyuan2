package org.xson.tangyuan.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.util.PlaceholderResourceSupport;

public class DefaultXmlComponentBuilder extends DefaultXmlBuilder {

	protected void parse(XmlContext xmlContext, String resource) throws Throwable {
		throw new TangYuanException(TangYuanLang.get("method.need.override"));
	}

	protected void buildConfigNode(List<XmlNodeWrapper> contexts, TangYuanComponent component) throws Throwable {
		// <config-property name="A" value="B" />
		String tagName = "config-property";
		Map<String, String> configMap = new HashMap<String, String>();
		for (XmlNodeWrapper xNode : contexts) {
			String name = getStringFromAttr(xNode, "name", lang("xml.tag.attribute.empty", "name", tagName, this.resource));
			String value = getStringFromAttr(xNode, "value", lang("xml.tag.attribute.empty", "value", tagName, this.resource));
			configMap.put(name.toUpperCase(), value);
		}
		if (configMap.size() > 0) {
			PlaceholderResourceSupport.processMap(configMap);
			component.config(configMap);
		}
	}

	protected Map<String, Object> buildPropertyMap(List<XmlNodeWrapper> contexts, String tagName, boolean upperCase) throws Throwable {
		Map<String, Object> map = new HashMap<String, Object>();
		// Map map = new HashMap();
		for (XmlNodeWrapper xNode : contexts) {
			String name = getStringFromAttr(xNode, "name", lang("xml.tag.attribute.empty", "name", tagName, this.resource));
			String value = getStringFromAttr(xNode, "value", lang("xml.tag.attribute.empty", "value", tagName, this.resource));
			if (upperCase) {
				name = name.toUpperCase();
			}
			map.put(name, value);
		}
		return map;
	}

}
