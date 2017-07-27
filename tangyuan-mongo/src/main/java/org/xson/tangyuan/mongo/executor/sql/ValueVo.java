package org.xson.tangyuan.mongo.executor.sql;

public class ValueVo {

	public enum ValueType {
		INTEGER, DOUBLE, BOOLEAN, STRING, NULL
	}

	private Object		value;

	private ValueType	type;

	public ValueVo(Object value, ValueType type) {
		this.value = value;
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public Object getSqlValue() {
		if (ValueType.NULL == type) {
			return "null";
		}
		return value;
	}

	public ValueType getType() {
		return type;
	}

}
