package org.xson.tangyuan.mapping;

import java.util.Map;

public class MappingVo2 {

	private String				id;

	// 似乎无用
	private String				type;

	private MappingHandler		handler;

	// key: column, value:property
	private Map<String, String>	columnMap;

	private Class<?>			beanClass;

	public MappingVo2(String id, String type, Class<?> beanClass, MappingHandler handler, Map<String, String> columnMap) {
		this.id = id;
		this.type = type;
		this.handler = handler;
		this.columnMap = columnMap;
		this.beanClass = beanClass;
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public MappingHandler getHandler() {
		return handler;
	}

	public Map<String, String> getColumnMap() {
		return columnMap;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	// 获取数据库中列映射的Bean属性名
	public String getProperty(String column) {
		String property = null;
		if (null != columnMap) {
			property = columnMap.get(column);
		}
		if (null == property && null != handler) {
			property = handler.columnToProperty(column);
		}

		if (null == property) {
			return column;
		}

		return property;
	}

}
