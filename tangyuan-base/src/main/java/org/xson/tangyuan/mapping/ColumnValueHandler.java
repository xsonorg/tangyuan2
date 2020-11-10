package org.xson.tangyuan.mapping;

/**
 * 列值处理器
 */
public interface ColumnValueHandler {

	public Object process(String columnName, Object columnValue);

}
