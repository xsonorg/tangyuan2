package org.xson.tangyuan.ognl.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class FastJsonConverter implements IConverter {

	@Override
	public boolean isSupportType(Object object) {
		if (object instanceof JSONObject) {
			return true;
		}
		return false;
	}

	@Override
	public Object convert(Object object, Class<?> targetClass) {
		if (null == targetClass) {
			targetClass = TangYuanContainer.getInstance().getDefaultResultType();
		}
		if (XCO.class == targetClass) {
			return jsonToXCO((JSONObject) object);
		} else if (Map.class == targetClass) {
			return jsonToMap((JSONObject) object);
		} else {
			return jsonToMap((JSONObject) object);
		}
		// throw new OgnlException("不支持的参数转换: " + object);
	}

	protected Map<String, Object> jsonToMap(JSONObject json) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (String k : json.keySet()) {
			Object v = json.get(k);
			if (v instanceof JSONArray) {
				map.put(k, arrayToMap((JSONArray) v));
			} else if (v instanceof JSONObject) {
				map.put(k, jsonToMap((JSONObject) v));
			} else {
				map.put(k, v);
			}
		}
		return map;
	}

	protected List<Object> arrayToMap(JSONArray array) {
		List<Object> list = new ArrayList<Object>();
		int size = array.size();
		for (int i = 0; i < size; i++) {
			Object v = array.get(i);
			if (v instanceof JSONArray) {
				list.add(arrayToMap((JSONArray) v));
			} else if (v instanceof JSONObject) {
				list.add(jsonToMap((JSONObject) v));
			} else {
				list.add(v);
			}
		}
		return list;
	}

	protected XCO jsonToXCO(JSONObject json) {
		XCO x = new XCO();
		for (String k : json.keySet()) {
			Object v = json.get(k);
			if (v instanceof JSONArray) {
				// x.setObjectValue(k, ((JSONArray) v).toArray());// TODO
				setArrayToXCO(x, k, (JSONArray) v);
			} else if (v instanceof JSONObject) {
				x.setXCOValue(k, jsonToXCO((JSONObject) v));
			} else {
				x.setObjectValue(k, v);
			}
		}
		return x;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void setArrayToXCO(XCO xco, String key, JSONArray array) {
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
				list.add(jsonToXCO((JSONObject) item));
			} else {
				throw new TangYuanException("Unsupported conversion type: " + item.getClass().getName());
			}
		}
		xco.setObjectValue(key, list);
	}
}
