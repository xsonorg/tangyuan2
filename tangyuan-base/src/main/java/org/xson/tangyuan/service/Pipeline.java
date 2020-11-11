package org.xson.tangyuan.service;

import java.util.ArrayList;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.util.TangYuanUtil;

/**
 * 服务管道调用: 当前服务的返回结果(realData)是下一个服务调用的参数
 * 
 * <br />
 * <b>注意: 需要确保每个服务的返回结果(realData)是XCO类型</b>
 */
public class Pipeline {

	interface ErrorHandler {
		void process(String serviceURI, XCO arg, XCO result);
	}

	private List<String>       serviceList      = null;
	private List<ErrorHandler> errorHandlerList = null;
	private Object             firstArg         = null;

	private Pipeline(String serviceURI, Object arg) {
		this(serviceURI, arg, null);
	}

	private Pipeline(String serviceURI, Object arg, ErrorHandler errorHandler) {
		this.firstArg = arg;
		this.serviceList = new ArrayList<String>();
		this.errorHandlerList = new ArrayList<ErrorHandler>();
		this.serviceList.add(serviceURI);
		this.errorHandlerList.add(errorHandler);
	}

	protected static Pipeline create(String serviceURI, Object arg) {
		return create(serviceURI, arg, null);
	}

	protected static Pipeline create(String serviceURI, Object arg, ErrorHandler errorHandler) {
		return new Pipeline(serviceURI, arg, errorHandler);
	}

	public Pipeline next(String serviceURI) {
		return next(serviceURI, null);
	}

	public Pipeline next(String serviceURI, ErrorHandler errorHandler) {
		this.serviceList.add(serviceURI);
		this.errorHandlerList.add(errorHandler);
		return this;
	}

	public XCO execute() throws TangYuanException {
		Object arg  = this.firstArg;
		XCO    res  = null;
		int    suc  = TangYuanContainer.SUCCESS_CODE;
		int    size = this.serviceList.size();
		for (int i = 0; i < size; i++) {
			if (!(arg instanceof XCO)) {
				throw new TangYuanException("无效的参数类型: " + arg.getClass().getName());
			}
			res = Actuator.execute(this.serviceList.get(i), arg);
			if (suc == res.getCode()) {
				arg = res.getData();
				continue;
			}
			ErrorHandler errorHandler = this.errorHandlerList.get(i);
			if (null == errorHandler) {
				throw new ServiceException(res.getCode(), res.getMessage());
			}
			errorHandler.process(this.serviceList.get(i), (XCO) arg, res);
			return res;
		}
		return TangYuanUtil.retObjToXco(res);
	}

}
