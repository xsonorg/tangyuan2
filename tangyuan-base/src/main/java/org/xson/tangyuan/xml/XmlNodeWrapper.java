package org.xson.tangyuan.xml;

import java.util.List;
import java.util.Properties;

import org.w3c.dom.CharacterData;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlNodeWrapper {

	private Node        node;
	private String      name;
	private String      body;
	private Properties  attributes;
	private XPathParser xmlPathParser;

	public XmlNodeWrapper(XPathParser xmlPathParser, Node node) {
		this.xmlPathParser = xmlPathParser;
		this.node = node;
		this.name = node.getNodeName();
		this.attributes = parseAttributes(node);
		this.body = parseBody(node);
	}

	private Properties parseAttributes(Node n) {
		Properties   attributes     = new Properties();
		NamedNodeMap attributeNodes = n.getAttributes();
		if (attributeNodes != null) {
			for (int i = 0; i < attributeNodes.getLength(); i++) {
				Node   attribute = attributeNodes.item(i);
				// TODO String value = PropertyParser.parse(attribute.getNodeValue(), null);
				String value     = attribute.getNodeValue();
				attributes.put(attribute.getNodeName(), value);
			}
		}
		return attributes;
	}

	private String parseBody(Node node) {
		String data = getBodyData(node);
		if (data == null) {
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				data = getBodyData(child);
				if (data != null)
					break;
			}
		}
		return data;
	}

	private String getBodyData(Node child) {
		if (child.getNodeType() == Node.CDATA_SECTION_NODE || child.getNodeType() == Node.TEXT_NODE) {
			String data = ((CharacterData) child).getData();
			// TODO data = PropertyParser.parse(data, null);
			return data;
		}
		return null;
	}

	public Node getNode() {
		return node;
	}

	public String getName() {
		return name;
	}

	public String getStringBody() {
		return getStringBody(null);
	}

	public String getStringBody(String def) {
		if (body == null) {
			return def;
		} else {
			return body;
		}
	}

	public String get2StringAttribute(String[] names) {
		String value = null;
		for (String name : names) {
			value = getStringAttribute(name, null);
			if (null != value) {
				return value;
			}
		}
		return value;
	}

	public String getStringAttribute(String name) {
		return getStringAttribute(name, null);
	}

	public String getStringAttribute(String name, String def) {
		String value = attributes.getProperty(name);
		if (value == null) {
			return def;
		} else {
			return value;
		}
	}

	public List<XmlNodeWrapper> evalNodes(String expression) {
		return this.xmlPathParser.evalNodes(node, expression);
	}

	public List<XmlNodeWrapper> evalNodes(String expr1, String expr2) {
		List<XmlNodeWrapper> nodeList1 = this.xmlPathParser.evalNodes(node, expr1);
		List<XmlNodeWrapper> nodeList2 = this.xmlPathParser.evalNodes(node, expr2);
		if (nodeList2.size() > 0) {
			nodeList1.addAll(nodeList2);
		}
		return nodeList1;
	}

	public XmlNodeWrapper newXMlNode(Node node) {
		return new XmlNodeWrapper(this.xmlPathParser, node);
	}

}
