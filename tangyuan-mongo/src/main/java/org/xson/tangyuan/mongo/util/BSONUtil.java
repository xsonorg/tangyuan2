package org.xson.tangyuan.mongo.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bson.BSONObject;
import org.bson.types.BSONTimestamp;
import org.bson.types.Decimal128;
import org.xson.common.object.XCO;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

public class BSONUtil {

	public static void setObjectValue(XCO xco, String key, Object value) {
		if (value instanceof org.bson.types.ObjectId) {
			xco.setStringValue(key, value.toString());
		} else if (value instanceof BasicDBList) {
			setArrayToXCO(xco, key, (BasicDBList) value);
		} else if (value instanceof DBObject[]) {
			DBObject[] array = (DBObject[]) value;
			int length = array.length;
			XCO[] xcoArray = new XCO[length];
			for (int i = 0; i < length; i++) {
				xcoArray[i] = BSONToXCO(array[i]);
			}
			xco.setXCOArrayValue(key, xcoArray);
		} else if (value instanceof DBObject) {
			xco.setXCOValue(key, BSONToXCO((DBObject) value));
		} else if (value instanceof BSONTimestamp) {
			xco.setTimestampValue(key, new java.sql.Timestamp(((BSONTimestamp) value).getTime() * 1000L));
		} else if (value instanceof Decimal128) {
			xco.setBigDecimalValue(key, ((Decimal128) value).bigDecimalValue());
		} else if (value instanceof java.util.regex.Pattern) {
			xco.setStringValue(key, ((java.util.regex.Pattern) value).toString());
		} else if (value instanceof org.bson.types.Code) {
			xco.setStringValue(key, ((org.bson.types.Code) value).getCode());
		} else if (value instanceof org.bson.types.Binary) {
			xco.setByteArrayValue(key, ((org.bson.types.Binary) value).getData());
		} else {
			try {
				xco.setObjectValue(key, value);
			} catch (Throwable e) {
				xco.setStringValue(key, value.toString());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void setObjectValue(Map<String, Object> map, String key, Object value) {
		if (value instanceof org.bson.types.ObjectId) {
			map.put(key, value.toString());
		} else if (value instanceof BasicDBList) {
			// BasicDBList list = (BasicDBList) value;
			// int length = list.size();
			// List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
			// for (int i = 0; i < length; i++) {
			// mapList.add(BSONToMap((DBObject) list.get(i)));
			// }
			// map.put(key, mapList);
			setArrayToMap(map, key, (BasicDBList) value);
		} else if (value instanceof DBObject[]) {
			DBObject[] array = (DBObject[]) value;
			int length = array.length;
			Map<String, Object>[] mapArray = new Map[length];
			for (int i = 0; i < length; i++) {
				mapArray[i] = BSONToMap(array[i]);
			}
			map.put(key, mapArray);
		} else if (value instanceof DBObject) {
			map.put(key, BSONToMap((DBObject) value));
		} else if (value instanceof BSONTimestamp) {
			map.put(key, new java.sql.Timestamp(((BSONTimestamp) value).getTime() * 1000L));
		} else if (value instanceof Decimal128) {
			map.put(key, ((Decimal128) value).bigDecimalValue());
		} else if (value instanceof java.util.regex.Pattern) {
			map.put(key, ((java.util.regex.Pattern) value).toString());
		} else if (value instanceof org.bson.types.Code) {
			map.put(key, ((org.bson.types.Code) value).getCode());
		} else if (value instanceof org.bson.types.Binary) {
			map.put(key, ((org.bson.types.Binary) value).getData());
		} else {
			map.put(key, value);
		}
	}

	public static void BSONToXCO(BSONObject bson, XCO xco) {
		Set<String> keys = bson.keySet();
		for (String key : keys) {
			setObjectValue(xco, key, bson.get(key));
		}
	}

	public static XCO BSONToXCO(BSONObject bson) {
		XCO xco = new XCO();
		BSONToXCO(bson, xco);
		return xco;
	}

	public static void BSONToMap(BSONObject bson, Map<String, Object> map) {
		Set<String> keys = bson.keySet();
		for (String key : keys) {
			setObjectValue(map, key, bson.get(key));
		}
	}

	public static Map<String, Object> BSONToMap(BSONObject bson) {
		Map<String, Object> map = new HashMap<String, Object>();
		BSONToMap(bson, map);
		return map;
	}

	protected static void setArrayToMap(Map<String, Object> map, String key, BasicDBList array) {
		if (0 == array.size()) {
			return;
		}
		Object first = array.get(0);
		Class<?> firstClass = first.getClass();
		if (Integer.class == firstClass) {
			int[] arr = new int[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = (Integer) array.get(i);
			}
			map.put(key, arr);
			return;
		} else if (Long.class == firstClass) {
			long[] arr = new long[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = (Long) array.get(i);
			}
			map.put(key, arr);
			return;
		} else if (Float.class == firstClass) {
			float[] arr = new float[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = (Float) array.get(i);
			}
			map.put(key, arr);
			return;
		} else if (Double.class == firstClass) {
			double[] arr = new double[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = (Double) array.get(i);
			}
			map.put(key, arr);
			return;
		} else if (Boolean.class == firstClass) {
			boolean[] arr = new boolean[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = (Boolean) array.get(i);
			}
			map.put(key, arr);
			return;
		} else if (String.class == firstClass) {
			String[] arr = new String[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = (String) array.get(i);
			}
			map.put(key, arr);
			return;
		} else if (first instanceof DBObject) {
			XCO[] arr = new XCO[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = BSONToXCO((DBObject) array.get(i));
			}
			map.put(key, arr);
		} else {
			String[] arr = new String[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = array.get(i).toString();
			}
			map.put(key, arr);
		}

	}

	protected static void setArrayToXCO(XCO xco, String key, BasicDBList array) {
		if (0 == array.size()) {
			return;
		}
		Object first = array.get(0);
		Class<?> firstClass = first.getClass();
		if (Integer.class == firstClass) {
			int[] arr = new int[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = (Integer) array.get(i);
			}
			xco.setIntegerArrayValue(key, arr);
			return;
		} else if (Long.class == firstClass) {
			long[] arr = new long[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = (Long) array.get(i);
			}
			xco.setLongArrayValue(key, arr);
			return;
		} else if (Float.class == firstClass) {
			float[] arr = new float[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = (Float) array.get(i);
			}
			xco.setFloatArrayValue(key, arr);
			return;
		} else if (Double.class == firstClass) {
			double[] arr = new double[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = (Double) array.get(i);
			}
			xco.setDoubleArrayValue(key, arr);
			return;
		} else if (Boolean.class == firstClass) {
			boolean[] arr = new boolean[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = (Boolean) array.get(i);
			}
			xco.setBooleanArrayValue(key, arr);
			return;
		} else if (String.class == firstClass) {
			// xco.setStringListValue(key, (List) array);
			String[] arr = new String[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = (String) array.get(i);
			}
			xco.setStringArrayValue(key, arr);
			return;
		} else if (first instanceof DBObject) {
			// List<XCO> list = new ArrayList<XCO>();
			// for (Object item : array) {
			// DBObject obj = (DBObject) item;
			// list.add(BSONToXCO(obj));
			// }
			// xco.setXCOListValue(key, list);

			XCO[] arr = new XCO[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = BSONToXCO((DBObject) array.get(i));
			}
			xco.setXCOArrayValue(key, arr);
		} else {
			// List<String> list = new ArrayList<String>();
			// for (Object item : array) {
			// list.add(item.toString());
			// }
			// xco.setStringListValue(key, list);

			String[] arr = new String[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = array.get(i).toString();
			}
			xco.setStringArrayValue(key, arr);
		}

		// throw new TangYuanException("Unsupported conversion type: " + item.getClass().getName());
	}

}
