package org.xson.tangyuan.service;

import java.util.ArrayList;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceException;

/**
 * 服务管道调用: 当前服务的返回结果(realData)是下一个服务调用的参数
 * 
 * <br />
 * <b>注意: 需要确保每个服务的返回结果(realData)是XCO类型</b>
 */
public class Pipeline {

	//TODO 考虑吧此类迁移到Actuator中

	interface ErrorHandler {
		void process(String serviceURI, XCO arg, XCO result);
	}

	interface FailureHandler {
		void process(String serviceURI, XCO arg, XCO result);
	}

	interface PipelineFailureHandler {
		void process(String serviceURI, XCO arg, XCO result);
	}

	private List<String> serviceList = null;
	private Object       firstArg    = null;

	private Pipeline(String serviceURI, Object arg) {
		this.firstArg = arg;
		this.serviceList = new ArrayList<String>();
		this.serviceList.add(serviceURI);
	}

	public static Pipeline create(String serviceURI, Object arg) {
		return new Pipeline(serviceURI, arg);
	}

	public Pipeline next(String serviceURI) {
		this.serviceList.add(serviceURI);
		return this;
	}

	public Pipeline next(String serviceURI, ErrorHandler eh) {
		this.serviceList.add(serviceURI);
		return this;
	}

	//	public Pipeline error(ErrorHandler eh) {
	//		//		eh.process(serviceURI, arg, result);
	//		return this;
	//	}

	public XCO execute() throws TangYuanException {
		Object arg = this.firstArg;
		XCO    res = null;
		int    suc = TangYuanContainer.SUCCESS_CODE;
		for (String serviceURI : serviceList) {
			if (!(arg instanceof XCO)) {
				throw new TangYuanException("参数类型不匹配");//TODO
			}
			res = Actuator.execute(serviceURI, arg);
			if (suc != res.getCode()) {
				// throw new ServiceException(res.getCode(), res.getMessage());
				throw new ServiceException("执行异常");//TODO
			}
			arg = res.getData();
		}
		return res;
	}
}
