package org.xson.tangyuan.mongo.executor.sql;

import java.lang.reflect.Method;

import org.xson.tangyuan.TangYuanException;

public class ValueVo {

	public enum ValueType {
		// INTEGER, DOUBLE, BOOLEAN, STRING, NULL
		// INTEGER, LONG, DOUBLE, BOOLEAN, STRING, NULL
		INTEGER, LONG, DOUBLE, BOOLEAN, STRING, NULL, ARRAY, CALL
	}

	private Object		value;

	private ValueType	type;

	private String		original;

	public ValueVo(Object value, ValueType type, String original) {
		this.value = value;
		this.type = type;
		this.original = original;
	}

	// public Object getValue() {
	// if (ValueType.CALL == type) {
	// Method staticMethod = (Method) value;
	// return staticMethod.invoke(null, tempArgs);
	// }
	// return value;
	// }

	public Object getValue(Object arg) {
		if (ValueType.CALL == type) {
			Method staticMethod = (Method) value;
			try {
				return staticMethod.invoke(null, arg);
			} catch (Throwable e) {
				throw new TangYuanException(e);
			}
		}
		return value;
	}

	public Object getSqlValue() {
		if (ValueType.NULL == type) {
			return "null";
		}
		if (ValueType.ARRAY == type) {
			return original;
		}
		if (ValueType.CALL == type) {
			return original;
		}
		return value;
	}

	public ValueType getType() {
		return type;
	}

}
