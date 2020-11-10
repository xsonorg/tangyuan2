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
		String              tagName   = "config-property";
		Map<String, String> configMap = new HashMap<String, String>();
		for (XmlNodeWrapper xNode : contexts) {
			String name  = getStringFromAttr(xNode, "name", lang("xml.tag.attribute.empty", "name", tagName, this.resource));
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
		//		Map                 map = new HashMap();
		for (XmlNodeWrapper xNode : contexts) {
			String name  = getStringFromAttr(xNode, "name", lang("xml.tag.attribute.empty", "name", tagName, this.resource));
			String value = getStringFromAttr(xNode, "value", lang("xml.tag.attribute.empty", "value", tagName, this.resource));
			if (upperCase) {
				name = name.toUpperCase();
			}
			map.put(name, value);
		}
		//		if (placeholder) {
		//			PlaceholderResourceSupport.processMap(map);
		//		}
		return map;
	}

	//	protected ShardingMode getShardingMode(String type) {
	//		if (ShardingMode.RANGE.toString().equalsIgnoreCase(type)) {
	//			return ShardingMode.RANGE;
	//		} else if (ShardingMode.HASH.toString().equalsIgnoreCase(type)) {
	//			return ShardingMode.HASH;
	//		} else if (ShardingMode.MOD.toString().equalsIgnoreCase(type)) {
	//			return ShardingMode.MOD;
	//		} else if (ShardingMode.RANDOM.toString().equalsIgnoreCase(type)) {
	//			return ShardingMode.RANDOM;
	//		}
	//		return null;
	//	}

	//========================================================================

	//	protected void init(String resource, String rootName, boolean placeholder) throws Throwable {
	//		// info("*** Start parsing: {}", resource);
	//
	//		this.resource = resource;
	//		InputStream inputStream = ResourceManager.getInputStream(resource, placeholder);
	//		this.xPathParser = new XPathParser(inputStream);
	//		this.root = this.xPathParser.evalNode("/" + rootName);
	//
	//		inputStream.close();// 
	//	}

	// protected void clean() {
	// super.clean();
	// }

	//	protected void buildConfigNodes(List<XmlNodeWrapper> contexts, TangYuanComponent component) {
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
	//			component.config(configMap);
	//		}
	//	}
}
