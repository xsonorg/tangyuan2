package org.xson.tangyuan.aop.vo;

import java.util.List;

import org.xson.tangyuan.aop.AopVo;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public class BeforeAopVo extends AopVo {

	protected static Log log = LogFactory.getLog(BeforeAopVo.class);

	public BeforeAopVo(String exec, int order, CallMode mode, List<String> includeList, List<String> excludeList) {
		this.exec = exec;
		this.order = order;
		this.mode = mode;
		this.includeList = includeList;
		this.excludeList = excludeList;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected void execBefore(Object pkgArg) {
		if (CallMode.EXTEND == mode) {
			ServiceActuator.execute(exec, pkgArg);
		} else if (CallMode.ALONE == mode) {
			ServiceActuator.executeAlone(exec, pkgArg);
		} else if (CallMode.ASYNC == mode) {
			ServiceActuator.executeAsync(exec, pkgArg);
		}
	}

}
