package org.xson.tangyuan.ognl.xco;

import java.util.Collection;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.ognl.FieldVo;
import org.xson.tangyuan.ognl.FieldVoWrapper;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.vo.VariableItem;
import org.xson.tangyuan.ognl.vars.vo.VariableItem.VariableItemType;
import org.xson.tangyuan.ognl.vars.vo.VariableItemWraper;
import org.xson.tangyuan.util.TypeUtils;

public class OgnlXCO {

	/**
	 * bean到XCO转换(目前只支持,简单结构)
	 */
	public static XCO beanToXCO(Object bean) {
		if (null == bean) {
			return null;
		}
		XCO            xco            = new XCO();
		FieldVoWrapper fieldVoWrapper = TypeUtils.getBeanField(bean.getClass());
		List<FieldVo>  fieldList      = fieldVoWrapper.getFieldList();
		for (FieldVo model : fieldList) {
			try {
				Object result = model.getGetter().invoke(bean);
				if (null != result) {
					Ognl.setValue(xco, model.getName(), result);
				}
			} catch (Exception e) {
				throw new OgnlException("bean to xco error: " + bean.getClass(), e);
			}
		}
		return xco;
	}

	/**
	 * 从XCO对象中递归取值
	 * 
	 * @param data
	 *            data为原始数据
	 * @param varVo
	 * @return
	 */
	public static Object getValue(XCO data, VariableItemWraper varVo) {

		// 先尝试整体取值
		try {
			Object returnObj = data.getObjectValue(varVo.getOriginal());
			if (null != returnObj) {
				return returnObj;
			}
		} catch (Throwable e) {
		}

		// 这里取值为空是否要报错, 应该严格报错, 只有最后一个为空，可以忽略
		if (null != varVo.getItem()) {
			return data.getObjectValue(varVo.getItem().getName());
		}
		List<VariableItem> varUnitList = varVo.getItemList();
		int                size        = varUnitList.size();
		Object             returnObj   = data;
		for (int i = 0; i < size; i++) {
			boolean      hasNext = (i + 1) < size;
			VariableItem vUnitVo = varUnitList.get(i);
			if (returnObj instanceof XCO) {
				returnObj = getValueFromXCO(returnObj, vUnitVo, data);
			} else if (returnObj instanceof Collection) {
				returnObj = getValueFromCollection(returnObj, vUnitVo, data);
			} else if (returnObj.getClass().isArray()) {
				Class<?> clazz = returnObj.getClass();
				if (int[].class == clazz) {
					returnObj = getValueFromIntArray(returnObj, vUnitVo, data);
				} else if (long[].class == clazz) {
					returnObj = getValueFromLongArray(returnObj, vUnitVo, data);
				} else if (float[].class == clazz) {
					returnObj = getValueFromFloatArray(returnObj, vUnitVo, data);
				} else if (double[].class == clazz) {
					returnObj = getValueFromDoubleArray(returnObj, vUnitVo, data);
				} else if (byte[].class == clazz) {
					returnObj = getValueFromByteArray(returnObj, vUnitVo, data);
				} else if (short[].class == clazz) {
					returnObj = getValueFromShortArray(returnObj, vUnitVo, data);
				} else if (boolean[].class == clazz) {
					returnObj = getValueFromBooleanArray(returnObj, vUnitVo, data);
				} else if (char[].class == clazz) {
					returnObj = getValueFromCharArray(returnObj, vUnitVo, data);
				}
				// else if (char[].class == clazz) {
				// returnObj = getValueFromCharArray(returnObj, vUnitVo, data);
				// }
				else {
					returnObj = getValueFromObjectArray(returnObj, vUnitVo, data);
				}
			} else {
				throw new OgnlException("get xco value error: " + returnObj);// 类型错误
			}
			if (null == returnObj && hasNext) {
				throw new OgnlException("get xco value error: " + varVo.getOriginal());
			}
		}

		// if (null == returnObj && varVo.isHasDefault()) {
		// returnObj = varVo.getDefaultValue();
		// }

		return returnObj;
	}

	private static Object getValueFromXCO(Object target, VariableItem peVo, XCO original) {

		String key = peVo.getName();
		if (VariableItemType.VAR == peVo.getType()) {
			key = (String) original.getObjectValue(peVo.getName());
		}

		if (VariableItemType.PROPERTY == peVo.getType() || VariableItemType.VAR == peVo.getType()) {
			XCO    xco   = (XCO) target;
			Object value = xco.getObjectValue(key);
			if (null != value) {
				return value;
			}
			if ("size".equalsIgnoreCase(key) || "length".equalsIgnoreCase(key)) {
				return xco.size();
			}
			return null;
		}
		throw new OgnlException("getValueFromXCO error: " + target);
	}

	@SuppressWarnings("rawtypes")
	private static Object getValueFromCollection(Object target, VariableItem peVo, XCO original) {

		int index = peVo.getIndex();
		if (VariableItemType.VAR == peVo.getType()) {
			index = (Integer) original.getObjectValue(peVo.getName());
		}

		if (VariableItemType.INDEX == peVo.getType() || VariableItemType.VAR == peVo.getType()) {
			if (target instanceof List) {
				List list = (List) target;
				if (index < list.size()) {
					Object value = list.get(index);
					return value;
				}
			} else {
				int        i          = 0;
				Collection collection = (Collection<?>) target;
				for (Object obj : collection) {
					if (i++ == index) {
						return obj;
					}
				}
			}
		} else {
			String key = peVo.getName();
			if ("size".equalsIgnoreCase(key) || "length".equalsIgnoreCase(key)) {
				Collection<?> collection = (Collection<?>) target;
				return collection.size();
			}
		}
		return null;
	}

	private static Object getValueFromObjectArray(Object target, VariableItem peVo, XCO original) {
		Object[] array = (Object[]) target;
		if (VariableItemType.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				Object value = array[peVo.getIndex()];
				return value;
			}
		} else if (VariableItemType.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				Object value = array[index];
				return value;
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromIntArray(Object target, VariableItem peVo, XCO original) {
		int[] array = (int[]) target;
		if (VariableItemType.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableItemType.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromLongArray(Object target, VariableItem peVo, XCO original) {
		long[] array = (long[]) target;
		if (VariableItemType.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableItemType.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromBooleanArray(Object target, VariableItem peVo, XCO original) {
		boolean[] array = (boolean[]) target;
		if (VariableItemType.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableItemType.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromByteArray(Object target, VariableItem peVo, XCO original) {
		byte[] array = (byte[]) target;
		if (VariableItemType.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableItemType.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromCharArray(Object target, VariableItem peVo, XCO original) {
		char[] array = (char[]) target;
		if (VariableItemType.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableItemType.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromDoubleArray(Object target, VariableItem peVo, XCO original) {
		double[] array = (double[]) target;
		if (VariableItemType.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableItemType.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromFloatArray(Object target, VariableItem peVo, XCO original) {
		float[] array = (float[]) target;
		if (VariableItemType.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableItemType.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromShortArray(Object target, VariableItem peVo, XCO original) {
		short[] array = (short[]) target;
		if (VariableItemType.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableItemType.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

}
