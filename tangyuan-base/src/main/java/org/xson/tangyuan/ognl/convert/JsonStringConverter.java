package org.xson.tangyuan.ognl.convert;

import com.alibaba.fastjson.JSON;

/**
 * @deprecated
 */
public class JsonStringConverter extends FastJsonConverter {

	@Override
	public boolean isSupportType(Object object) {
		if (object instanceof String) {
			String json = (String) object;
			if (json.startsWith("{") && json.endsWith("}")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object convert(Object object, Class<?> targetClass) {
		return super.convert(JSON.parseObject((String) object), targetClass);
	}

}
