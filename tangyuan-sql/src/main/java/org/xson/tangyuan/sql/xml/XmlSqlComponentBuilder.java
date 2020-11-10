package org.xson.tangyuan.sql.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xson.tangyuan.sql.SqlComponent;
import org.xson.tangyuan.sql.datasource.AbstractDataSource;
import org.xson.tangyuan.sql.datasource.DataSourceGroupVo;
import org.xson.tangyuan.sql.datasource.DataSourceManager;
import org.xson.tangyuan.sql.datasource.DataSourceVo;
import org.xson.tangyuan.sql.datasource.DataSourceVo.ConnPoolType;
import org.xson.tangyuan.sql.datasource.MuiltDataSourceManager;
import org.xson.tangyuan.sql.datasource.SimpleDataSourceManager;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.type.TypeHandler;
import org.xson.tangyuan.type.TypeHandlerRegistry;
import org.xson.tangyuan.util.MixedResourceManager;
import org.xson.tangyuan.util.PropertyUtils;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlSqlComponentBuilder extends DefaultXmlComponentBuilder {

	private XmlSqlContext				componentContext		= null;
	private Map<String, DataSourceVo>	dataSourceVoMap			= new HashMap<String, DataSourceVo>();
	private int							defaultDataSourceCount	= 0;

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		this.componentContext = (XmlSqlContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "sql-component", true);
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
		buildConfigNode(this.root.evalNodes("config-property"), SqlComponent.getInstance());
		// 解析dataSource
		List<DataSourceVo> dsList = buildDataSourceNode(this.root.evalNodes("dataSource"));
		// 解析dataSourceGroup
		List<DataSourceVo> dsGroupList = buildDataSourceGroupNode(this.root.evalNodes("dataSourceGroup"));
		addDataSource(dsList, dsGroupList);
		// 解析transaction
		buildTransactionNode(this.root.evalNodes("transaction"));
		// 解析默认的transaction
		buildSetDefaultTransaction(getMostOneNode(this.root, "setDefaultTransaction", lang("xml.tag.mostone", "setDefaultTransaction")));
		// 解析mapper
		buildMapperNode(getMostOneNode(this.root, "mapper", lang("xml.tag.mostone", "mapper")));
		// 解析sharding
		buildShardingNode(getMostOneNode(this.root, "sharding", lang("xml.tag.mostone", "sharding")));
		// 解析plugin
		buildPluginNode(this.root.evalNodes("plugin"));
	}

	private void buildTransactionNode(List<XmlNodeWrapper> contexts) {
		int size = contexts.size();
		if (0 == size) {
			return;
		}
		String tagName = "transaction";
		Map<String, XTransactionDefinition> transactionMap = new HashMap<String, XTransactionDefinition>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			// 此属性因该无用了
			String name = getStringFromAttr(xNode, "name");
			String _behavior = getStringFromAttr(xNode, "behavior");
			String _isolation = getStringFromAttr(xNode, "isolation");
			Integer timeout = getIntegerFromAttr(xNode, "timeout");
			Boolean readOnly = getBoolanFromAttr(xNode, "readOnly");

			if (transactionMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			Integer behavior = null;
			if (null != _behavior) {
				behavior = getBehaviorValue(_behavior);
			}
			Integer isolation = null;
			if (null != _isolation) {
				isolation = getIsolationValue(_isolation);
			}

			XTransactionDefinition transactionDefinition = new XTransactionDefinition(id, name, behavior, isolation, timeout, readOnly);
			transactionMap.put(id, transactionDefinition);

			// log.info("add transaction definition: " + id);
			log.info(lang("add.tag", tagName, id));
		}

		this.componentContext.getTransactionMatcher().setTransactionMap(transactionMap);
	}

	private void buildSetDefaultTransaction(XmlNodeWrapper xNode) {
		if (null == xNode) {
			return;
		}
		String tagName = "setDefaultTransaction";
		String type = getStringFromAttr(xNode, "type", lang("xml.tag.attribute.empty", "type", tagName, this.resource));
		List<XmlNodeWrapper> properties = xNode.evalNodes("property");
		List<String[]> ruleList = new ArrayList<String[]>();
		for (XmlNodeWrapper propertyNode : properties) {
			String name = getStringFromAttr(propertyNode, "name", lang("xml.tag.attribute.empty", "name", tagName + ".property", this.resource));
			String value = getStringFromAttr(propertyNode, "value", lang("xml.tag.attribute.empty", "value", tagName + ".property", this.resource));
			ruleList.add(new String[] { name, value });
		}
		this.componentContext.getTransactionMatcher().setTypeAndRule(type, ruleList);
		// log.info("add default transaction rule, type is " + type);
		log.info(lang("add.tag", tagName, "type[" + type + "]"));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<DataSourceVo> buildDataSourceNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		String tagName = "dataSource";

		List<DataSourceVo> dsList = new ArrayList<DataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String _type = getStringFromAttr(xNode, "type", lang("xml.tag.attribute.empty", "type", tagName, this.resource));
			boolean defaultDs = getBoolFromAttr(xNode, "isDefault", false);
			String resource = getStringFromAttr(xNode, "resource");
			String sharedUse = getStringFromAttr(xNode, "sharedUse");

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
					String name = getStringFromAttr(propertyNode, "name",
							lang("xml.tag.attribute.empty", "name", tagName + ".property", this.resource));
					String value = getStringFromAttr(propertyNode, "value",
							lang("xml.tag.attribute.empty", "value", tagName + ".property", this.resource));
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
		int size = contexts.size();
		String tagName = "dataSourceGroup";

		List<DataSourceVo> dsList = new ArrayList<DataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);

			String id = getStringFromAttr(xNode, "groupId", lang("xml.tag.attribute.empty", "groupId", tagName, this.resource));
			String _type = getStringFromAttr(xNode, "type", lang("xml.tag.attribute.empty", "type", tagName, this.resource));
			boolean defaultDs = getBoolFromAttr(xNode, "isDefault", false);
			String resource = getStringFromAttr(xNode, "resource");
			int start = getIntFromAttr(xNode, "start", 0);
			int end = getIntFromAttr(xNode, "end", lang("xml.tag.attribute.empty", "end", tagName, this.resource));

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
					String name = getStringFromAttr(propertyNode, "name",
							lang("xml.tag.attribute.empty", "name", tagName + ".property", this.resource));
					String value = getStringFromAttr(propertyNode, "value",
							lang("xml.tag.attribute.empty", "value", tagName + ".property", this.resource));
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

		DataSourceManager dataSourceManager = null;
		String _defaultDsKey = null;
		Map<String, DataSourceVo> logicDataSourceMap = new HashMap<String, DataSourceVo>();
		Map<String, AbstractDataSource> realDataSourceMap = new HashMap<String, AbstractDataSource>();

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
		SqlComponent.getInstance().setDataSourceManager(dataSourceManager);
	}

	/**
	 * 解析mapper
	 */
	private void buildMapperNode(XmlNodeWrapper xNode) throws Throwable {
		if (null == xNode) {
			Map<String, TypeHandler<?>> jdbcTypeMap = new HashMap<String, TypeHandler<?>>();
			TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
			typeHandlerRegistry.init(jdbcTypeMap);
			SqlComponent.getInstance().setTypeHandlerRegistry(typeHandlerRegistry);
			return;
		}
		String tagName = "mapper";
		String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
		XmlSqlMapperBuilder xmlMapperBuilder = new XmlSqlMapperBuilder();
		xmlMapperBuilder.parse(this.componentContext, resource);
	}

	/**
	 * 解析Sharding
	 */
	private void buildShardingNode(XmlNodeWrapper xNode) throws Throwable {
		if (null == xNode) {
			return;
		}
		String tagName = "sharding";
		String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
		XmlSqlShardingBuilder xmlShardingBuilder = new XmlSqlShardingBuilder();
		xmlShardingBuilder.parse(this.componentContext, resource);
	}

	private void buildPluginNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		String tagName = "plugin";
		List<XmlSqlPluginBuilder> pluginBuilders = new ArrayList<XmlSqlPluginBuilder>();
		// 扫描所有的plugin
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
			XmlSqlPluginBuilder builder = new XmlSqlPluginBuilder();
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
