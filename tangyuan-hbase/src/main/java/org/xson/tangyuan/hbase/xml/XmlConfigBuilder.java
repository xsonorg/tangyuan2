package org.xson.tangyuan.hbase.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.hbase.HBaseComponent;
import org.xson.tangyuan.hbase.datasource.DataSourceVo;
import org.xson.tangyuan.hbase.datasource.HBaseDataSource;
import org.xson.tangyuan.hbase.datasource.HBaseDataSourceManager;
import org.xson.tangyuan.hbase.xml.node.XMLHBaseNodeBuilder;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlExtendBuilder;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlNodeBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlConfigBuilder implements XmlExtendBuilder {

	private Log				log			= LogFactory.getLog(getClass());
	private XPathParser		xPathParser	= null;
	private XmlHBaseContext	context		= new XmlHBaseContext();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = Resources.getResourceAsStream(resource);
		this.xPathParser = new XPathParser(inputStream);
		this.context.setXmlContext((XmlGlobalContext) xmlContext);
		configurationElement(xPathParser.evalNode("/hbase-component"));
		context.clean();
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildConfigNodes(context.evalNodes("config-property"));// 解析配置项目
		buildDataSourceNodes(context.evalNodes("dataSource"));
		buildPluginNodes(context.evalNodes("plugin"));
	}

	private void buildConfigNodes(List<XmlNodeWrapper> contexts) {
		Map<String, String> configMap = new HashMap<String, String>();
		for (XmlNodeWrapper context : contexts) {
			String name = StringUtils.trim(context.getStringAttribute("name"));
			String value = StringUtils.trim(context.getStringAttribute("value"));
			if (null == name || null == value) {
				throw new XmlParseException("<config-property> missing name or value");
			}
			configMap.put(name.toUpperCase(), value);
		}
		if (configMap.size() > 0) {
			HBaseComponent.getInstance().config(configMap);
		}
	}

	private void buildDataSourceNodes(List<XmlNodeWrapper> contexts) throws Throwable {

		int size = contexts.size();
		if (0 == size) {
			throw new XmlParseException("No hbase data source...");
		}

		Map<String, DataSourceVo> dataSourceVoMap = new HashMap<String, DataSourceVo>();
		String defaultDsKey = null;

		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));
			if (dataSourceVoMap.containsKey(id)) {
				throw new XmlParseException("Duplicate data source ID: " + id);
			}
			String sharedUse = StringUtils.trim(xNode.getStringAttribute("sharedUse"));

			Map<String, String> data = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				// data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
				// StringUtils.trim(propertyNode.getStringAttribute("value")));
				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")), StringUtils.trim(propertyNode.getStringAttribute("value")));
			}

			DataSourceVo dsVo = new DataSourceVo(id, data, sharedUse, TangYuanContainer.getInstance().getSystemName());
			dataSourceVoMap.put(id, dsVo);
			defaultDsKey = id;
		}

		if (1 == dataSourceVoMap.size()) {
			HBaseDataSource ds = dataSourceVoMap.get(defaultDsKey).start();
			HBaseDataSourceManager.setDataSource(ds, null);
			log.info("add hbase datasource: " + defaultDsKey);
		} else {
			Map<String, HBaseDataSource> dataSourceMap = new HashMap<String, HBaseDataSource>();
			for (Entry<String, DataSourceVo> item : dataSourceVoMap.entrySet()) {
				dataSourceMap.put(item.getKey(), item.getValue().start());
				log.info("add hbase datasource: " + item.getKey());
			}
			HBaseDataSourceManager.setDataSource(null, dataSourceMap);
		}
	}

	private void buildPluginNodes(List<XmlNodeWrapper> contexts) throws Exception {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		List<String> resourceList = new ArrayList<String>();
		List<XmlNodeBuilder> xmlNodeBuilderList = new ArrayList<XmlNodeBuilder>();

		// 扫描所有的<SQL>
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String resource = StringUtils.trim(xNode.getStringAttribute("resource"));

			if (null == resource || 0 == resource.length()) {
				throw new XmlParseException("<plugin> node resource property is empty.");
			}

			// log.info("*** Start parsing(ref): " + resource);
			InputStream inputStream = Resources.getResourceAsStream(resource);
			XPathParser parser = new XPathParser(inputStream);
			XmlNodeBuilder nodeBuilder = getXmlNodeBuilder(parser);
			nodeBuilder.parseRef();

			xmlNodeBuilderList.add(nodeBuilder);
			resourceList.add(resource);
		}

		// 注册所有的服务
		for (int i = 0; i < size; i++) {
			log.info("*** Start parsing(service): " + resourceList.get(i));
			xmlNodeBuilderList.get(i).parseService();
		}
	}

	private XmlNodeBuilder getXmlNodeBuilder(XPathParser parser) {
		XmlNodeWrapper _root = null;
		if (null != (_root = parser.evalNode("/hbaseServices"))) {
			XmlNodeBuilder nodeBuilder = new XMLHBaseNodeBuilder();
			nodeBuilder.setContext(_root, this.context);
			return nodeBuilder;
		} else {
			throw new XmlParseException("Unsupported root node in the service plug-in");
		}
	}

	// private void testStringEmpty(String str, String message) {
	// if (null == str || 0 == str.length()) {
	// throw new XmlParseException(message);
	// }
	// }
}
