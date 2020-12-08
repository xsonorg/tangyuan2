package org.xson.tangyuan.hive.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xson.tangyuan.hive.HiveComponent;
import org.xson.tangyuan.hive.datasource.AbstractDataSource;
import org.xson.tangyuan.hive.datasource.DataSourceGroupVo;
import org.xson.tangyuan.hive.datasource.DataSourceManager;
import org.xson.tangyuan.hive.datasource.DataSourceVo;
import org.xson.tangyuan.hive.datasource.DataSourceVo.ConnPoolType;
import org.xson.tangyuan.hive.datasource.MuiltDataSourceManager;
import org.xson.tangyuan.hive.datasource.SimpleDataSourceManager;
import org.xson.tangyuan.type.TypeHandler;
import org.xson.tangyuan.type.TypeHandlerRegistry;
import org.xson.tangyuan.util.MixedResourceManager;
import org.xson.tangyuan.util.PropertyUtils;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlHiveComponentBuilder extends DefaultXmlComponentBuilder {

	private XmlHiveContext            componentContext       = null;
	private Map<String, DataSourceVo> dataSourceVoMap        = new HashMap<String, DataSourceVo>();
	private int                       defaultDataSourceCount = 0;

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		this.componentContext = (XmlHiveContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "hive-component", true);
		this.configurationElement();
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();
		this.componentContext = null;
		this.dataSourceVoMap = null;
	}

	private void configurationElement() throws Throwable {
		// 解析配置项目
		buildConfigNode(this.root.evalNodes("config-property"), HiveComponent.getInstance());
		// 解析dataSource
		List<DataSourceVo> dsList      = buildDataSourceNode(this.root.evalNodes("dataSource"));
		// 解析dataSourceGroup
		List<DataSourceVo> dsGroupList = buildDataSourceGroupNode(this.root.evalNodes("dataSourceGroup"));
		addDataSource(dsList, dsGroupList);

		//		buildMapperNodes(context.evalNodes("mapper"));
		//		buildPluginNodes(context.evalNodes("plugin"));

		// 解析mapper
		buildMapperNode(getMostOneNode(this.root, "mapper", lang("xml.tag.mostone", "mapper")));
		// 解析sharding
		buildShardingNode(getMostOneNode(this.root, "sharding", lang("xml.tag.mostone", "sharding")));
		// 解析plugin
		buildPluginNode(this.root.evalNodes("plugin"));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<DataSourceVo> buildDataSourceNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int                size    = contexts.size();
		String             tagName = "dataSource";

		List<DataSourceVo> dsList  = new ArrayList<DataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode     = contexts.get(i);

			String         id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String         _type     = getStringFromAttr(xNode, "type", lang("xml.tag.attribute.empty", "type", tagName, this.resource));
			boolean        defaultDs = getBoolFromAttr(xNode, "isDefault", false);
			String         resource  = getStringFromAttr(xNode, "resource");
			String         sharedUse = getStringFromAttr(xNode, "sharedUse");

			if (dataSourceVoMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			ConnPoolType type = getConnPoolType(_type);
			if (null == type) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid", "type", tagName, this.resource));
			}
			if (defaultDs) {
				defaultDataSourceCount++;
			}

			Map<String, String> data = null;
			if (null != resource) {
				Properties p = MixedResourceManager.getProperties(resource, true, true);
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

			// key转大写
			data = PropertyUtils.keyToUpperCase(data);

			DataSourceVo dsVo = new DataSourceVo(id, type, defaultDs, data, sharedUse, null, resource);
			dsList.add(dsVo);
			dataSourceVoMap.put(id, dsVo);
		}
		return dsList;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<DataSourceVo> buildDataSourceGroupNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int                size    = contexts.size();
		String             tagName = "dataSourceGroup";

		List<DataSourceVo> dsList  = new ArrayList<DataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode     = contexts.get(i);

			String         id        = getStringFromAttr(xNode, "groupId", lang("xml.tag.attribute.empty", "groupId", tagName, this.resource));
			String         _type     = getStringFromAttr(xNode, "type", lang("xml.tag.attribute.empty", "type", tagName, this.resource));
			boolean        defaultDs = getBoolFromAttr(xNode, "isDefault", false);
			String         resource  = getStringFromAttr(xNode, "resource");
			int            start     = getIntFromAttr(xNode, "start", 0);
			int            end       = getIntFromAttr(xNode, "end", lang("xml.tag.attribute.empty", "end", tagName, this.resource));

			if (dataSourceVoMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			ConnPoolType type = getConnPoolType(_type);
			if (null == type) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid", "type", tagName, this.resource));
			}
			if (defaultDs) {
				defaultDataSourceCount++;
			}

			Map<String, String> data = null;
			if (null != resource) {
				Properties p = MixedResourceManager.getProperties(resource, true, false);
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

			// key转大写
			data = PropertyUtils.keyToUpperCase(data);

			DataSourceGroupVo dsGroupVo = new DataSourceGroupVo(id, type, defaultDs, data, null, start, end, resource);
			dsList.add(dsGroupVo);

			dataSourceVoMap.put(id, dsGroupVo);
		}
		return dsList;
	}

	private void addDataSource(List<DataSourceVo> dsList, List<DataSourceVo> dsGroupList) throws Exception {
		List<DataSourceVo> allList = new ArrayList<DataSourceVo>();
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

		DataSourceManager               dataSourceManager  = null;
		String                          _defaultDsKey      = null;
		Map<String, DataSourceVo>       logicDataSourceMap = new HashMap<String, DataSourceVo>();
		Map<String, AbstractDataSource> realDataSourceMap  = new HashMap<String, AbstractDataSource>();

		for (DataSourceVo dsVo : allList) {
			dsVo.start(logicDataSourceMap, realDataSourceMap);
			if (dsVo.isDefaultDs()) {
				_defaultDsKey = dsVo.getId();
			}
		}

		// 再次判断
		if (logicDataSourceMap.size() == 1 && realDataSourceMap.size() == 1) {
			_defaultDsKey = allList.get(0).getId();
			AbstractDataSource onoDs = realDataSourceMap.get(_defaultDsKey);
			dataSourceManager = new SimpleDataSourceManager(null, onoDs, _defaultDsKey);
		} else {
			dataSourceManager = new MuiltDataSourceManager(null, logicDataSourceMap, realDataSourceMap, _defaultDsKey);
		}

		this.componentContext.setDataSourceVoMap(this.dataSourceVoMap);
		HiveComponent.getInstance().setDataSourceManager(dataSourceManager);
	}

	//	/**
	//	 * 解析mapper
	//	 */
	//	private void buildMapperNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		int size = contexts.size();
	//		if (size == 0) {
	//			Map<String, TypeHandler<?>> jdbcTypeMap         = new HashMap<String, TypeHandler<?>>();
	//			TypeHandlerRegistry         typeHandlerRegistry = new TypeHandlerRegistry();
	//			typeHandlerRegistry.init(jdbcTypeMap);
	//			HiveComponent.getInstance().setTypeHandlerRegistry(typeHandlerRegistry);
	//			return;
	//		}
	//		if (size > 1) {
	//			throw new XmlParseException("<mapper> plugin can only have one.");
	//		}
	//		XmlNodeWrapper xNode    = contexts.get(0);
	//		String         resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml v
	//
	//		// log.info("*** Start parsing: " + resource);
	//		log.info("*** Start parsing mapper plugin: " + resource);
	//
	//		// InputStream inputStream = Resources.getResourceAsStream(resource);
	//		InputStream      inputStream      = ResourceManager.getInputStream(resource, false);
	//
	//		XmlMapperBuilder xmlMapperBuilder = new XmlMapperBuilder(inputStream);
	//		xmlMapperBuilder.parse(this.sqlContext);
	//	}

	/**
	 * 解析mapper
	 */
	private void buildMapperNode(XmlNodeWrapper xNode) throws Throwable {
		if (null == xNode) {
			Map<String, TypeHandler<?>> jdbcTypeMap         = new HashMap<String, TypeHandler<?>>();
			TypeHandlerRegistry         typeHandlerRegistry = new TypeHandlerRegistry();
			typeHandlerRegistry.init(jdbcTypeMap);
			HiveComponent.getInstance().setTypeHandlerRegistry(typeHandlerRegistry);
			return;
		}
		String               tagName          = "mapper";
		String               resource         = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
		XmlHiveMapperBuilder xmlMapperBuilder = new XmlHiveMapperBuilder();
		xmlMapperBuilder.parse(this.componentContext, resource);
	}

	/**
	 * 解析Sharding
	 */
	private void buildShardingNode(XmlNodeWrapper xNode) throws Throwable {
		if (null == xNode) {
			return;
		}
		String                 tagName            = "sharding";
		String                 resource           = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
		XmlHiveShardingBuilder xmlShardingBuilder = new XmlHiveShardingBuilder();
		xmlShardingBuilder.parse(this.componentContext, resource);
	}

	//	private void buildPluginNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		int size = contexts.size();
	//		if (size == 0) {
	//			return;
	//		}
	//		List<String>         resourceList       = new ArrayList<String>();
	//		List<XmlNodeBuilder> xmlNodeBuilderList = new ArrayList<XmlNodeBuilder>();
	//
	//		// 扫描所有的<SQL>
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode    = contexts.get(i);
	//			String         resource = StringUtils.trim(xNode.getStringAttribute("resource"));
	//
	//			log.info("*** Start parsing(ref): " + resource);
	//			// InputStream inputStream = Resources.getResourceAsStream(resource);
	//			InputStream    inputStream = ResourceManager.getInputStream(resource, false);
	//			XPathParser    parser      = new XPathParser(inputStream);
	//			XmlNodeBuilder nodeBuilder = getXmlNodeBuilder(parser);
	//			nodeBuilder.parseRef();
	//
	//			xmlNodeBuilderList.add(nodeBuilder);
	//			resourceList.add(resource);
	//		}
	//
	//		// 注册所有的服务
	//		for (int i = 0; i < size; i++) {
	//			log.info("*** Start parsing(service): " + resourceList.get(i));
	//			xmlNodeBuilderList.get(i).parseService();
	//		}
	//	}

	private void buildPluginNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		String                     tagName        = "plugin";
		List<XmlHivePluginBuilder> pluginBuilders = new ArrayList<XmlHivePluginBuilder>();
		// 扫描所有的plugin
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper       xNode    = contexts.get(i);
			String               resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
			XmlHivePluginBuilder builder  = new XmlHivePluginBuilder();
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

	private ConnPoolType getConnPoolType(String type) {
		if (ConnPoolType.C3P0.toString().equalsIgnoreCase(type)) {
			return ConnPoolType.C3P0;
		} else if (ConnPoolType.DBCP.toString().equalsIgnoreCase(type)) {
			return ConnPoolType.DBCP;
		} else if (ConnPoolType.PROXOOL.toString().equalsIgnoreCase(type)) {
			return ConnPoolType.PROXOOL;
		} else if (ConnPoolType.DRUID.toString().equalsIgnoreCase(type)) {
			return ConnPoolType.DRUID;
		}
		return null;
	}

	///////////////////////////////////////

	//	private Log                       log                    = LogFactory.getLog(getClass());
	//	private XPathParser               xPathParser            = null;
	//	private Map<String, DataSourceVo> dataSourceVoMap        = new HashMap<String, DataSourceVo>();
	//	private int                       defaultDataSourceCount = 0;
	//	private XmlSqlContext             sqlContext             = new XmlSqlContext();

	//	@Override
	//	public void parse(XmlContext xmlContext, String resource) throws Throwable {
	//		log.info("*** Start parsing: " + resource);
	//
	//		InputStream inputStream = ResourceManager.getInputStream(resource, true);
	//
	//		this.xPathParser = new XPathParser(inputStream);
	//		sqlContext.setXmlContext((XmlGlobalContext) xmlContext);
	//		configurationElement(xPathParser.evalNode("/hive-component"));
	//		sqlContext.clean();
	//
	//		inputStream.close();
	//	}
	//	private void configurationElement(XmlNodeWrapper context) throws Throwable {
	//		buildConfigNodes(context.evalNodes("config-property"));// 解析配置项目
	//		List<DataSourceVo> dsList      = buildDataSourceNodes(context.evalNodes("dataSource"));// 解析dataSource
	//		List<DataSourceVo> dsGroupList = buildDataSourceGroupNodes(context.evalNodes("dataSourceGroup"));// 解析dataSourceGroup
	//		addDataSource(dsList, dsGroupList);
	//		// buildTransactionNodes(context.evalNodes("transaction"));// 解析transaction
	//		// buildSetDefaultTransaction(context.evalNodes("setDefaultTransaction"));// 解析默认的transaction
	//		buildMapperNodes(context.evalNodes("mapper"));
	//		// buildShardingNodes(context.evalNodes("sharding"));
	//		buildPluginNodes(context.evalNodes("plugin"));
	//	}

	//	private XmlNodeBuilder getXmlNodeBuilder(XPathParser parser) {
	//		XmlNodeWrapper _root = null;
	//		if (null != (_root = parser.evalNode("/hiveservices"))) {
	//			XmlNodeBuilder nodeBuilder = new XMLHiveNodeBuilder();
	//			nodeBuilder.setContext(_root, sqlContext);
	//			return nodeBuilder;
	//		} else {
	//			throw new XmlParseException("Unsupported root node in the service plug-in");
	//		}
	//	}

	//	private ConnPoolType getConnPoolType(String type) {
	//		if ("JNDI".equalsIgnoreCase(type)) {
	//			return ConnPoolType.JNDI;
	//		} else if ("C3P0".equalsIgnoreCase(type)) {
	//			return ConnPoolType.C3P0;
	//		} else if ("DBCP".equalsIgnoreCase(type)) {
	//			return ConnPoolType.DBCP;
	//		} else if ("PROXOOL".equalsIgnoreCase(type)) {
	//			return ConnPoolType.PROXOOL;
	//		} else if ("DRUID".equalsIgnoreCase(type)) {
	//			return ConnPoolType.DRUID;
	//		} else if ("SHARE".equalsIgnoreCase(type)) {
	//			return ConnPoolType.SHARE;
	//		}
	//		return null;
	//	}

	//	@SuppressWarnings({ "unchecked", "rawtypes" })
	//	private List<DataSourceVo> buildDataSourceNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		int                size   = contexts.size();
	//		List<DataSourceVo> dsList = new ArrayList<DataSourceVo>();
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode = contexts.get(i);
	//			String         id    = StringUtils.trim(xNode.getStringAttribute("id"));
	//			if (dataSourceVoMap.containsKey(id)) {
	//				throw new XmlParseException("Duplicate data source ID: " + id);
	//			}
	//			String       tmp  = StringUtils.trim(xNode.getStringAttribute("type"));
	//			ConnPoolType type = getConnPoolType(tmp);
	//			if (null == type) {
	//				throw new XmlParseException("Invalid data source type.");
	//			}
	//
	//			String sharedUse = StringUtils.trim(xNode.getStringAttribute("sharedUse"));
	//			if (ConnPoolType.SHARE == type && null == sharedUse) {
	//				throw new XmlParseException("In share type, the sharedUse can not be missing.");
	//			}
	//
	//			boolean defaultDs = false;
	//			tmp = StringUtils.trim(xNode.getStringAttribute("isDefault"));
	//			if (null != tmp) {
	//				if ("true".equalsIgnoreCase(tmp)) {
	//					defaultDs = true;
	//					defaultDataSourceCount++;
	//				}
	//			}
	//
	//			// Map<String, String> data = new HashMap<String, String>();
	//			// List<XmlNodeWrapper> properties = xNode.evalNodes("property");
	//			// for (XmlNodeWrapper propertyNode : properties) {
	//			// data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
	//			// StringUtils.trim(propertyNode.getStringAttribute("value")));
	//			// }
	//
	//			Map<String, String> data     = null;
	//			String              resource = StringUtils.trim(xNode.getStringAttribute("resource"));
	//			if (null != resource) {
	//				data = (Map) ResourceManager.getProperties(resource, true);
	//				data = PropertyUtils.keyToUpperCase(data); // 还需要将Key全变成大写
	//			} else {
	//				data = new HashMap<String, String>();
	//				List<XmlNodeWrapper> properties = xNode.evalNodes("property");
	//				for (XmlNodeWrapper propertyNode : properties) {
	//					// data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
	//					// StringUtils.trim(propertyNode.getStringAttribute("value")));
	//					data.put(StringUtils.trim(propertyNode.getStringAttribute("name")), StringUtils.trim(propertyNode.getStringAttribute("value")));
	//				}
	//				PlaceholderResourceSupport.processMap(data); // 占位替换
	//				data = PropertyUtils.keyToUpperCase(data); // key转大写
	//			}
	//
	//			DataSourceVo dsVo = new DataSourceVo(id, type, defaultDs, data, sharedUse, TangYuanContainer.getInstance().getSystemName(), resource);
	//			dsList.add(dsVo);
	//			dataSourceVoMap.put(id, dsVo);
	//		}
	//		return dsList;
	//	}

	//	@SuppressWarnings({ "unchecked", "rawtypes" })
	//	private List<DataSourceVo> buildDataSourceGroupNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//
	//		int                size   = contexts.size();
	//		List<DataSourceVo> dsList = new ArrayList<DataSourceVo>();
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode = contexts.get(i);
	//			String         id    = StringUtils.trim(xNode.getStringAttribute("groupId"));
	//			if (dataSourceVoMap.containsKey(id)) {
	//				throw new XmlParseException("Duplicate data source ID: " + id);
	//			}
	//			String       tmp  = StringUtils.trim(xNode.getStringAttribute("type")); // xml v
	//			ConnPoolType type = getConnPoolType(tmp);
	//			if (null == type) {
	//				throw new XmlParseException("Invalid data source type.");
	//			}
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
	//					defaultDs = true;
	//					defaultDataSourceCount++;
	//				}
	//			}
	//
	//			// Map<String, String> data = new HashMap<String, String>();
	//			// List<XmlNodeWrapper> properties = xNode.evalNodes("property");
	//			// for (XmlNodeWrapper propertyNode : properties) {
	//			// data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
	//			// StringUtils.trim(propertyNode.getStringAttribute("value")));
	//			// }
	//
	//			Map<String, String> data     = null;
	//			String              resource = StringUtils.trim(xNode.getStringAttribute("resource"));
	//			if (null != resource) {
	//				data = (Map) ResourceManager.getProperties(resource, true);
	//				data = PropertyUtils.keyToUpperCase(data); // 还需要将Key全变成大写
	//			} else {
	//				data = new HashMap<String, String>();
	//				List<XmlNodeWrapper> properties = xNode.evalNodes("property");
	//				for (XmlNodeWrapper propertyNode : properties) {
	//					data.put(StringUtils.trim(propertyNode.getStringAttribute("name")), StringUtils.trim(propertyNode.getStringAttribute("value")));
	//				}
	//				PlaceholderResourceSupport.processMap(data); // 占位替换
	//				data = PropertyUtils.keyToUpperCase(data); // key转大写
	//			}
	//
	//			DataSourceGroupVo dsGroupVo = new DataSourceGroupVo(id, type, defaultDs, data, TangYuanContainer.getInstance().getSystemName(), start, end, resource);
	//			dsList.add(dsGroupVo);
	//
	//			dataSourceVoMap.put(id, dsGroupVo);
	//		}
	//		return dsList;
	//	}

	//	private void addDataSource(List<DataSourceVo> dsList, List<DataSourceVo> dsGroupList) throws Exception {
	//		List<DataSourceVo> allList = new ArrayList<DataSourceVo>();
	//		if (null != dsList && dsList.size() > 0) {
	//			allList.addAll(dsList);
	//		}
	//		if (null != dsGroupList && dsGroupList.size() > 0) {
	//			allList.addAll(dsGroupList);
	//		}
	//		if (0 == allList.size()) {
	//			throw new XmlParseException("No data source...");
	//		}
	//
	//		if (defaultDataSourceCount > 1) {
	//			throw new XmlParseException("The default data source can have at most one.");
	//		}
	//
	//		DataSourceManager               dataSourceManager  = null;
	//		Map<String, DataSourceVo>       logicDataSourceMap = new HashMap<String, DataSourceVo>();
	//		Map<String, AbstractDataSource> realDataSourceMap  = new HashMap<String, AbstractDataSource>();
	//
	//		String                          _defaultDsKey      = null;
	//
	//		for (DataSourceVo dsVo : allList) {
	//			dsVo.start(logicDataSourceMap, realDataSourceMap);
	//			if (dsVo.isDefaultDs()) {
	//				_defaultDsKey = dsVo.getId();
	//			}
	//		}
	//
	//		// 再次判断
	//		if (logicDataSourceMap.size() == 1 && realDataSourceMap.size() == 1) {
	//			_defaultDsKey = allList.get(0).getId();
	//			AbstractDataSource onoDs = realDataSourceMap.get(_defaultDsKey);
	//			dataSourceManager = new SimpleDataSourceManager(TangYuanContainer.getInstance().getSystemName(), onoDs, _defaultDsKey);
	//		} else {
	//			dataSourceManager = new MuiltDataSourceManager(TangYuanContainer.getInstance().getSystemName(), logicDataSourceMap, realDataSourceMap, _defaultDsKey);
	//		}
	//		HiveComponent.getInstance().setDataSourceManager(dataSourceManager);
	//
	//		this.sqlContext.setDataSourceVoMap(this.dataSourceVoMap);
	//	}

}
