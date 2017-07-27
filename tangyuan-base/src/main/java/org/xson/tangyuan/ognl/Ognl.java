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
		if (XCO.class == container.getClass()) {
			((XCO) container).setObjectValue(key, value);
		} else if (Map.class.isAssignableFrom(container.getClass())) {
			((Map) container).put(key, value);
		} else {
			throw new OgnlException("Ognl.setValue不支持的类型:" + container.getClass());
		}
	}

	@SuppressWarnings("unchecked")
	public static Object getValue(Object container, VariableItemWraper varVo) {
		if (null == container) {
			return null;
		}
		if (XCO.class == container.getClass()) {
			return OgnlXCO.getValue((XCO) container, varVo);
		} else if (Map.class.isAssignableFrom(container.getClass())) {
			return OgnlMap.getValue((Map<String, Object>) container, varVo);
		} else {
			throw new OgnlException("Ognl.getValue不支持的类型:" + container.getClass());
		}
	}
}
