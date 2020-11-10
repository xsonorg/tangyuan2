package org.xson.tangyuan.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xson.common.object.XCO;
import org.xson.common.object.XCOUtil;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.web.XCOWebException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JSONFieldConverterSupport {

	private static JSONFieldConverterSupport instance = new JSONFieldConverterSupport();

	public static JSONFieldConverterSupport getInstance() {
		return instance;
	}

	interface FieldConverter {
		void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable;
	}

	// base

	class ByteFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setByteValue(fieldName, Byte.parseByte(value.toString()));
		}
	}

	class BooleanFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setBooleanValue(fieldName, XCOUtil.castToBoolean(value));
		}
	}

	class CharFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setCharValue(fieldName, value.toString().charAt(0));
		}
	}

	class ShortFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setShortValue(fieldName, XCOUtil.castToShort(value));
		}
	}

	class IntFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setIntegerValue(fieldName, XCOUtil.castToInt(value));
		}
	}

	class LongFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setLongValue(fieldName, XCOUtil.castToLong(value));
		}
	}

	class FloatFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setFloatValue(fieldName, XCOUtil.castToFloat(value));
		}
	}

	class DoubleFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setDoubleValue(fieldName, XCOUtil.castToDouble(value));
		}
	}

	class DateTimeFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setDateTimeValue(fieldName, XCOUtil.castToDateTime(value));
		}
	}

	class SqlTimeFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setTimeValue(fieldName, XCOUtil.castToSqlTime(value));
		}
	}

	class SqlDateFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setDateValue(fieldName, XCOUtil.castToSqlDate(value));
		}
	}

	class TimestampFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setTimestampValue(fieldName, XCOUtil.castToSqlTimestamp(value));
		}
	}

	class BigIntegerFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setBigIntegerValue(fieldName, XCOUtil.castToBigInteger(value));
		}
	}

	class BigDecimalFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setBigDecimalValue(fieldName, XCOUtil.castToBigDecimal(value));
		}
	}

	// String

	class StringFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			xco.setStringValue(fieldName, value.toString());
		}
	}

	class StringArrayFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			JSONArray data  = (JSONArray) value;
			String[]  array = data.toArray(new String[data.size()]);
			xco.setStringArrayValue(fieldName, array);
		}
	}

	class StringListFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			JSONArray data = (JSONArray) value;
			if (CollectionUtils.isEmpty(data)) {
				return;
			}
			List<String> list = new ArrayList<String>();
			for (Object o : data) {
				list.add(o.toString());
			}
			xco.setStringListValue(fieldName, list);
		}
	}

	class StringSetFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			JSONArray data = (JSONArray) value;
			if (CollectionUtils.isEmpty(data)) {
				return;
			}
			Set<String> set = new HashSet<String>();
			for (Object o : data) {
				set.add(o.toString());
			}
			xco.setStringSetValue(fieldName, set);
		}
	}

	// XCO

	class XCOFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {

			List<RuleGroupItem> childItemList = item.getItems();
			if (CollectionUtils.isEmpty(childItemList)) {
				return;
			}

			XCO childXco = new XCO();
			xco.setXCOValue(fieldName, childXco);

			JSONObject data = (JSONObject) value;
			for (RuleGroupItem childItem : childItemList) {
				String childFieldName = childItem.getFieldName();
				Object childValue     = data.get(childFieldName);
				if (null == childValue) {
					continue;
				}
				//converterJSONField(childItem, childXco, childFieldName, childItem.getType(), childValue);
				converterJSONField(childItem, childXco, childValue);
			}
		}
	}

	class XCOArrayFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {

			List<RuleGroupItem> childItemList = item.getItems();
			if (CollectionUtils.isEmpty(childItemList)) {
				return;
			}

			JSONArray data = (JSONArray) value;
			if (CollectionUtils.isEmpty(data)) {
				return;
			}

			XCO[] array = new XCO[data.size()];
			xco.setXCOArrayValue(fieldName, array);

			for (int i = 0; i < data.size(); i++) {
				Object childValue = data.get(i);
				XCO    childXco   = new XCO();
				array[i] = childXco;
				for (RuleGroupItem childItem : childItemList) {
					// String childFieldName = childItem.getFieldName();
					// converterJSONField(childItem, childXco, childFieldName, childItem.getType(), childValue);
					converterJSONField(childItem, childXco, childValue);
				}
			}
		}
	}

	class XCOListFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {

			List<RuleGroupItem> childItemList = item.getItems();
			if (CollectionUtils.isEmpty(childItemList)) {
				return;
			}

			JSONArray data = (JSONArray) value;
			if (CollectionUtils.isEmpty(data)) {
				return;
			}

			List<XCO> list = new ArrayList<XCO>();
			xco.setXCOListValue(fieldName, list);

			for (int i = 0; i < data.size(); i++) {
				Object childValue = data.get(i);
				XCO    childXco   = new XCO();
				list.add(childXco);
				for (RuleGroupItem childItem : childItemList) {
					//					String childFieldName = childItem.getFieldName();
					//					converterJSONField(childItem, childXco, childFieldName, childItem.getType(), childValue);
					converterJSONField(childItem, childXco, childValue);
				}
			}
		}
	}

	class XCOSetFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {

			List<RuleGroupItem> childItemList = item.getItems();
			if (CollectionUtils.isEmpty(childItemList)) {
				return;
			}

			JSONArray data = (JSONArray) value;
			if (CollectionUtils.isEmpty(data)) {
				return;
			}

			Set<XCO> set = new HashSet<XCO>();
			xco.setXCOSetValue(fieldName, set);

			for (int i = 0; i < data.size(); i++) {
				Object childValue = data.get(i);
				XCO    childXco   = new XCO();
				set.add(childXco);
				for (RuleGroupItem childItem : childItemList) {
					//					String childFieldName = childItem.getFieldName();
					//					converterJSONField(childItem, childXco, childFieldName, childItem.getType(), childValue);
					converterJSONField(childItem, childXco, childValue);
				}
			}
		}
	}

	// array

	class IntArrayFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			JSONArray data = (JSONArray) value;
			if (CollectionUtils.isEmpty(data)) {
				return;
			}

			int[] array = new int[data.size()];
			xco.setIntegerArrayValue(fieldName, array);

			for (int i = 0; i < data.size(); i++) {
				array[i] = XCOUtil.castToInt(data.get(i));
			}
		}
	}

	class LongArrayFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			JSONArray data = (JSONArray) value;
			if (CollectionUtils.isEmpty(data)) {
				return;
			}

			long[] array = new long[data.size()];
			xco.setLongArrayValue(fieldName, array);

			for (int i = 0; i < data.size(); i++) {
				array[i] = XCOUtil.castToLong(data.get(i));
			}
		}
	}

	class FloatArrayFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			JSONArray data = (JSONArray) value;
			if (CollectionUtils.isEmpty(data)) {
				return;
			}

			float[] array = new float[data.size()];
			xco.setFloatArrayValue(fieldName, array);

			for (int i = 0; i < data.size(); i++) {
				array[i] = XCOUtil.castToFloat(data.get(i));
			}
		}
	}

	class DoubleArrayFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			JSONArray data = (JSONArray) value;
			if (CollectionUtils.isEmpty(data)) {
				return;
			}

			double[] array = new double[data.size()];
			xco.setDoubleArrayValue(fieldName, array);

			for (int i = 0; i < data.size(); i++) {
				array[i] = XCOUtil.castToDouble(data.get(i));
			}
		}
	}

	class ByteArrayFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			JSONArray data = (JSONArray) value;
			if (CollectionUtils.isEmpty(data)) {
				return;
			}

			byte[] array = new byte[data.size()];
			xco.setByteArrayValue(fieldName, array);

			for (int i = 0; i < data.size(); i++) {
				array[i] = Byte.parseByte(value.toString());
			}
		}
	}

	class BooleanArrayFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			JSONArray data = (JSONArray) value;
			if (CollectionUtils.isEmpty(data)) {
				return;
			}

			boolean[] array = new boolean[data.size()];
			xco.setBooleanArrayValue(fieldName, array);

			for (int i = 0; i < data.size(); i++) {
				array[i] = XCOUtil.castToBoolean(data.get(i));
			}
		}
	}

	class ShortArrayFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			JSONArray data = (JSONArray) value;
			if (CollectionUtils.isEmpty(data)) {
				return;
			}

			short[] array = new short[data.size()];
			xco.setShortArrayValue(fieldName, array);

			for (int i = 0; i < data.size(); i++) {
				array[i] = XCOUtil.castToShort(data.get(i));
			}
		}
	}

	class CharArrayFieldConverter implements FieldConverter {
		@Override
		public void convert(RuleGroupItem item, XCO xco, String fieldName, Object value) throws Throwable {
			JSONArray data = (JSONArray) value;
			if (CollectionUtils.isEmpty(data)) {
				return;
			}

			char[] array = new char[data.size()];
			xco.setCharArrayValue(fieldName, array);

			for (int i = 0; i < data.size(); i++) {
				array[i] = data.get(i).toString().charAt(i);
			}
		}
	}

	// end

	private Map<TypeEnum, FieldConverter> converterMap = new HashMap<TypeEnum, FieldConverter>();

	private JSONFieldConverterSupport() {

		converterMap.put(TypeEnum.INTEGER, new IntFieldConverter());
		converterMap.put(TypeEnum.LONG, new LongFieldConverter());
		converterMap.put(TypeEnum.FLOAT, new FloatFieldConverter());
		converterMap.put(TypeEnum.DOUBLE, new DoubleFieldConverter());
		converterMap.put(TypeEnum.BYTE, new ByteFieldConverter());
		converterMap.put(TypeEnum.BOOLEAN, new BooleanFieldConverter());
		converterMap.put(TypeEnum.SHORT, new ShortFieldConverter());
		converterMap.put(TypeEnum.CHAR, new CharFieldConverter());

		converterMap.put(TypeEnum.DATE, new SqlDateFieldConverter());
		converterMap.put(TypeEnum.TIME, new SqlTimeFieldConverter());
		converterMap.put(TypeEnum.TIMESTAMP, new TimestampFieldConverter());
		converterMap.put(TypeEnum.DATETIME, new DateTimeFieldConverter());

		converterMap.put(TypeEnum.BIGINTEGER, new BigIntegerFieldConverter());
		converterMap.put(TypeEnum.BIGDECIMAL, new BigDecimalFieldConverter());

		converterMap.put(TypeEnum.INT_ARRAY, new IntArrayFieldConverter());
		converterMap.put(TypeEnum.LONG_ARRAY, new LongArrayFieldConverter());
		converterMap.put(TypeEnum.FLOAT_ARRAY, new FloatArrayFieldConverter());
		converterMap.put(TypeEnum.DOUBLE_ARRAY, new DoubleArrayFieldConverter());
		converterMap.put(TypeEnum.BYTE_ARRAY, new ByteArrayFieldConverter());
		converterMap.put(TypeEnum.BOOLEAN_ARRAY, new BooleanArrayFieldConverter());
		converterMap.put(TypeEnum.SHORT_ARRAY, new ShortArrayFieldConverter());
		converterMap.put(TypeEnum.CHAR_ARRAY, new CharArrayFieldConverter());

		converterMap.put(TypeEnum.STRING, new StringFieldConverter());
		converterMap.put(TypeEnum.STRING_ARRAY, new StringArrayFieldConverter());
		converterMap.put(TypeEnum.STRING_LIST, new StringListFieldConverter());
		converterMap.put(TypeEnum.STRING_SET, new StringSetFieldConverter());

		converterMap.put(TypeEnum.XCO, new XCOFieldConverter());
		converterMap.put(TypeEnum.XCO_ARRAY, new XCOArrayFieldConverter());
		converterMap.put(TypeEnum.XCO_LIST, new XCOListFieldConverter());
		converterMap.put(TypeEnum.XCO_SET, new XCOSetFieldConverter());
	}

	public void converterJSONField(RuleGroupItem item, XCO xco, Object value) throws Throwable {
		String         fieldName = item.getFieldName();
		TypeEnum       type      = item.getType();
		FieldConverter c         = converterMap.get(type);
		if (null == c) {
			throw new XCOWebException(TangYuanLang.get("web.converter.field.json.unsupported", type.toString()));
		}
		c.convert(item, xco, fieldName, value);
	}

	//////////////////////////////////////////////////////////////////////

	//		ARRAY("array"),
	//		COLLECTION("collection"),
	//

	//	public void converterJSONField0(RuleGroupItem item, XCO xco, String fieldName, TypeEnum type, Object value) throws Throwable {
	//		// value != null
	//		FieldConverter c = converterMap.get(type);
	//		c.convert(item, xco, fieldName, value);
	//	}

	//			Class<?> targetClass = value.getClass();
	//			if (Integer.class == targetClass) {
	//				xco.setIntegerValue(fieldName, (Integer) value);
	//			} else {
	//				xco.setIntegerValue(fieldName, Integer.parseInt(value.toString()));
	//			}

}
