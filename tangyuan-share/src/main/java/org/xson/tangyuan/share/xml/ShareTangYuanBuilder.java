package org.xson.tangyuan.share.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.share.ShareComponent;
import org.xson.tangyuan.share.util.ResourceManager;
import org.xson.tangyuan.share.util.StringUtils;
import org.xson.tangyuan.share.util.TangYuanAssert;

public class ShareTangYuanBuilder {

	private Log			log			= LogFactory.getLog(getClass());
	private XPathParser	xPathParser	= null;
	private String		basePath	= null;

	public void parse(String basePath, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		this.basePath = basePath;

		InputStream inputStream = new FileInputStream(new File(basePath, resource));
		this.xPathParser = new XPathParser(inputStream);
		inputStream.close();

		configurationElement(xPathParser.evalNode("/share-component"));
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildPlaceholderNodes(context.evalNodes("placeholder"));// 解析占位项
		buildJdbcNodes(context.evalNodes("plugin-jdbc"));
		buildCacheNodes(context.evalNodes("plugin-cache"));
		buildMongoNodes(context.evalNodes("plugin-mongo"));
		buildMqNodes(context.evalNodes("plugin-mq"));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void buildPlaceholderNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size > 1) {
			throw new XmlParseException("The <placeholder> node can have at most one.");
		}
		if (size == 0) {
			return;
		}

		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource"));
		TangYuanAssert.stringEmpty(resource, "in the <placeholder> tag, the 'resource' property can not be empty.");

		//		Properties props = null;
		//		if (resource.toLowerCase().startsWith("http://") || resource.toLowerCase().startsWith("https://")) {
		//			// TODO 这个要考虑加密
		//			props = Resources.getUrlAsProperties(resource);
		//		} else {
		//			props = Resources.getResourceAsProperties(resource);
		//		}

		Properties props = ResourceManager.getProperties(this.basePath, resource);

		ShareComponent.getInstance().setPlaceholderMap((Map) props);
		log.info("add placeholder properties: " + resource);
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
