package org.xson.tangyuan.service.mongo.sql;

import org.xson.tangyuan.ognl.vars.Variable;

import com.mongodb.util.JSONExt;
import com.mongodb.util.JSONExtCallback;

public class ValueVo {

	// INTEGER, DOUBLE, BOOLEAN, STRING, NULL
	// INTEGER, LONG, DOUBLE, BOOLEAN, STRING, NULL
	// INTEGER, LONG, DOUBLE, BOOLEAN, STRING, NULL, ARRAY, CALL

	public enum ValueType {
		INTEGER, LONG, DOUBLE, BOOLEAN, STRING, NULL, ARRAY, CALL, OBJECT, UNKNOWN
	}

	private Object    value;

	private ValueType type;

	private String    original;

	public ValueVo(Object value, ValueType type, String original) {
		this.value = value;
		this.type = type;
		this.original = original;
	}

	public Object getValue(Object arg) {

		//		if (ValueType.CALL == type) {
		//			Method staticMethod = (Method) value;
		//			try {
		//				return staticMethod.invoke(null, arg);
		//			} catch (Throwable e) {
		//				throw new TangYuanException(e);
		//			}
		//		}

		if (ValueType.CALL == type) {
			return ((Variable) value).getValue(arg);
		}

		// new support json object
		if (ValueType.OBJECT == type) {
			// DBObject obj = (DBObject) JSONExt.parse(original, new JSONExtCallback());
			return JSONExt.parse(original, new JSONExtCallback(arg));
		}

		// new support array 
		if (ValueType.ARRAY == type) {
			return JSONExt.parse(original, new JSONExtCallback(arg));
		}

		if (ValueType.UNKNOWN == type) {
			return JSONExt.parse(original, new JSONExtCallback(arg));
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
		if (ValueType.OBJECT == type) {
			return original;
		}
		if (ValueType.UNKNOWN == type) {
			return original;
		}
		return value;
	}

	public ValueType getType() {
		return type;
	}

	//////////////////////////////////////////////////////////////////////////////

	// public Object getValue() {
	// if (ValueType.CALL == type) {
	// Method staticMethod = (Method) value;
	// return staticMethod.invoke(null, tempArgs);
	// }
	// return value;
	// }

}
