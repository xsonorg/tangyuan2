package org.xson.tangyuan.aop.service;

import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceException;

/**
 * 服务前置AOP处理器
 */
public interface ServiceBeforeAopHandler {

	void execute(String serviceURI, XCO arg) throws ServiceException;

}
