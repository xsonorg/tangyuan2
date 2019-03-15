package org.xson.tangyuan.hive.xml;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.sql.datasource.DataSourceVo;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;

/**
 * SQL组件解析上下文
 */
public class XmlSqlContext implements XmlContext {

	private XmlGlobalContext			xmlContext		= null;

	private Map<String, MappingVo>		mappingVoMap	= new HashMap<String, MappingVo>();

	private Map<String, DataSourceVo>	dataSourceVoMap	= new HashMap<String, DataSourceVo>();

	@Override
	public void clean() {
		mappingVoMap = null;
		dataSourceVoMap = null;
		// transactionMatcher = null;
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

	public Map<String, DataSourceVo> getDataSourceVoMap() {
		return dataSourceVoMap;
	}

	public void setDataSourceVoMap(Map<String, DataSourceVo> dataSourceVoMap) {
		this.dataSourceVoMap = dataSourceVoMap;
	}

	/** 事务匹配器 */
	// private DefaultTransactionMatcher transactionMatcher = new DefaultTransactionMatcher();

	// public void setTransactionMatcher(DefaultTransactionMatcher transactionMatcher) {
	// this.transactionMatcher = transactionMatcher;
	// }
	//
	// public DefaultTransactionMatcher getTransactionMatcher() {
	// return transactionMatcher;
	// }
}
