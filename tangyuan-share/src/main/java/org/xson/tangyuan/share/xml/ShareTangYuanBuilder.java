package org.xson.tangyuan.share.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.share.util.StringUtils;

public class ShareTangYuanBuilder {

	private Log			log			= LogFactory.getLog(getClass());
	private XPathParser	xPathParser	= null;
	private String		basePath	= null;

	public void parse(String basePath, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = new FileInputStream(new File(basePath, resource));
		this.xPathParser = new XPathParser(inputStream);
		this.basePath = basePath;
		inputStream.close();
		configurationElement(xPathParser.evalNode("/tangyuan-share"));
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildJdbcNodes(context.evalNodes("plugin-jdbc"));
		buildCacheNodes(context.evalNodes("plugin-cache"));
		buildMongoNodes(context.evalNodes("plugin-mongo"));
		buildMqNodes(context.evalNodes("plugin-mq"));
	}

	private void buildJdbcNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		if (size > 1) {
			throw new XmlParseException("<plugin-jdbc> plugin can only have one.");
		}
		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml v
		new ShareJdbcBuilder().parse(this.basePath, resource);
	}

	private void buildCacheNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		if (size > 1) {
			throw new XmlParseException("<plugin-cache> plugin can only have one.");
		}
		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml v
		new ShareCacheBuilder().parse(this.basePath, resource);
	}

	private void buildMongoNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		if (size > 1) {
			throw new XmlParseException("<plugin-mongo> plugin can only have one.");
		}
		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml v
		new ShareMongoBuilder().parse(this.basePath, resource);
	}

	private void buildMqNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		if (size > 1) {
			throw new XmlParseException("<plugin-mq> plugin can only have one.");
		}
		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml v
		new ShareMqBuilder().parse(this.basePath, resource);
	}

}
