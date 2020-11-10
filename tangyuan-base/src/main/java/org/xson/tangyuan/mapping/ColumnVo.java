package org.xson.tangyuan.mapping;

public class ColumnVo {

	private String             property;

	private ColumnValueHandler valueHandler;

	public ColumnVo(String property, ColumnValueHandler valueHandler) {
		this.property = property;
		this.valueHandler = valueHandler;
	}

	public String getProperty() {
		return property;
	}

	public ColumnValueHandler getValueHandler() {
		return valueHandler;
	}
}
