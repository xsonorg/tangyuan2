package org.xson.tangyuan.xml;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.xml.node.TangYuanNode;

public class XmlGlobalContext implements XmlContext {

	private Map<String, TangYuanNode>	integralRefMap			= new HashMap<String, TangYuanNode>();
	private Map<String, Integer>		integralServiceMap		= new HashMap<String, Integer>();
	private Map<String, Integer>		integralServiceNsMap	= new HashMap<String, Integer>();
	private Map<String, Integer>		integralServiceClassMap	= new HashMap<String, Integer>();

	public Map<String, TangYuanNode> getIntegralRefMap() {
		return integralRefMap;
	}

	public Map<String, Integer> getIntegralServiceMap() {
		return integralServiceMap;
	}

	public Map<String, Integer> getIntegralServiceNsMap() {
		return integralServiceNsMap;
	}

	public Map<String, Integer> getIntegralServiceClassMap() {
		return integralServiceClassMap;
	}

	public void checkNs(String ns) {
		if (integralServiceNsMap.containsKey(ns)) {
			throw new XmlParseException("Duplicate ns: " + ns);
		}
		integralServiceNsMap.put(ns, 1);
	}

	@Override
	public void clean() {
		integralServiceNsMap.clear();
		integralServiceNsMap = null;

		integralRefMap.clear();
		integralRefMap = null;

		integralServiceMap = null;
		integralServiceClassMap = null;
	}
}
