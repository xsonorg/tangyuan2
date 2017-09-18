package org.xson.tangyuan.mongo.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.mongo.MongoComponent;
import org.xson.tangyuan.mongo.datasource.AbstractMongoDataSource;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceGroupVo;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceManager;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceVo;
import org.xson.tangyuan.mongo.datasource.MongoSupport;
import org.xson.tangyuan.mongo.datasource.MuiltMongoDataSourceManager;
import org.xson.tangyuan.mongo.datasource.SimpleMongoDataSourceManager;
import org.xson.tangyuan.mongo.xml.node.XMLMongoNodeBuilder;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlExtendBuilder;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlNodeBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlMongoConfigBuilder implements XmlExtendBuilder {

	private Log								log					= LogFactory.getLog(getClass());
	private XPathParser						xPathParser			= null;

	private Map<String, MongoDataSourceVo>	dataSourceVoMap		= new HashMap<String, MongoDataSourceVo>();
	private String							defaultDataSource	= null;
	private XmlMongoContext					context				= new XmlMongoContext();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = Resources.getResourceAsStream(resource);
		this.xPathParser = new XPathParser(inputStream);
		context.setXmlContext((XmlGlobalContext) xmlContext);
		configurationElement(xPathParser.evalNode("/mongo-component"));
		context.clean();
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		// 解析配置项目
		buildConfigNodes(context.evalNodes("config-property"));
		// 解析dataSource
		List<MongoDataSourceVo> dsList = buildDataSourceNodes(context.evalNodes("dataSource"));
		// 解析dataSourceGroup
		List<MongoDataSourceVo> dsGroupList = buildDataSourceGroupNodes(context.evalNodes("dataSourceGroup"));
		addDataSource(dsList, dsGroupList);
		buildMapperNodes(context.evalNodes("mapper"));
		buildShardingNodes(context.evalNodes("sharding"));
		buildPluginNodes(context.evalNodes("plugin"));
	}

	private void buildConfigNodes(List<XmlNodeWrapper> contexts) {
		// <config-property name="A" value="B" />
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
			MongoComponent.getInstance().config(configMap);
		}
	}

	private List<MongoDataSourceVo> buildDataSourceNodes(List<XmlNodeWrapper> contexts) {
		int size = contexts.size();
		List<MongoDataSourceVo> dsList = new ArrayList<MongoDataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));
			if (dataSourceVoMap.containsKey(id)) {
				throw new XmlParseException("Duplicate mongo data source ID: " + id);
			}
			String tmp = null;

			boolean defaultDs = false;
			tmp = StringUtils.trim(xNode.getStringAttribute("isDefault"));
			if (null != tmp) {
				if ("true".equalsIgnoreCase(tmp)) {
					if (null != this.defaultDataSource) {
						throw new XmlParseException("The default mongo data source must be unique.");
					} else {
						this.defaultDataSource = id;
					}
					defaultDs = true;
				}
			}

			// String jndiName = StringUtils.trim(xNode.getStringAttribute("jndiName"));
			String sharedUse = StringUtils.trim(xNode.getStringAttribute("sharedUse"));

			Map<String, String> data = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
						StringUtils.trim(propertyNode.getStringAttribute("value")));
			}
			// MongoDataSourceVo dsVo = new MongoDataSourceVo(id, data, defaultDs);
			MongoDataSourceVo dsVo = new MongoDataSourceVo(id, data, defaultDs, sharedUse, TangYuanContainer.getInstance().getSystemName());
			dsList.add(dsVo);
			dataSourceVoMap.put(id, dsVo);
		}
		return dsList;
	}

	private List<MongoDataSourceVo> buildDataSourceGroupNodes(List<XmlNodeWrapper> contexts) {
		// log.info("解析数据源:" + contexts.size());
		int size = contexts.size();
		List<MongoDataSourceVo> dsList = new ArrayList<MongoDataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("groupId"));
			if (dataSourceVoMap.containsKey(id)) {
				throw new XmlParseException("Duplicate mongo data source: " + id);
			}
			String tmp = null;
			// String tmp = StringUtils.trim(xNode.getStringAttribute("type"));
			// ConnPoolType type = null;
			// ConnPoolType type = getConnPoolType(tmp);
			// if (null == type) {
			// throw new XmlParseException("无效的数据源类型");
			// }
			tmp = StringUtils.trim(xNode.getStringAttribute("start")); // xml v
			int start = 0;
			if (null != tmp) {
				start = Integer.parseInt(tmp);
			}
			tmp = StringUtils.trim(xNode.getStringAttribute("end")); // xml v
			int end = Integer.parseInt(tmp);
			boolean defaultDs = false;
			tmp = StringUtils.trim(xNode.getStringAttribute("isDefault"));
			if (null != tmp) {
				if ("true".equalsIgnoreCase(tmp)) {
					if (null != this.defaultDataSource) {
						throw new XmlParseException("The default mongo data source must be unique.");
					} else {
						this.defaultDataSource = id;
					}
					defaultDs = true;
				}
			}

			Map<String, String> data = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
						StringUtils.trim(propertyNode.getStringAttribute("value")));
			}
			// MongoDataSourceGroupVo dsGroupVo = new MongoDataSourceGroupVo(id, defaultDs, data, start, end);
			MongoDataSourceGroupVo dsGroupVo = new MongoDataSourceGroupVo(id, defaultDs, data, TangYuanContainer.getInstance().getSystemName(), start,
					end);

			dsList.add(dsGroupVo);

			dataSourceVoMap.put(id, dsGroupVo);
		}
		return dsList;
	}

	private void addDataSource(List<MongoDataSourceVo> dsList, List<MongoDataSourceVo> dsGroupList) throws Exception {
		List<MongoDataSourceVo> allList = new ArrayList<MongoDataSourceVo>();
		if (null != dsList && dsList.size() > 0) {
			allList.addAll(dsList);
		}
		if (null != dsGroupList && dsGroupList.size() > 0) {
			allList.addAll(dsGroupList);
		}
		if (0 == allList.size()) {
			throw new XmlParseException("mongo data source is missing.");
		}

		MongoDataSourceManager dataSourceManager = null;
		Map<String, MongoDataSourceVo> logicDataSourceMap = new HashMap<String, MongoDataSourceVo>();
		Map<String, AbstractMongoDataSource> realDataSourceMap = new HashMap<String, AbstractMongoDataSource>();

		for (MongoDataSourceVo dsVo : allList) {
			dsVo.start(logicDataSourceMap, realDataSourceMap);
		}

		if (logicDataSourceMap.size() == 1 && realDataSourceMap.size() == 1) {
			String dataSourceId = allList.get(0).getId();
			AbstractMongoDataSource ds = realDataSourceMap.get(dataSourceId);
			dataSourceManager = new SimpleMongoDataSourceManager(TangYuanContainer.getInstance().getSystemName(), ds, dataSourceId);
		} else {
			dataSourceManager = new MuiltMongoDataSourceManager(TangYuanContainer.getInstance().getSystemName(), logicDataSourceMap,
					realDataSourceMap);
		}

		MongoComponent.getInstance().setDataSourceManager(dataSourceManager);
		MongoSupport.setManager(dataSourceManager);

		this.context.setDataSourceVoMap(dataSourceVoMap);
		this.context.setDefaultDataSource(defaultDataSource);
		dataSourceVoMap = null;
	}

	/**
	 * 解析mapper
	 */
	private void buildMapperNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		if (size > 1) {
			throw new XmlParseException("<mapper> plugin can only have one.");
		}
		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml v
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = Resources.getResourceAsStream(resource);
		XmlMongoMapperBuilder xmlMapperBuilder = new XmlMongoMapperBuilder(inputStream);
		xmlMapperBuilder.parse(this.context);
	}

	/** 解析Sharding */
	private void buildShardingNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		if (size > 1) {
			throw new XmlParseException("<sharding> can only have one at most.");
		}
		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml
		// v
		log.info("Start parsing: " + resource);
		InputStream inputStream = Resources.getResourceAsStream(resource);
		XmlMongoShardingBuilder xmlShardingBuilder = new XmlMongoShardingBuilder(inputStream);
		xmlShardingBuilder.parse(this.context);
	}

	private void buildPluginNodes(List<XmlNodeWrapper> contexts) throws Exception {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		List<String> resourceList = new ArrayList<String>();
		List<XmlNodeBuilder> xmlsqlNodeBuilderList = new ArrayList<XmlNodeBuilder>();

		// 扫描所有的<SQL>
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String resource = StringUtils.trim(xNode.getStringAttribute("resource"));
			log.info("*** Start parsing(ref): " + resource);
			InputStream inputStream = Resources.getResourceAsStream(resource);
			XmlNodeBuilder xmlSqlNodeBuilder = getXmlNodeBuilder(new XPathParser(inputStream));

			xmlSqlNodeBuilder.parseRef();
			xmlsqlNodeBuilderList.add(xmlSqlNodeBuilder);
			resourceList.add(resource);
		}

		// 注册所有的服务
		for (int i = 0; i < size; i++) {
			log.info("*** Start parsing(service): " + resourceList.get(i));
			xmlsqlNodeBuilderList.get(i).parseService();
		}
	}

	private XmlNodeBuilder getXmlNodeBuilder(XPathParser parser) {
		XmlNodeWrapper _root = null;
		if (null != (_root = parser.evalNode("/mongoservices"))) {
			XmlNodeBuilder nodeBuilder = new XMLMongoNodeBuilder();
			nodeBuilder.setContext(_root, this.context);
			return nodeBuilder;
		} else {
			throw new XmlParseException("Unsupported root node in the service plug-in");
		}
	}

}
