package org.xson.tangyuan.mongo.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xson.tangyuan.mongo.MongoComponent;
import org.xson.tangyuan.mongo.datasource.AbstractMongoDataSource;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceGroupVo;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceManager;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceVo;
import org.xson.tangyuan.mongo.datasource.MongoSupport;
import org.xson.tangyuan.mongo.datasource.MuiltMongoDataSourceManager;
import org.xson.tangyuan.mongo.datasource.SimpleMongoDataSourceManager;
import org.xson.tangyuan.util.MixedResourceManager;
import org.xson.tangyuan.util.PropertyUtils;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlMongoComponentBuilder extends DefaultXmlComponentBuilder {

	private XmlMongoContext                componentContext       = null;
	private Map<String, MongoDataSourceVo> dataSourceVoMap        = new HashMap<String, MongoDataSourceVo>();
	private String                         defaultDataSource      = null;
	private int                            defaultDataSourceCount = 0;

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		this.componentContext = (XmlMongoContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "mongo-component", true);
		this.configurationElement();
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();
		this.componentContext = null;

		this.dataSourceVoMap = null;
		this.defaultDataSource = null;
	}

	private void configurationElement() throws Throwable {
		// 解析配置项目
		buildConfigNode(this.root.evalNodes("config-property"), MongoComponent.getInstance());
		// 解析dataSource
		List<MongoDataSourceVo> dsList      = buildDataSourceNode(this.root.evalNodes("dataSource"));
		// 解析dataSourceGroup
		List<MongoDataSourceVo> dsGroupList = buildDataSourceGroupNode(this.root.evalNodes("dataSourceGroup"));
		addDataSource(dsList, dsGroupList);
		// 解析mapper
		buildMapperNode(getMostOneNode(this.root, "mapper", lang("xml.tag.mostone", "mapper")));
		// 解析sharding
		buildShardingNode(getMostOneNode(this.root, "sharding", lang("xml.tag.mostone", "sharding")));
		// 解析plugin
		buildPluginNode(this.root.evalNodes("plugin"));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<MongoDataSourceVo> buildDataSourceNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int                     size    = contexts.size();
		String                  tagName = "dataSource";
		List<MongoDataSourceVo> dsList  = new ArrayList<MongoDataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode     = contexts.get(i);

			String         id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			boolean        defaultDs = getBoolFromAttr(xNode, "isDefault", false);
			String         resource  = getStringFromAttr(xNode, "resource");
			//			String         sharedUse = getStringFromAttr(xNode, "sharedUse");

			if (dataSourceVoMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			if (defaultDs) {
				defaultDataSourceCount++;
				this.defaultDataSource = id;
			}

			Map<String, String> data = null;
			if (null != resource) {
				Properties p = MixedResourceManager.getProperties(resource, true, true);
				// data = PropertyUtils.keyToUpperCase((Map) p);
				data = (Map) p;
			} else {
				data = new HashMap<String, String>();
				List<XmlNodeWrapper> properties = xNode.evalNodes("property");
				for (XmlNodeWrapper propertyNode : properties) {
					String name  = getStringFromAttr(propertyNode, "name", lang("xml.tag.attribute.empty", "name", tagName + ".property", this.resource));
					String value = getStringFromAttr(propertyNode, "value", lang("xml.tag.attribute.empty", "value", tagName + ".property", this.resource));
					data.put(name, value);
				}
			}

			// 此处不需要，之前已经替换 PlaceholderResourceSupport.processMap(data);
			data = PropertyUtils.keyToUpperCase(data);// key转大写

			MongoDataSourceVo dsVo = new MongoDataSourceVo(id, data, defaultDs, null, null, resource);
			dsList.add(dsVo);
			dataSourceVoMap.put(id, dsVo);
		}
		return dsList;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<MongoDataSourceVo> buildDataSourceGroupNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String                  tagName = "dataSourceGroup";
		int                     size    = contexts.size();
		List<MongoDataSourceVo> dsList  = new ArrayList<MongoDataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode     = contexts.get(i);

			String         id        = getStringFromAttr(xNode, "groupId", lang("xml.tag.attribute.empty", "groupId", tagName, this.resource));
			//			String         _type     = getStringFromAttr(xNode, "type", lang("xml.tag.attribute.empty", "type", tagName, this.resource));
			boolean        defaultDs = getBoolFromAttr(xNode, "isDefault", false);
			String         resource  = getStringFromAttr(xNode, "resource");
			int            start     = getIntFromAttr(xNode, "start", 0);
			int            end       = getIntFromAttr(xNode, "end", lang("xml.tag.attribute.empty", "end", tagName, this.resource));

			if (dataSourceVoMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			if (defaultDs) {
				defaultDataSourceCount++;
				this.defaultDataSource = id;
			}

			Map<String, String> data = null;
			if (null != resource) {
				Properties p = MixedResourceManager.getProperties(resource, true, true);
				//				data = PropertyUtils.keyToUpperCase((Map) p);
				data = (Map) p;
			} else {
				data = new HashMap<String, String>();
				List<XmlNodeWrapper> properties = xNode.evalNodes("property");
				for (XmlNodeWrapper propertyNode : properties) {
					String name  = getStringFromAttr(propertyNode, "name", lang("xml.tag.attribute.empty", "name", tagName + ".property", this.resource));
					String value = getStringFromAttr(propertyNode, "value", lang("xml.tag.attribute.empty", "value", tagName + ".property", this.resource));
					data.put(name, value);
				}
			}

			// 此处不需要，之前已经替换 PlaceholderResourceSupport.processMap(data);
			data = PropertyUtils.keyToUpperCase(data);// key转大写

			MongoDataSourceGroupVo dsGroupVo = new MongoDataSourceGroupVo(id, defaultDs, data, null, start, end, resource);
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
			throw new XmlParseException(lang("xml.tag.miss", "dataSource|dataSourceGroup", this.resource));
		}
		if (defaultDataSourceCount > 1) {
			throw new XmlParseException(lang("xml.tag.mostone.default", "dataSource|dataSourceGroup"));
		}

		MongoDataSourceManager               dataSourceManager  = null;
		Map<String, MongoDataSourceVo>       logicDataSourceMap = new HashMap<String, MongoDataSourceVo>();
		Map<String, AbstractMongoDataSource> realDataSourceMap  = new HashMap<String, AbstractMongoDataSource>();

		for (MongoDataSourceVo dsVo : allList) {
			dsVo.start(logicDataSourceMap, realDataSourceMap);
		}

		if (logicDataSourceMap.size() == 1 && realDataSourceMap.size() == 1) {
			String                  dataSourceId = allList.get(0).getId();
			AbstractMongoDataSource ds           = realDataSourceMap.get(dataSourceId);
			dataSourceManager = new SimpleMongoDataSourceManager(null, ds, dataSourceId);
		} else {
			dataSourceManager = new MuiltMongoDataSourceManager(null, logicDataSourceMap, realDataSourceMap);
		}

		MongoComponent.getInstance().setDataSourceManager(dataSourceManager);
		MongoSupport.setManager(dataSourceManager);

		this.componentContext.setDataSourceVoMap(dataSourceVoMap);
		this.componentContext.setDefaultDataSource(defaultDataSource);
	}

	/**
	 * 解析mapper
	 */
	private void buildMapperNode(XmlNodeWrapper xNode) throws Throwable {
		if (null == xNode) {
			return;
		}
		String                tagName          = "mapper";
		String                resource         = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
		XmlMongoMapperBuilder xmlMapperBuilder = new XmlMongoMapperBuilder();
		xmlMapperBuilder.parse(this.componentContext, resource);
	}

	/**
	 * 解析Sharding
	 */
	private void buildShardingNode(XmlNodeWrapper xNode) throws Throwable {
		if (null == xNode) {
			return;
		}
		String                  tagName            = "sharding";
		String                  resource           = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
		XmlMongoShardingBuilder xmlShardingBuilder = new XmlMongoShardingBuilder();
		xmlShardingBuilder.parse(this.componentContext, resource);
	}

	private void buildPluginNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		String                      tagName        = "plugin";
		List<XmlMongoPluginBuilder> pluginBuilders = new ArrayList<XmlMongoPluginBuilder>();
		// 扫描所有的plugin
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper        xNode    = contexts.get(i);
			String                resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
			XmlMongoPluginBuilder builder  = new XmlMongoPluginBuilder();
			builder.setContext(resource, this.componentContext);
			// first
			builder.parseRef();
			pluginBuilders.add(builder);
		}
		// 注册所有的服务
		for (int i = 0; i < size; i++) {
			pluginBuilders.get(i).parseService();
			pluginBuilders.get(i).clean();
		}
	}

	//	private void buildPluginNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		int size = contexts.size();
	//		if (size == 0) {
	//			return;
	//		}
	//		List<String>         resourceList          = new ArrayList<String>();
	//		List<XmlNodeBuilder> xmlsqlNodeBuilderList = new ArrayList<XmlNodeBuilder>();
	//
	//		// 扫描所有的<SQL>
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode    = contexts.get(i);
	//			String         resource = StringUtils.trim(xNode.getStringAttribute("resource"));
	//			log.info("*** Start parsing(ref): " + resource);
	//			//InputStream inputStream = Resources.getResourceAsStream(resource);
	//			InputStream    inputStream       = ResourceManager.getInputStream(resource, false);
	//
	//			XmlNodeBuilder xmlSqlNodeBuilder = getXmlNodeBuilder(new XPathParser(inputStream));
	//
	//			xmlSqlNodeBuilder.parseRef();
	//			xmlsqlNodeBuilderList.add(xmlSqlNodeBuilder);
	//			resourceList.add(resource);
	//		}
	//
	//		// 注册所有的服务
	//		for (int i = 0; i < size; i++) {
	//			log.info("*** Start parsing(service): " + resourceList.get(i));
	//			xmlsqlNodeBuilderList.get(i).parseService();
	//		}
	//	}
	//
	//	private XmlNodeBuilder getXmlNodeBuilder(XPathParser parser) {
	//		XmlNodeWrapper _root = null;
	//		if (null != (_root = parser.evalNode("/mongoservices"))) {
	//			XmlNodeBuilder nodeBuilder = new XMLMongoNodeBuilder();
	//			nodeBuilder.setContext(_root, this.context);
	//			return nodeBuilder;
	//		} else {
	//			throw new XmlParseException("Unsupported root node in the service plug-in");
	//		}
	//	}

	//	@Override
	//	public void parse(XmlContext xmlContext, String resource) throws Throwable {
	//		log.info("*** Start parsing: " + resource);
	//		InputStream inputStream = ResourceManager.getInputStream(resource, true);
	//		this.xPathParser = new XPathParser(inputStream);
	//		context.setXmlContext((XmlGlobalContext) xmlContext);
	//		configurationElement(xPathParser.evalNode("/mongo-component"));
	//		context.clean();
	//		inputStream.close();
	//	}
	//	private Log								log					= LogFactory.getLog(getClass());
	//	private XPathParser						xPathParser			= null;
	//	private XmlMongoContext                context           = new XmlMongoContext();
	//	private void configurationElement(XmlNodeWrapper context) throws Throwable {
	//		// 解析配置项目
	//		buildConfigNodes(context.evalNodes("config-property"));
	//		// 解析dataSource
	//		List<MongoDataSourceVo> dsList      = buildDataSourceNodes(context.evalNodes("dataSource"));
	//		// 解析dataSourceGroup
	//		List<MongoDataSourceVo> dsGroupList = buildDataSourceGroupNodes(context.evalNodes("dataSourceGroup"));
	//		addDataSource(dsList, dsGroupList);
	//		buildMapperNodes(context.evalNodes("mapper"));
	//		buildShardingNodes(context.evalNodes("sharding"));
	//		buildPluginNodes(context.evalNodes("plugin"));
	//	}

	//	private void buildConfigNodes(List<XmlNodeWrapper> contexts) {
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
	//			MongoComponent.getInstance().config(configMap);
	//		}
	//	}

	//	@SuppressWarnings({ "unchecked", "rawtypes" })
	//	private List<MongoDataSourceVo> buildDataSourceNode(List<XmlNodeWrapper> contexts) throws Throwable {
	//		int                     size   = contexts.size();
	//		List<MongoDataSourceVo> dsList = new ArrayList<MongoDataSourceVo>();
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode = contexts.get(i);
	//			String         id    = StringUtils.trim(xNode.getStringAttribute("id"));
	//			if (dataSourceVoMap.containsKey(id)) {
	//				throw new XmlParseException("Duplicate mongo data source ID: " + id);
	//			}
	//			String  tmp       = null;
	//
	//			boolean defaultDs = false;
	//			tmp = StringUtils.trim(xNode.getStringAttribute("isDefault"));
	//			if (null != tmp) {
	//				if ("true".equalsIgnoreCase(tmp)) {
	//					if (null != this.defaultDataSource) {
	//						throw new XmlParseException("The default mongo data source must be unique.");
	//					} else {
	//						this.defaultDataSource = id;
	//					}
	//					defaultDs = true;
	//				}
	//			}
	//
	//			// String jndiName = StringUtils.trim(xNode.getStringAttribute("jndiName"));
	//			String              sharedUse = StringUtils.trim(xNode.getStringAttribute("sharedUse"));
	//
	//			//			Map<String, String> data = new HashMap<String, String>();
	//			//			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
	//			//			for (XmlNodeWrapper propertyNode : properties) {
	//			//				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
	//			//						StringUtils.trim(propertyNode.getStringAttribute("value")));
	//			//			}
	//
	//			Map<String, String> data      = null;
	//			String              resource  = StringUtils.trim(xNode.getStringAttribute("resource"));
	//			if (null != resource) {
	//				data = (Map) ResourceManager.getProperties(resource, true);
	//				data = PropertyUtils.keyToUpperCase(data);		// 还需要将Key全变成大写
	//			} else {
	//				data = new HashMap<String, String>();
	//				List<XmlNodeWrapper> properties = xNode.evalNodes("property");
	//				for (XmlNodeWrapper propertyNode : properties) {
	//					//					data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
	//					//							StringUtils.trim(propertyNode.getStringAttribute("value")));
	//					data.put(StringUtils.trim(propertyNode.getStringAttribute("name")), StringUtils.trim(propertyNode.getStringAttribute("value")));
	//				}
	//				PlaceholderResourceSupport.processMap(data);	// 占位替换
	//				data = PropertyUtils.keyToUpperCase(data);		// key转大写
	//			}
	//
	//			// MongoDataSourceVo dsVo = new MongoDataSourceVo(id, data, defaultDs);
	//			MongoDataSourceVo dsVo = new MongoDataSourceVo(id, data, defaultDs, sharedUse, TangYuanContainer.getInstance().getSystemName(), resource);
	//			dsList.add(dsVo);
	//			dataSourceVoMap.put(id, dsVo);
	//		}
	//		return dsList;
	//	}

	//	@SuppressWarnings({ "unchecked", "rawtypes" })
	//	private List<MongoDataSourceVo> buildDataSourceGroupNode(List<XmlNodeWrapper> contexts) throws Throwable {
	//		// log.info("解析数据源:" + contexts.size());
	//		int                     size   = contexts.size();
	//		List<MongoDataSourceVo> dsList = new ArrayList<MongoDataSourceVo>();
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode = contexts.get(i);
	//			String         id    = StringUtils.trim(xNode.getStringAttribute("groupId"));
	//			if (dataSourceVoMap.containsKey(id)) {
	//				throw new XmlParseException("Duplicate mongo data source: " + id);
	//			}
	//			String tmp = null;
	//			// String tmp = StringUtils.trim(xNode.getStringAttribute("type"));
	//			// ConnPoolType type = null;
	//			// ConnPoolType type = getConnPoolType(tmp);
	//			// if (null == type) {
	//			// throw new XmlParseException("无效的数据源类型");
	//			// }
	//			tmp = StringUtils.trim(xNode.getStringAttribute("start")); // xml v
	//			int start = 0;
	//			if (null != tmp) {
	//				start = Integer.parseInt(tmp);
	//			}
	//			tmp = StringUtils.trim(xNode.getStringAttribute("end")); // xml v
	//			int     end       = Integer.parseInt(tmp);
	//			boolean defaultDs = false;
	//			tmp = StringUtils.trim(xNode.getStringAttribute("isDefault"));
	//			if (null != tmp) {
	//				if ("true".equalsIgnoreCase(tmp)) {
	//					if (null != this.defaultDataSource) {
	//						throw new XmlParseException("The default mongo data source must be unique.");
	//					} else {
	//						this.defaultDataSource = id;
	//					}
	//					defaultDs = true;
	//				}
	//			}
	//
	//			//			Map<String, String> data = new HashMap<String, String>();
	//			//			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
	//			//			for (XmlNodeWrapper propertyNode : properties) {
	//			//				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
	//			//						StringUtils.trim(propertyNode.getStringAttribute("value")));
	//			//			}
	//
	//			Map<String, String> data     = null;
	//			String              resource = StringUtils.trim(xNode.getStringAttribute("resource"));
	//			if (null != resource) {
	//				data = (Map) ResourceManager.getProperties(resource, true);
	//				data = PropertyUtils.keyToUpperCase(data);		// 还需要将Key全变成大写
	//			} else {
	//				data = new HashMap<String, String>();
	//				List<XmlNodeWrapper> properties = xNode.evalNodes("property");
	//				for (XmlNodeWrapper propertyNode : properties) {
	//					//					data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
	//					//							StringUtils.trim(propertyNode.getStringAttribute("value")));
	//					data.put(StringUtils.trim(propertyNode.getStringAttribute("name")), StringUtils.trim(propertyNode.getStringAttribute("value")));
	//				}
	//				PlaceholderResourceSupport.processMap(data);	// 占位替换
	//				data = PropertyUtils.keyToUpperCase(data);		// key转大写
	//			}
	//
	//			// MongoDataSourceGroupVo dsGroupVo = new MongoDataSourceGroupVo(id, defaultDs, data, start, end);
	//			MongoDataSourceGroupVo dsGroupVo = new MongoDataSourceGroupVo(id, defaultDs, data, TangYuanContainer.getInstance().getSystemName(), start, end, resource);
	//
	//			dsList.add(dsGroupVo);
	//
	//			dataSourceVoMap.put(id, dsGroupVo);
	//		}
	//		return dsList;
	//	}

	/**
	 * 解析mapper
	 */
	//	private void buildMapperNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		int size = contexts.size();
	//		if (size == 0) {
	//			return;
	//		}
	//		if (size > 1) {
	//			throw new XmlParseException("<mapper> plugin can only have one.");
	//		}
	//		XmlNodeWrapper xNode    = contexts.get(0);
	//		String         resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml v
	//		log.info("*** Start parsing: " + resource);
	//		InputStream           inputStream      = ResourceManager.getInputStream(resource, false);
	//		XmlMongoMapperBuilder xmlMapperBuilder = new XmlMongoMapperBuilder(inputStream);
	//		xmlMapperBuilder.parse(this.context);
	//	}
	//	/** 解析Sharding */
	//	private void buildShardingNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		int size = contexts.size();
	//		if (size == 0) {
	//			return;
	//		}
	//		if (size > 1) {
	//			throw new XmlParseException("<sharding> can only have one at most.");
	//		}
	//		XmlNodeWrapper xNode    = contexts.get(0);
	//		String         resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml
	//		// v
	//		log.info("Start parsing: " + resource);
	//		InputStream             inputStream        = ResourceManager.getInputStream(resource, true);
	//		XmlMongoShardingBuilder xmlShardingBuilder = new XmlMongoShardingBuilder(inputStream);
	//		xmlShardingBuilder.parse(this.context);
	//	}
}
