package org.xson.tangyuan.aop.service;

import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceException;

/**
 * 服务后置AOP处理器
 */
public interface ServiceAfterAopHandler {

	void execute(String serviceURI, XCO arg, XCO result, Throwable ex) throws ServiceException;

}
