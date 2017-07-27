package org.xson.tangyuan.mongo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.BSONObject;
import org.xson.common.object.XCO;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

public class BSONUtil {

	public static void setObjectValue(XCO xco, String key, Object value) {
		if (value instanceof org.bson.types.ObjectId) {
			xco.setStringValue(key, value.toString());
		} else if (value instanceof DBObject) {
			// BSONToXCO((DBObject) value, xco);
			xco.setXCOValue(key, BSONToXCO((DBObject) value));
		} else if (value instanceof BasicDBList) {
			BasicDBList list = (BasicDBList) value;
			int length = list.size();
			List<XCO> xcoList = new ArrayList<XCO>();
			for (int i = 0; i < length; i++) {
				xcoList.add(BSONToXCO((DBObject) list.get(i)));
			}
			xco.setXCOListValue(key, xcoList);
		} else if (value instanceof DBObject[]) {
			DBObject[] array = (DBObject[]) value;
			int length = array.length;
			XCO[] xcoArray = new XCO[length];
			for (int i = 0; i < length; i++) {
				xcoArray[i] = BSONToXCO(array[i]);
			}
			xco.setXCOArrayValue(key, xcoArray);
		} else {
			xco.setObjectValue(key, value);
		}
	}

	@SuppressWarnings("unchecked")
	public static void setObjectValue(Map<String, Object> map, String key, Object value) {
		if (value instanceof org.bson.types.ObjectId) {
			map.put(key, value.toString());
		} else if (value instanceof DBObject) {
			// BSONToMap((DBObject) value, map);
			map.put(key, BSONToMap((DBObject) value));
		} else if (value instanceof BasicDBList) {
			BasicDBList list = (BasicDBList) value;
			int length = list.size();
			List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < length; i++) {
				mapList.add(BSONToMap((DBObject) list.get(i)));
			}
			map.put(key, mapList);
		} else if (value instanceof DBObject[]) {
			DBObject[] array = (DBObject[]) value;
			int length = array.length;
			Map<String, Object>[] mapArray = new Map[length];
			for (int i = 0; i < length; i++) {
				mapArray[i] = BSONToMap(array[i]);
			}
			map.put(key, mapArray);
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
}
