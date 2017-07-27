package org.xson.tangyuan.mongo.xml;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceVo;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;

public class XmlMongoContext implements XmlContext {

	private XmlGlobalContext				xmlContext			= null;

	private Map<String, MappingVo>			mappingVoMap		= new HashMap<String, MappingVo>();

	private Map<String, MongoDataSourceVo>	dataSourceVoMap		= new HashMap<String, MongoDataSourceVo>();

	private String							defaultDataSource	= null;

	@Override
	public void clean() {
		mappingVoMap = null;
		dataSourceVoMap = null;
		defaultDataSource = null;
		xmlContext = null;
	}

	public XmlGlobalContext getXmlContext() {
		return xmlContext;
	}

	public void setXmlContext(XmlGlobalContext xmlContext) {
		this.xmlContext = xmlContext;
	}

	public Map<String, MappingVo> getMappingVoMap() {
		return mappingVoMap;
	}

	public void setMappingVoMap(Map<String, MappingVo> mappingVoMap) {
		this.mappingVoMap = mappingVoMap;
	}

	public Map<String, MongoDataSourceVo> getDataSourceVoMap() {
		return dataSourceVoMap;
	}

	public void setDataSourceVoMap(Map<String, MongoDataSourceVo> dataSourceVoMap) {
		this.dataSourceVoMap = dataSourceVoMap;
	}

	public String getDefaultDataSource() {
		return defaultDataSource;
	}

	public void setDefaultDataSource(String defaultDataSource) {
		this.defaultDataSource = defaultDataSource;
	}

}
