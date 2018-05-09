package org.xson.tangyuan.share.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.share.ShareComponent;
import org.xson.tangyuan.share.util.PlaceholderResourceSupport;
import org.xson.tangyuan.share.util.PropertyUtils;
import org.xson.tangyuan.share.util.ResourceManager;
import org.xson.tangyuan.share.util.StringUtils;
import org.xson.tangyuan.sql.datasource.AbstractDataSource;
import org.xson.tangyuan.sql.datasource.DataSourceGroupVo;
import org.xson.tangyuan.sql.datasource.DataSourceVo;
import org.xson.tangyuan.sql.datasource.DataSourceVo.ConnPoolType;
import org.xson.tangyuan.sql.datasource.ShareJdbcContainer;

public class ShareJdbcBuilder {

	private Log							log				= LogFactory.getLog(getClass());

	private XPathParser					xPathParser		= null;
	private Map<String, DataSourceVo>	dataSourceVoMap	= new HashMap<String, DataSourceVo>();

	private String						basePath		= null;

	public void parse(String basePath, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		//		InputStream inputStream = new FileInputStream(new File(basePath, resource));
		//		InputStream in = PlaceholderResourceSupport.processInputStream(inputStream, ShareComponent.getInstance().getPlaceholderMap());
		this.basePath = basePath;
		InputStream inputStream = ResourceManager.getInputStream(basePath, resource, true);

		this.xPathParser = new XPathParser(inputStream);
		configurationElement(xPathParser.evalNode("/sql-component"));
		inputStream.close();
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		List<DataSourceVo> dsList = buildDataSourceNodes(context.evalNodes("dataSource"));// 解析dataSource
		List<DataSourceVo> dsGroupList = buildDataSourceGroupNodes(context.evalNodes("dataSourceGroup"));// 解析dataSourceGroup
		addDataSource(dsList, dsGroupList);
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

			// String jndiName = null;
			String sharedUse = null;
			boolean defaultDs = false;

			//			Map<String, String> data = new HashMap<String, String>();
			//			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			//			for (XmlNodeWrapper propertyNode : properties) {
			//				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
			//						StringUtils.trim(propertyNode.getStringAttribute("value")));
			//			}

			Map<String, String> data = null;
			String resource = StringUtils.trim(xNode.getStringAttribute("resource"));
			if (null != resource) {
				data = (Map) ResourceManager.getProperties(this.basePath, resource, true);
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

			DataSourceVo dsVo = new DataSourceVo(id, type, defaultDs, data, sharedUse, ShareComponent.getInstance().getSystemName(), resource);
			dsList.add(dsVo);
			dataSourceVoMap.put(id, dsVo);
		}
		return dsList;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<DataSourceVo> buildDataSourceGroupNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		// log.info("解析数据源:" + contexts.size());
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

			//			Map<String, String> data = new HashMap<String, String>();
			//			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			//			for (XmlNodeWrapper propertyNode : properties) {
			//				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
			//						StringUtils.trim(propertyNode.getStringAttribute("value")));
			//			}

			Map<String, String> data = null;
			String resource = StringUtils.trim(xNode.getStringAttribute("resource"));
			if (null != resource) {
				data = (Map) ResourceManager.getProperties(this.basePath, resource, true);
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

			DataSourceGroupVo dsGroupVo = new DataSourceGroupVo(id, type, defaultDs, data, ShareComponent.getInstance().getSystemName(), start, end,
					resource);
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

		Map<String, DataSourceVo> logicDataSourceMap = new HashMap<String, DataSourceVo>();
		Map<String, AbstractDataSource> realDataSourceMap = new HashMap<String, AbstractDataSource>();

		for (DataSourceVo dsVo : allList) {
			dsVo.start(logicDataSourceMap, realDataSourceMap);
		}

		ShareJdbcContainer.getInstance().setDsMap(realDataSourceMap);
		ShareJdbcContainer.getInstance().setDsVoMap(logicDataSourceMap);

		dataSourceVoMap = null;
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
		}
		return null;
	}
}
