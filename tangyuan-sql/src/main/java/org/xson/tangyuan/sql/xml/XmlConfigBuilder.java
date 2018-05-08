package org.xson.tangyuan.sql.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.sql.SqlComponent;
import org.xson.tangyuan.sql.datasource.AbstractDataSource;
import org.xson.tangyuan.sql.datasource.DataSourceGroupVo;
import org.xson.tangyuan.sql.datasource.DataSourceManager;
import org.xson.tangyuan.sql.datasource.DataSourceVo;
import org.xson.tangyuan.sql.datasource.DataSourceVo.ConnPoolType;
import org.xson.tangyuan.sql.datasource.MuiltDataSourceManager;
import org.xson.tangyuan.sql.datasource.SimpleDataSourceManager;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.sql.xml.node.XMLSqlNodeBuilder;
import org.xson.tangyuan.type.TypeHandler;
import org.xson.tangyuan.type.TypeHandlerRegistry;
import org.xson.tangyuan.util.PlaceholderResourceSupport;
import org.xson.tangyuan.util.PropertyUtils;
import org.xson.tangyuan.util.ResourceManager;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlExtendBuilder;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlNodeBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlConfigBuilder implements XmlExtendBuilder {

	private Log							log						= LogFactory.getLog(getClass());
	private XPathParser					xPathParser				= null;

	private Map<String, DataSourceVo>	dataSourceVoMap			= new HashMap<String, DataSourceVo>();
	private int							defaultDataSourceCount	= 0;

	private XmlSqlContext				sqlContext				= new XmlSqlContext();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		//		InputStream inputStream = Resources.getResourceAsStream(resource);
		//		inputStream = PlaceholderResourceSupport.processInputStream(inputStream,
		//				TangYuanContainer.getInstance().getXmlGlobalContext().getPlaceholderMap());

		InputStream inputStream = ResourceManager.getInputStream(resource, true);

		this.xPathParser = new XPathParser(inputStream);
		sqlContext.setXmlContext((XmlGlobalContext) xmlContext);
		configurationElement(xPathParser.evalNode("/sql-component"));
		sqlContext.clean();
		
		inputStream.close();
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildConfigNodes(context.evalNodes("config-property"));// 解析配置项目
		List<DataSourceVo> dsList = buildDataSourceNodes(context.evalNodes("dataSource"));// 解析dataSource
		List<DataSourceVo> dsGroupList = buildDataSourceGroupNodes(context.evalNodes("dataSourceGroup"));// 解析dataSourceGroup
		addDataSource(dsList, dsGroupList);
		buildTransactionNodes(context.evalNodes("transaction"));// 解析transaction
		buildSetDefaultTransaction(context.evalNodes("setDefaultTransaction"));// 解析默认的transaction
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
			SqlComponent.getInstance().config(configMap);
		}
	}

	private void buildSetDefaultTransaction(List<XmlNodeWrapper> contexts) {
		int size = contexts.size();
		if (size > 1) {
			throw new XmlParseException("The default transaction setting can have at most one.");
		}
		if (size == 0) {
			return;
		}
		XmlNodeWrapper xNode = contexts.get(0);
		String type = StringUtils.trim(xNode.getStringAttribute("type"));
		List<XmlNodeWrapper> properties = xNode.evalNodes("property");
		List<String[]> ruleList = new ArrayList<String[]>();
		for (XmlNodeWrapper propertyNode : properties) {
			// TODO check value
			ruleList.add(new String[] { StringUtils.trim(propertyNode.getStringAttribute("name")),
					StringUtils.trim(propertyNode.getStringAttribute("value")) });
		}
		this.sqlContext.getTransactionMatcher().setTypeAndRule(type, ruleList);
		log.info("add default transaction rule, type is " + type);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<DataSourceVo> buildDataSourceNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		List<DataSourceVo> dsList = new ArrayList<DataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));
			if (dataSourceVoMap.containsKey(id)) {
				throw new XmlParseException("Duplicate data source ID: " + id);
			}
			String tmp = StringUtils.trim(xNode.getStringAttribute("type"));
			ConnPoolType type = getConnPoolType(tmp);
			if (null == type) {
				throw new XmlParseException("Invalid data source type.");
			}

			String sharedUse = StringUtils.trim(xNode.getStringAttribute("sharedUse"));
			if (ConnPoolType.SHARE == type && null == sharedUse) {
				throw new XmlParseException("In share type, the sharedUse can not be missing.");
			}

			boolean defaultDs = false;
			tmp = StringUtils.trim(xNode.getStringAttribute("isDefault"));
			if (null != tmp) {
				if ("true".equalsIgnoreCase(tmp)) {
					defaultDs = true;
					defaultDataSourceCount++;
				}
			}

			//			Map<String, String> data = new HashMap<String, String>();
			//			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			//			for (XmlNodeWrapper propertyNode : properties) {
			//				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
			//						StringUtils.trim(propertyNode.getStringAttribute("value")));
			//			}

			Map<String, String> data = null;
			String resource = StringUtils.trim(xNode.getStringAttribute("resource"));
			if (null != resource) {
				data = (Map) ResourceManager.getProperties(resource, true);
				data = PropertyUtils.keyToUpperCase(data);		// 还需要将Key全变成大写
			} else {
				data = new HashMap<String, String>();
				List<XmlNodeWrapper> properties = xNode.evalNodes("property");
				for (XmlNodeWrapper propertyNode : properties) {
					//					data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
					//							StringUtils.trim(propertyNode.getStringAttribute("value")));
					data.put(StringUtils.trim(propertyNode.getStringAttribute("name")), StringUtils.trim(propertyNode.getStringAttribute("value")));
				}
				PlaceholderResourceSupport.processMap(data);	// 占位替换
				data = PropertyUtils.keyToUpperCase(data);		// key转大写
			}

			DataSourceVo dsVo = new DataSourceVo(id, type, defaultDs, data, sharedUse, TangYuanContainer.getInstance().getSystemName(), resource);
			dsList.add(dsVo);
			dataSourceVoMap.put(id, dsVo);
		}
		return dsList;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<DataSourceVo> buildDataSourceGroupNodes(List<XmlNodeWrapper> contexts) throws Throwable {

		int size = contexts.size();
		List<DataSourceVo> dsList = new ArrayList<DataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("groupId"));
			if (dataSourceVoMap.containsKey(id)) {
				throw new XmlParseException("Duplicate data source ID: " + id);
			}
			String tmp = StringUtils.trim(xNode.getStringAttribute("type")); // xml v
			ConnPoolType type = getConnPoolType(tmp);
			if (null == type) {
				throw new XmlParseException("Invalid data source type.");
			}
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
					defaultDs = true;
					defaultDataSourceCount++;
				}
			}

			//			Map<String, String> data = new HashMap<String, String>();
			//			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			//			for (XmlNodeWrapper propertyNode : properties) {
			//				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
			//						StringUtils.trim(propertyNode.getStringAttribute("value")));
			//			}

			Map<String, String> data = null;
			String resource = StringUtils.trim(xNode.getStringAttribute("resource"));
			if (null != resource) {
				data = (Map) ResourceManager.getProperties(resource, true);
				data = PropertyUtils.keyToUpperCase(data);		// 还需要将Key全变成大写
			} else {
				data = new HashMap<String, String>();
				List<XmlNodeWrapper> properties = xNode.evalNodes("property");
				for (XmlNodeWrapper propertyNode : properties) {
					data.put(StringUtils.trim(propertyNode.getStringAttribute("name")), StringUtils.trim(propertyNode.getStringAttribute("value")));
				}
				PlaceholderResourceSupport.processMap(data);	// 占位替换
				data = PropertyUtils.keyToUpperCase(data);		// key转大写
			}

			DataSourceGroupVo dsGroupVo = new DataSourceGroupVo(id, type, defaultDs, data, TangYuanContainer.getInstance().getSystemName(), start,
					end, resource);
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
			throw new XmlParseException("No data source...");
		}

		if (defaultDataSourceCount > 1) {
			throw new XmlParseException("The default data source can have at most one.");
		}

		DataSourceManager dataSourceManager = null;
		Map<String, DataSourceVo> logicDataSourceMap = new HashMap<String, DataSourceVo>();
		Map<String, AbstractDataSource> realDataSourceMap = new HashMap<String, AbstractDataSource>();

		String _defaultDsKey = null;

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
			dataSourceManager = new SimpleDataSourceManager(TangYuanContainer.getInstance().getSystemName(), onoDs, _defaultDsKey);
		} else {
			dataSourceManager = new MuiltDataSourceManager(TangYuanContainer.getInstance().getSystemName(), logicDataSourceMap, realDataSourceMap,
					_defaultDsKey);
		}
		SqlComponent.getInstance().setDataSourceManager(dataSourceManager);
	}

	private void buildTransactionNodes(List<XmlNodeWrapper> contexts) {
		// log.info("解析事务定义:" + contexts.size());
		int size = contexts.size();
		if (0 == size) {
			return;
		}
		Map<String, XTransactionDefinition> transactionMap = new HashMap<String, XTransactionDefinition>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));
			if (null != transactionMap.get(id)) {
				throw new XmlParseException("Duplicate transaction definition: " + id);
			}
			String name = StringUtils.trim(xNode.getStringAttribute("name"));

			String tmp = StringUtils.trim(xNode.getStringAttribute("behavior"));
			Integer behavior = null;
			if (null != tmp) {
				behavior = getBehaviorValue(tmp);
			}

			tmp = StringUtils.trim(xNode.getStringAttribute("isolation"));
			Integer isolation = null;
			if (null != tmp) {
				isolation = getIsolationValue(tmp);
			}

			tmp = StringUtils.trim(xNode.getStringAttribute("timeout"));
			Integer timeout = null;
			if (null != tmp) {
				timeout = Integer.parseInt(tmp);
			}

			tmp = StringUtils.trim(xNode.getStringAttribute("readOnly"));
			Boolean readOnly = null;
			if (null != tmp) {
				readOnly = Boolean.parseBoolean(tmp);
			}

			XTransactionDefinition transactionDefinition = new XTransactionDefinition(id, name, behavior, isolation, timeout, readOnly);
			transactionMap.put(id, transactionDefinition);

			log.info("add transaction definition: " + id);
		}

		this.sqlContext.getTransactionMatcher().setTransactionMap(transactionMap);
	}

	/**
	 * 解析mapper
	 */
	private void buildMapperNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			Map<String, TypeHandler<?>> jdbcTypeMap = new HashMap<String, TypeHandler<?>>();
			TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
			typeHandlerRegistry.init(jdbcTypeMap);
			SqlComponent.getInstance().setTypeHandlerRegistry(typeHandlerRegistry);
			return;
		}
		if (size > 1) {
			throw new XmlParseException("<mapper> plugin can only have one.");
		}
		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml v

		//log.info("*** Start parsing: " + resource);
		log.info("*** Start parsing mapper plugin: " + resource);

		//InputStream inputStream = Resources.getResourceAsStream(resource);
		InputStream inputStream = ResourceManager.getInputStream(resource, false);

		XmlMapperBuilder xmlMapperBuilder = new XmlMapperBuilder(inputStream);
		xmlMapperBuilder.parse(this.sqlContext);
	}

	/**
	 * 解析Sharding
	 */
	private void buildShardingNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		if (size > 1) {
			throw new XmlParseException("<sharding> can only have one at most.");
		}
		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml v
		// log.info("*** Start parsing: " + resource);
		log.info("*** Start parsing sharding plugin: " + resource);
		// InputStream inputStream = Resources.getResourceAsStream(resource);
		InputStream inputStream = ResourceManager.getInputStream(resource, true);
		XmlShardingBuilder xmlShardingBuilder = new XmlShardingBuilder(inputStream);
		xmlShardingBuilder.parse(this.sqlContext);
	}

	private void buildPluginNodes(List<XmlNodeWrapper> contexts) throws Throwable {
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

			log.info("*** Start parsing(ref): " + resource);
			// InputStream inputStream = Resources.getResourceAsStream(resource);
			InputStream inputStream = ResourceManager.getInputStream(resource, false);
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
		if (null != (_root = parser.evalNode("/sqlservices"))) {
			XmlNodeBuilder nodeBuilder = new XMLSqlNodeBuilder();
			nodeBuilder.setContext(_root, sqlContext);
			return nodeBuilder;
		} else {
			throw new XmlParseException("Unsupported root node in the service plug-in");
		}
		// else if (null != (_root = parser.evalNode("/javaservices"))) {
		// XmlNodeBuilder nodeBuilder = new XMLJavaNodeBuilder();
		// nodeBuilder.setContext(_root, sqlContext);
		// return nodeBuilder;
		// }
	}

	private ConnPoolType getConnPoolType(String type) {
		if ("JNDI".equalsIgnoreCase(type)) {
			return ConnPoolType.JNDI;
		} else if ("C3P0".equalsIgnoreCase(type)) {
			return ConnPoolType.C3P0;
		} else if ("DBCP".equalsIgnoreCase(type)) {
			return ConnPoolType.DBCP;
		} else if ("PROXOOL".equalsIgnoreCase(type)) {
			return ConnPoolType.PROXOOL;
		} else if ("DRUID".equalsIgnoreCase(type)) {
			return ConnPoolType.DRUID;
		} else if ("SHARE".equalsIgnoreCase(type)) {
			return ConnPoolType.SHARE;
		}
		return null;
	}

	private int getBehaviorValue(String str) {
		if ("REQUIRED".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_REQUIRED;
		} else if ("SUPPORTS".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_SUPPORTS;
		} else if ("MANDATORY".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_MANDATORY;
		} else if ("REQUIRES_NEW".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_REQUIRES_NEW;
		} else if ("NOT_SUPPORTED".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_NOT_SUPPORTED;
		} else if ("NEVER".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_NEVER;
		} else if ("NESTED".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_NESTED;
		} else {
			return XTransactionDefinition.PROPAGATION_REQUIRED;
		}
	}

	private int getIsolationValue(String str) {
		if ("READ_UNCOMMITTED".equalsIgnoreCase(str)) {
			return XTransactionDefinition.ISOLATION_READ_UNCOMMITTED;
		} else if ("READ_COMMITTED".equalsIgnoreCase(str)) {
			return XTransactionDefinition.ISOLATION_READ_COMMITTED;
		} else if ("REPEATABLE_READ".equalsIgnoreCase(str)) {
			return XTransactionDefinition.ISOLATION_REPEATABLE_READ;
		} else if ("SERIALIZABLE".equalsIgnoreCase(str)) {
			return XTransactionDefinition.ISOLATION_SERIALIZABLE;
		} else {
			return XTransactionDefinition.ISOLATION_DEFAULT;
		}
	}

}
