package org.xson.tangyuan.aop.vo;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.aop.AspectVo;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public class AfterAloneVo extends AspectVo {

	protected static Log		log	= LogFactory.getLog(AfterAloneVo.class);

	protected CallMode			mode;

	/** 是否会影响其他切面 */
	protected boolean			propagation;

	protected AspectCondition	condition;

	public AfterAloneVo(String exec, int order, List<String> includeList, List<String> excludeList, CallMode mode, boolean propagation,
			AspectCondition condition) {
		this.exec = exec;
		this.order = order;
		this.includeList = includeList;
		this.excludeList = excludeList;
		this.mode = mode;
		this.propagation = propagation;
		this.condition = condition;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected void execAfter(String service, Object pkgArg, Object result, Throwable ex) {
		// if (AspectCondition.SUCCESS == condition && null != ex) {
		// return;
		// }

		if (AspectCondition.SUCCESS == condition) {
			if (null != ex) {
				return;
			}
			// 判断执行结果
			if (null != result && result instanceof XCO) {
				XCO xco = (XCO) result;
				Integer code = xco.getCode();
				if (null != code && 0 != code.intValue()) {
					return;
				}
			}
		}

		if (AspectCondition.EXCEPTION == condition && null == ex) {
			return;
		}
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

	// @Override
	// protected void execAfter(String service, Object pkgArg, Object result, Throwable ex) {
	// if (AspectCondition.SUCCESS == condition && null != ex) {
	// return;
	// }
	// if (AspectCondition.EXCEPTION == condition && null == ex) {
	// return;
	// }
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

	// @Override
	// protected void execAfter(String service, Object arg, Object result, Throwable ex) {
	// if (AspectCondition.SUCCESS == condition && null != ex) {
	// return;
	// }
	// if (AspectCondition.EXCEPTION == condition && null == ex) {
	// return;
	// }
	// Object pkgObj = assembly.assembly(service, arg, result, ex);
	// if (CallMode.ALONE == mode) {
	// try {
	// ServiceActuator.executeAlone(exec, pkgObj);
	// } catch (ServiceException e) {
	// if (propagation) {
	// throw e;
	// } else {
	// getLog().error("An exception occurred before the service: " + service, e);
	// }
	// }
	// } else if (CallMode.ASYNC == mode) {
	// ServiceActuator.executeAsync(exec, pkgObj);
	// }
	// }

}
