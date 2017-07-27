package org.xson.tangyuan.aop.vo;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.aop.AspectVo;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public class BeforeAloneVo extends AspectVo {

	protected static Log	log	= LogFactory.getLog(BeforeAloneVo.class);

	protected CallMode		mode;

	/** 是否会影响其他切面 */
	protected boolean		propagation;

	public BeforeAloneVo(String exec, int order, List<String> includeList, List<String> excludeList, CallMode mode, boolean propagation) {
		this.exec = exec;
		this.order = order;
		this.includeList = includeList;
		this.excludeList = excludeList;
		this.mode = mode;
		this.propagation = propagation;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	// @Override
	// protected void execBefore(String service, Object pkgArg) {
	// if (CallMode.ALONE == mode) {
	// try {
	// ServiceActuator.executeAlone(exec, pkgArg);
	// } catch (ServiceException e) {
	// if (propagation) {
	// throw e;
	// } else {
	// getLog().error("An exception occurred before the service: " + service, e);
	// }
	// }
	// } else if (CallMode.ASYNC == mode) {
	// ServiceActuator.executeAsync(exec, pkgArg);
	// }
	// }

	@Override
	protected void execBefore(String service, Object pkgArg) {
		if (CallMode.ALONE == mode) {
			try {
				Object obj = ServiceActuator.executeAlone(exec, pkgArg);
				if (propagation && null != obj && obj instanceof XCO) {
					XCO xco = (XCO) obj;
					Integer code = xco.getCode();
					if (null != code && 0 != code.intValue()) {
						throw new ServiceException(code.intValue(), xco.getMessage());
					}
				}
			} catch (ServiceException e) {
				if (propagation) {
					throw e;
				} else {
					getLog().error("An exception occurred before the service: " + service, e);
				}
			}
		} else if (CallMode.ASYNC == mode) {
			ServiceActuator.executeAsync(exec, pkgArg);
		}
	}

}
