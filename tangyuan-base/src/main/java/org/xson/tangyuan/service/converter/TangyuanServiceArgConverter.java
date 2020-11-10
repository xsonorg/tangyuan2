package org.xson.tangyuan.service.converter;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;

/**
 * 服务入参转换器，可以将所有类型的参数转换成XCO类型
 */
public class TangyuanServiceArgConverter implements ServiceArgConverter {

	@Override
	public XCO convert(Object arg) throws Throwable {
		if (arg instanceof XCO) {
			return (XCO) arg;
		}
		throw new TangYuanException("unsupported parameter type: " + ((null != arg) ? arg.getClass() : "null"));
	}

}
