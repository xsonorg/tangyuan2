package org.xson.tangyuan.es.util;

import java.util.ArrayList;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class FastJsonUtil {

	// public Map toMap(JSONObject json) {
	// return null;
	// }
	// protected static void setArrayToXCO(XCO xco, String key, Object array) {
	// System.out.println(array.getClass().getName());
	// }

	public static XCO toXCO(JSONObject json) {
		return toXCO(null, json);
	}

	public static XCO toXCO(XCO x, JSONObject json) {
		if (null == x) {
			x = new XCO();
		}
		for (String k : json.keySet()) {
			Object v = json.get(k);
			if (v instanceof JSONArray) {
				setArrayToXCO(x, k, (JSONArray) v);
			} else if (v instanceof JSONObject) {
				x.setXCOValue(k, toXCO((JSONObject) v));
			} else {
				x.setObjectValue(k, v);
			}
		}
		return x;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected static void setArrayToXCO(XCO xco, String key, JSONArray array) {
		if (0 == array.size()) {
			return;
		}
		Object first = array.get(0);
		Class<?> firstClass = first.getClass();
		if (Integer.class == firstClass) {
			int[] arr = new int[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = array.getIntValue(i);
			}
			xco.setIntegerArrayValue(key, arr);
			return;
		} else if (Long.class == firstClass) {
			long[] arr = new long[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = array.getLongValue(i);
			}
			xco.setLongArrayValue(key, arr);
			return;
		} else if (Float.class == firstClass) {
			float[] arr = new float[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = array.getFloatValue(i);
			}
			xco.setFloatArrayValue(key, arr);
			return;
		} else if (Double.class == firstClass) {
			double[] arr = new double[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = array.getDoubleValue(i);
			}
			xco.setDoubleArrayValue(key, arr);
			return;
		} else if (Boolean.class == firstClass) {
			boolean[] arr = new boolean[array.size()];
			for (int i = 0; i < array.size(); i++) {
				arr[i] = array.getBooleanValue(i);
			}
			xco.setBooleanArrayValue(key, arr);
			return;
		} else if (String.class == firstClass) {
			xco.setStringListValue(key, (List) array);
			return;
		}

		List<Object> list = new ArrayList<Object>();
		for (Object item : array) {
			if (item instanceof JSONObject) {
				list.add(toXCO((JSONObject) item));
			} else {
				throw new TangYuanException("Unsupported conversion type: " + item.getClass().getName());
			}
		}
		xco.setObjectValue(key, list);
	}

	public static void main(String[] args) {
		// String jsonstr = "{a:[1,2,3,4]}";
		String jsonstr = "{a:{a:['xxx','123']}}";
		JSONObject json = JSON.parseObject(jsonstr);
		XCO xco = toXCO(json);
		System.out.println(xco);
	}

}
