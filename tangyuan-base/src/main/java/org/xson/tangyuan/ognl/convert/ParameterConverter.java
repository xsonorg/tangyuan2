package org.xson.tangyuan.ognl.convert;

import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.map.OgnlMap;
import org.xson.tangyuan.ognl.xco.OgnlXCO;
import org.xson.tangyuan.util.TypeUtils;

public class ParameterConverter {

	private IConverter	fastJsonConverter	= new FastJsonConverter();
	private IConverter	jsonStringConverter	= new JsonStringConverter();
	private IConverter	xmlStringConverter	= new XmlStringConverter();

	/**
	 * SQL Service 服务参数转换
	 */
	public Object parameterConvert(Object object, Class<?> resultType) {
		if (null == object) {
			return null;
		}
		if (object instanceof XCO) {
			return object;
		}
		if (object instanceof Map) {
			return object;
		}
		if (object instanceof String) {
			if (xmlStringConverter.isSupportType(object)) {
				return xmlStringConverter.convert(object, resultType);
			}
			if (jsonStringConverter.isSupportType(object)) {
				return jsonStringConverter.convert(object, resultType);
			}
			throw new OgnlException("不支持的参数转换: " + object.toString());
		}
		if (fastJsonConverter.isSupportType(object)) {
			return fastJsonConverter.convert(object, resultType);
		}
		if (TypeUtils.isBeanType(object)) {
			if (null == resultType) {
				resultType = TangYuanContainer.getInstance().getDefaultResultType();
			}
			if (XCO.class == resultType) {
				return OgnlXCO.beanToXCO(object);
			} else if (Map.class == resultType) {
				return OgnlMap.beanToMap(object);
			} else {
				return OgnlMap.beanToMap(object);
			}
		}

		throw new OgnlException("不支持的参数转换: " + object);
	}
}
