package org.xson.tangyuan.aop.service.vo;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.aop.service.ServiceAfterAopHandler;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.service.Actuator;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public class AfterAopVo extends AopVo {

	protected AopCondition condition;

	public AfterAopVo(String exec, ServiceAfterAopHandler afterHandler, int order, CallMode mode, AopCondition condition, List<String> includeList, List<String> excludeList) {
		this.exec = exec;
		this.afterHandler = afterHandler;

		this.order = order;
		this.mode = mode;
		this.condition = condition;
		this.includeList = includeList;
		this.excludeList = excludeList;
	}

	/** 组装AOP参数:after */
	private Object assembleAopArg(String service, Object arg, Object result, Throwable ex) {
		XCO pkgArg = new XCO();

		pkgArg.setStringValue(ServiceAopVo.aopServiceKey, service);
		pkgArg.setXCOValue(ServiceAopVo.aopArgKey, (XCO) arg);
		// result处理
		if (null == result) {
			// 理论上不会进入此处
			XCO     resultXCO = new XCO();
			Integer code      = null;
			String  message   = null;
			if (null != ex) {
				if (ex instanceof ServiceException) {
					ServiceException e = (ServiceException) ex;
					code = e.getErrorCode();
					message = e.getErrorMessage();
				} else {
					code = TangYuanContainer.getInstance().getErrorCode();
					message = ex.getMessage();
				}
			} else {
				// 此处是不合理的情况
				code = TangYuanContainer.getInstance().getErrorCode();
				message = TangYuanContainer.getInstance().getErrorMessage();
			}
			resultXCO.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, code);
			resultXCO.setStringValue(TangYuanContainer.XCO_MESSAGE_KEY, message);
			result = resultXCO;
		}
		// result中需要包含code和message
		pkgArg.setObjectValue(ServiceAopVo.aopResultKey, result);

		return pkgArg;
	}

	@Override
	protected void execAfter(String serviceURI, XCO arg, XCO result, Throwable ex) {
		if (AopCondition.SUCCESS == condition && null != ex) {
			return;
		}
		if (AopCondition.EXCEPTION == condition && null == ex) {
			return;
		}

		if (null != this.afterHandler) {
			// 可以改变arg
			if (CallMode.SYNC == mode) {
				this.afterHandler.execute(serviceURI, arg, result, ex);
			} else {
				TangYuanContainer.getInstance().getThreadPool().execute(new Runnable() {
					@Override
					public void run() {
						afterHandler.execute(serviceURI, arg, result, ex);
					}
				});
			}
		} else {
			Object pkgArg = assembleAopArg(serviceURI, arg, result, ex);
			if (CallMode.SYNC == mode) {
				Actuator.execute(exec, pkgArg);
			} else {
				Actuator.executeAsync(exec, pkgArg);
			}
		}
	}

	//	/** 组装AOP参数:after */
	//	private Object assembleAopArg(String service, Object arg, Object result, Throwable ex) {
	//		XCO     pkgArg  = new XCO();
	//		Integer code    = null;
	//		String  message = null;
	//
	//		pkgArg.setStringValue(ServiceAopVo.aopServiceKey, service);
	//		pkgArg.setXCOValue(ServiceAopVo.aopArgKey, (XCO) arg);
	//		if (null != result) {
	//			pkgArg.setObjectValue(ServiceAopVo.aopResultKey, result);
	//			if (result instanceof XCO) {
	//				XCO resultXCO = (XCO) result;
	//				code = resultXCO.getCode();
	//				message = resultXCO.getMessage();
	//			}
	//		}
	//		if (null != ex) {
	//			if (ex instanceof ServiceException) {
	//				ServiceException e = (ServiceException) ex;
	//				code = e.getErrorCode();
	//				message = e.getErrorMessage();
	//			} else {
	//				code = TangYuanContainer.getInstance().getErrorCode();
	//				message = ex.getMessage();
	//			}
	//		}
	//
	//		if (null == code) {
	//			code = TangYuanContainer.SUCCESS_CODE;
	//		}
	//		pkgArg.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, code);
	//		if (null != message) {
	//			pkgArg.setStringValue(TangYuanContainer.XCO_MESSAGE_KEY, message);
	//		}
	//		return pkgArg;
	//	}

	//	@Override
	//	protected void execAfter(ActuatorContext parent, Object pkgArg, Throwable ex) {
	//		if (AopCondition.SUCCESS == condition && null != ex) {
	//			return;
	//		}
	//		if (AopCondition.EXCEPTION == condition && null == ex) {
	//			return;
	//		}
	//		if (CallMode.SYNC == mode) {
	//			actuator.execute(exec, pkgArg);
	//		} else if (CallMode.SYNC == mode) {
	//			// ServiceActuator.executeAlone(exec, pkgArg, parent, TrackingManager.EXECUTE_MODE_SYNC);
	//			actuator.execute(exec, pkgArg);
	//		} else if (CallMode.ASYNC == mode) {
	//			// ServiceActuator.executeAsync(exec, pkgArg, parent);
	//			actuator.executeAsync(exec, pkgArg);
	//		}
	//	}

	//	public AfterAopVo(String exec, int order, CallMode mode, AopCondition condition, List<String> includeList, List<String> excludeList) {
	//		this.exec = exec;
	//		this.order = order;
	//		this.mode = mode;
	//		this.condition = condition;
	//		this.includeList = includeList;
	//		this.excludeList = excludeList;
	//	}

}
