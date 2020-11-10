package org.xson.tangyuan.ognl;

import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.ognl.map.OgnlMap;
import org.xson.tangyuan.ognl.vars.vo.VariableItemWraper;
import org.xson.tangyuan.ognl.xco.OgnlXCO;

public class Ognl {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void setValue(Object container, String key, Object value) {
		if (null == container) {
			return;
		}
		Class<?> clazz = container.getClass();
		//		if (XCO.class == container.getClass()) {
		if (XCO.class == clazz || XCO.class.isAssignableFrom(clazz)) {
			((XCO) container).setObjectValue(key, value);
		} else if (Map.class.isAssignableFrom(clazz)) {
			((Map) container).put(key, value);
		} else {
			throw new OgnlException("Ognl.setValue不支持的类型:" + clazz);
			// throw new OgnlException(TangYuanLang.get("1000101", container.getClass().getName()));
		}
	}

	@SuppressWarnings("unchecked")
	public static Object getValue(Object container, VariableItemWraper varVo) {
		if (null == container) {
			return null;
		}
		Class<?> clazz = container.getClass();
		//		if (XCO.class == container.getClass()) {
		if (XCO.class == clazz || XCO.class.isAssignableFrom(clazz)) {
			return OgnlXCO.getValue((XCO) container, varVo);
		} else if (Map.class.isAssignableFrom(clazz)) {
			return OgnlMap.getValue((Map<String, Object>) container, varVo);
		} else {
			throw new OgnlException("Ognl.getValue不支持的类型:" + clazz);
			// throw new OgnlException(TangYuanLang.get("1000102", container.getClass().getName()));
		}
	}
}
