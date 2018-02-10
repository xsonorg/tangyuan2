package org.xson.tangyuan.share.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.mongo.datasource.AbstractMongoDataSource;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceGroupVo;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceVo;
import org.xson.tangyuan.mongo.datasource.ShareMongoContainer;
import org.xson.tangyuan.share.ShareComponent;
import org.xson.tangyuan.share.util.PlaceholderResourceSupport;
import org.xson.tangyuan.share.util.StringUtils;

public class ShareMongoBuilder {

	private Log								log				= LogFactory.getLog(getClass());
	private XPathParser						xPathParser		= null;
	private Map<String, MongoDataSourceVo>	dataSourceVoMap	= new HashMap<String, MongoDataSourceVo>();

	public void parse(String basePath, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = new FileInputStream(new File(basePath, resource));
		InputStream in = PlaceholderResourceSupport.processInputStream(inputStream, ShareComponent.getInstance().getPlaceholderMap());
		this.xPathParser = new XPathParser(inputStream);
		in.close();
		configurationElement(xPathParser.evalNode("/mongo-component"));
	}

	private void configurationElement(XmlNodeWrapper context) {
		List<MongoDataSourceVo> dsList = buildDataSourceNodes(context.evalNodes("dataSource"));// 解析dataSource
		List<MongoDataSourceVo> dsGroupList = buildDataSourceGroupNodes(context.evalNodes("dataSourceGroup"));// 解析dataSourceGroup
		addDataSource(dsList, dsGroupList);
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
			boolean defaultDs = false;
			// String jndiName = null;
			String sharedUse = null;

			Map<String, String> data = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
						StringUtils.trim(propertyNode.getStringAttribute("value")));
			}
			MongoDataSourceVo dsVo = new MongoDataSourceVo(id, data, defaultDs, sharedUse, ShareComponent.getInstance().getSystemName());
			dsList.add(dsVo);
			dataSourceVoMap.put(id, dsVo);
		}
		return dsList;
	}

	private List<MongoDataSourceVo> buildDataSourceGroupNodes(List<XmlNodeWrapper> contexts) {
		int size = contexts.size();
		List<MongoDataSourceVo> dsList = new ArrayList<MongoDataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("groupId"));
			if (dataSourceVoMap.containsKey(id)) {
				throw new XmlParseException("Duplicate mongo data source: " + id);
			}
			String tmp = null;
			tmp = StringUtils.trim(xNode.getStringAttribute("start")); // xml v
			int start = 0;
			if (null != tmp) {
				start = Integer.parseInt(tmp);
			}
			tmp = StringUtils.trim(xNode.getStringAttribute("end")); // xml v
			int end = Integer.parseInt(tmp);
			boolean defaultDs = false;

			Map<String, String> data = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
						StringUtils.trim(propertyNode.getStringAttribute("value")));
			}
			MongoDataSourceGroupVo dsGroupVo = new MongoDataSourceGroupVo(id, defaultDs, data, ShareComponent.getInstance().getSystemName(), start,
					end);

			dsList.add(dsGroupVo);
			dataSourceVoMap.put(id, dsGroupVo);
		}
		return dsList;
	}

	private void addDataSource(List<MongoDataSourceVo> dsList, List<MongoDataSourceVo> dsGroupList) {
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

		Map<String, MongoDataSourceVo> logicDataSourceMap = new HashMap<String, MongoDataSourceVo>();
		Map<String, AbstractMongoDataSource> realDataSourceMap = new HashMap<String, AbstractMongoDataSource>();

		for (MongoDataSourceVo dsVo : allList) {
			dsVo.start(logicDataSourceMap, realDataSourceMap);
		}

		ShareMongoContainer.getInstance().setDsMap(realDataSourceMap);
		ShareMongoContainer.getInstance().setDsVoMap(logicDataSourceMap);

		dataSourceVoMap = null;
	}

}
