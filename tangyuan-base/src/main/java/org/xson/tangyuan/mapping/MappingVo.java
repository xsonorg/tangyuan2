package org.xson.tangyuan.mapping;

import java.util.Map;

public class MappingVo {

	private String                id;

	private MappingHandler        handler;

	private ColumnValueHandler    columnValueHandler;

	// key: column, value:property
	private Map<String, ColumnVo> columnMap;

	// 似乎无用
	private String                type;
	private Class<?>              beanClass;

	public MappingVo(String id, String type, Class<?> beanClass, MappingHandler handler, ColumnValueHandler columnValueHandler, Map<String, ColumnVo> columnMap) {
		this.id = id;
		this.type = type;
		this.handler = handler;
		this.columnValueHandler = columnValueHandler;
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

	public Map<String, ColumnVo> getColumnMap() {
		return columnMap;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public ColumnValueHandler getColumnValueHandler(String column) {
		ColumnValueHandler cvHandler = null;
		if (null != this.columnMap) {
			ColumnVo columnVo = columnMap.get(column);
			if (null != columnVo) {
				cvHandler = columnVo.getValueHandler();
			}
		}
		if (null == cvHandler) {
			cvHandler = this.columnValueHandler;
		}
		return cvHandler;
	}

	//	public ColumnValueHandler getColumnValueHandler(String column) {
	//		if (null == this.columnMap) {
	//			return null;
	//		}
	//		ColumnVo columnVo = columnMap.get(column);
	//		if (null == columnVo) {
	//			return null;
	//		}
	//		return columnVo.getValueHandler();
	//	}

	// 获取数据库中列映射的Bean属性名
	public String getProperty(String column) {
		ColumnVo columnVo = null;
		String   property = null;
		if (null != columnMap) {
			columnVo = columnMap.get(column);
		}
		if (null != columnVo) {
			property = columnVo.getProperty();
		}
		if (null == property && null != handler) {
			property = handler.columnToProperty(column);
		}

		if (null == property) {
			return column;
		}

		return property;
	}

	//	private Map<String, String>   columnMap;
	//	public MappingVo(String id, String type, Class<?> beanClass, MappingHandler handler, Map<String, String> columnMap) {
	//		this.id = id;
	//		this.type = type;
	//		this.handler = handler;
	//		this.columnMap = columnMap;
	//		this.beanClass = beanClass;
	//	}
	//	public Map<String, String> getColumnMap() {
	//		return columnMap;
	//	}
	//	// 获取数据库中列映射的Bean属性名
	//	public String getProperty(String column) {
	//		ColumnVo columnVo = null;
	//		String   property = null;
	//		if (null != columnMap) {
	//			property = columnMap.get(column);
	//		}
	//		if (null == property && null != handler) {
	//			property = handler.columnToProperty(column);
	//		}
	//
	//		if (null == property) {
	//			return column;
	//		}
	//
	//		return property;
	//	}

}
