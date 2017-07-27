package org.xson.tangyuan.aop.vo;

import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.aop.AspectVo;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceException;

public class BeforeCheckVo extends AspectVo {

	protected static Log log = LogFactory.getLog(BeforeCheckVo.class);

	public BeforeCheckVo(String exec, int order, List<String> includeList, List<String> excludeList) {
		this.exec = exec;
		this.order = order;
		this.includeList = includeList;
		this.excludeList = excludeList;
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected void execBefore(String service, Object pkgArg) {
		Object obj = ServiceActuator.executeAlone(exec, pkgArg);
		if (null != obj && obj instanceof XCO) {
			XCO xco = (XCO) obj;
			Integer code = xco.getCode();
			if (null != code && 0 != code.intValue()) {
				throw new ServiceException(code.intValue(), xco.getMessage());
			}
		} else if (null != obj && obj instanceof Map) {
			// TODO
		}
	}
}
