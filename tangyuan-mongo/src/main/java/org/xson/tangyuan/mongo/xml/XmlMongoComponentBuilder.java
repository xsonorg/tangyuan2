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

	private XmlMongoContext					componentContext		= null;
	private Map<String, MongoDataSourceVo>	dataSourceVoMap			= new HashMap<String, MongoDataSourceVo>();
	private String							defaultDataSource		= null;
	private int								defaultDataSourceCount	= 0;

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
		List<MongoDataSourceVo> dsList = buildDataSourceNode(this.root.evalNodes("dataSource"));
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
		int size = contexts.size();
		String tagName = "dataSource";
		List<MongoDataSourceVo> dsList = new ArrayList<MongoDataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			boolean defaultDs = getBoolFromAttr(xNode, "isDefault", false);
			String resource = getStringFromAttr(xNode, "resource");
			// String sharedUse = getStringFromAttr(xNode, "sharedUse");

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
					String name = getStringFromAttr(propertyNode, "name",
							lang("xml.tag.attribute.empty", "name", tagName + ".property", this.resource));
					String value = getStringFromAttr(propertyNode, "value",
							lang("xml.tag.attribute.empty", "value", tagName + ".property", this.resource));
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
		String tagName = "dataSourceGroup";
		int size = contexts.size();
		List<MongoDataSourceVo> dsList = new ArrayList<MongoDataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);

			String id = getStringFromAttr(xNode, "groupId", lang("xml.tag.attribute.empty", "groupId", tagName, this.resource));
			// String _type = getStringFromAttr(xNode, "type", lang("xml.tag.attribute.empty", "type", tagName, this.resource));
			boolean defaultDs = getBoolFromAttr(xNode, "isDefault", false);
			String resource = getStringFromAttr(xNode, "resource");
			int start = getIntFromAttr(xNode, "start", 0);
			int end = getIntFromAttr(xNode, "end", lang("xml.tag.attribute.empty", "end", tagName, this.resource));

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
					String name = getStringFromAttr(propertyNode, "name",
							lang("xml.tag.attribute.empty", "name", tagName + ".property", this.resource));
					String value = getStringFromAttr(propertyNode, "value",
							lang("xml.tag.attribute.empty", "value", tagName + ".property", this.resource));
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

		MongoDataSourceManager dataSourceManager = null;
		Map<String, MongoDataSourceVo> logicDataSourceMap = new HashMap<String, MongoDataSourceVo>();
		Map<String, AbstractMongoDataSource> realDataSourceMap = new HashMap<String, AbstractMongoDataSource>();

		for (MongoDataSourceVo dsVo : allList) {
			dsVo.start(logicDataSourceMap, realDataSourceMap);
		}

		if (logicDataSourceMap.size() == 1 && realDataSourceMap.size() == 1) {
			String dataSourceId = allList.get(0).getId();
			AbstractMongoDataSource ds = realDataSourceMap.get(dataSourceId);
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
		String tagName = "mapper";
		String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
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
		String tagName = "sharding";
		String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
		XmlMongoShardingBuilder xmlShardingBuilder = new XmlMongoShardingBuilder();
		xmlShardingBuilder.parse(this.componentContext, resource);
	}

	private void buildPluginNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		String tagName = "plugin";
		List<XmlMongoPluginBuilder> pluginBuilders = new ArrayList<XmlMongoPluginBuilder>();
		// 扫描所有的plugin
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
			XmlMongoPluginBuilder builder = new XmlMongoPluginBuilder();
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

}
