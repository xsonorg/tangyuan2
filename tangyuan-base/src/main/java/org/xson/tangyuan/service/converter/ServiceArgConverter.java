package org.xson.tangyuan.service.converter;

import org.xson.common.object.XCO;

/**
 * 服务入参转换器，可以将所有类型的参数转换成XCO类型
 */
public interface ServiceArgConverter {

	XCO convert(Object arg) throws Throwable;

}
