package org.xson.tangyuan.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XPathParser {

	private Document	document;
	private boolean		validation;
	private XPath		xpath;

	public XPathParser(InputStream inputStream) {
		commonConstructor(false);
		this.document = createDocument(new InputSource(inputStream));
	}

	private void commonConstructor(boolean validation) {
		this.validation = validation;
		XPathFactory factory = XPathFactory.newInstance();
		this.xpath = factory.newXPath();
	}

	public XmlNodeWrapper evalNode(String expression) {
		return evalNode(document, expression);
	}

	public XmlNodeWrapper evalNode(Object root, String expression) {
		Node node = (Node) evaluate(expression, root, XPathConstants.NODE);
		if (node == null) {
			return null;
		}
		return new XmlNodeWrapper(this, node);
	}

	public List<XmlNodeWrapper> evalNodes(String expression) {
		return evalNodes(document, expression);
	}

	public List<XmlNodeWrapper> evalNodes(Object root, String expression) {
		List<XmlNodeWrapper> xmlNodes = new ArrayList<XmlNodeWrapper>();
		NodeList nodes = (NodeList) evaluate(expression, root, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			xmlNodes.add(new XmlNodeWrapper(this, nodes.item(i)));
		}
		return xmlNodes;
	}

	private Object evaluate(String expression, Object root, QName returnType) {
		try {
			return xpath.evaluate(expression, root, returnType);
		} catch (Exception e) {
			throw new XmlParseException("Error evaluating XPath.  Cause: " + e, e);
		}
	}

	private Document createDocument(InputSource inputSource) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(validation);

			factory.setNamespaceAware(false);					// 是否支持名称空间
			factory.setIgnoringComments(true);					// 是否忽略注释
			factory.setIgnoringElementContentWhitespace(false);	// 是否可忽略空格
			factory.setCoalescing(false);						// 是否将CDATA节点转换为Text节点
			factory.setExpandEntityReferences(true);			// ??,是否引出安全漏洞

			DocumentBuilder builder = factory.newDocumentBuilder();
			// builder.setEntityResolver(entityResolver);
			builder.setErrorHandler(new ErrorHandler() {
				public void error(SAXParseException exception) throws SAXException {
					throw exception;
				}

				public void fatalError(SAXParseException exception) throws SAXException {
					throw exception;
				}

				public void warning(SAXParseException exception) throws SAXException {
				}
			});
			return builder.parse(inputSource);
		} catch (Exception e) {
			throw new XmlParseException("Error creating document instance.", e);
		}
	}
}
