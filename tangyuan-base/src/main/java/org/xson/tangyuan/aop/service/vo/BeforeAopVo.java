package org.xson.tangyuan.aop.service.vo;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.aop.service.ServiceBeforeAopHandler;
import org.xson.tangyuan.service.Actuator;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public class BeforeAopVo extends AopVo {

	public BeforeAopVo(String exec, ServiceBeforeAopHandler beforeHandler, int order, CallMode mode, List<String> includeList, List<String> excludeList) {
		this.exec = exec;
		this.beforeHandler = beforeHandler;
		this.order = order;
		this.mode = mode;
		this.includeList = includeList;
		this.excludeList = excludeList;
	}

	private Object assembleAopArg(String service, Object arg) {
		XCO pkgArg = new XCO();
		pkgArg.setStringValue(ServiceAopVo.aopServiceKey, service);
		pkgArg.setXCOValue(ServiceAopVo.aopArgKey, (XCO) arg);
		return pkgArg;
	}

	@Override
	protected void execBefore(String serviceURI, XCO arg) {
		if (null != this.beforeHandler) {
			// 可以改变arg
			if (CallMode.SYNC == mode) {
				this.beforeHandler.execute(serviceURI, arg);
			} else {
				TangYuanContainer.getInstance().getThreadPool().execute(new Runnable() {
					@Override
					public void run() {
						beforeHandler.execute(serviceURI, arg);
					}
				});
			}
		} else {
			Object pkgArg = assembleAopArg(serviceURI, arg);
			if (CallMode.SYNC == mode) {
				Actuator.execute(exec, pkgArg);
			} else {
				Actuator.executeAsync(exec, pkgArg);
			}
		}
	}

	/////////////////////////////////////////////////////////////////

	//	public BeforeAopVo(String exec, int order, CallMode mode, List<String> includeList, List<String> excludeList) {
	//		this.exec = exec;
	//		this.order = order;
	//		this.mode = mode;
	//		this.includeList = includeList;
	//		this.excludeList = excludeList;
	//	}
	//	@Override
	//	protected void execBefore(Object pkgArg) {
	//		if (CallMode.SYNC == mode) {
	//			actuator.execute(exec, pkgArg);
	//		} else if (CallMode.SYNC == mode) {
	//			actuator.execute(exec, pkgArg);
	//		} else if (CallMode.ASYNC == mode) {
	//			actuator.executeAsync(exec, pkgArg);
	//		}
	//	}
}
