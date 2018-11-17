package org.xson.tangyuan.aop.vo;

import java.util.List;

import org.xson.tangyuan.aop.AopVo;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.trace.TrackingManager;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public class AfterAopVo extends AopVo {

	protected static Log	log	= LogFactory.getLog(AfterAopVo.class);

	protected AopCondition	condition;

	public AfterAopVo(String exec, int order, CallMode mode, AopCondition condition, List<String> includeList, List<String> excludeList) {
		this.exec = exec;
		this.order = order;
		this.mode = mode;
		this.condition = condition;
		this.includeList = includeList;
		this.excludeList = excludeList;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected void execAfter(ServiceContext parent, Object pkgArg, Throwable ex) {

		if (AopCondition.SUCCESS == condition && null != ex) {
			return;
		}

		if (AopCondition.EXCEPTION == condition && null == ex) {
			return;
		}

		//		if (CallMode.EXTEND == mode) {
		//			ServiceActuator.execute(exec, pkgArg);
		//		} else if (CallMode.ALONE == mode) {
		//			ServiceActuator.executeAlone(exec, pkgArg);
		//		} else if (CallMode.ASYNC == mode) {
		//			ServiceActuator.executeAsync(exec, pkgArg);
		//		}

		if (CallMode.EXTEND == mode) {
			ServiceActuator.execute(exec, pkgArg);
		} else if (CallMode.ALONE == mode) {
			ServiceActuator.executeAlone(exec, pkgArg, parent, TrackingManager.EXECUTE_MODE_SYNC);
		} else if (CallMode.ASYNC == mode) {
			ServiceActuator.executeAsync(exec, pkgArg, parent);
		}
	}

}
