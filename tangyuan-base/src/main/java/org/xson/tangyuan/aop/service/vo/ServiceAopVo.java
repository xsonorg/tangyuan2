package org.xson.tangyuan.aop.service.vo;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.util.TangYuanUtil;

/**
 * 切面和服务映射
 */
public class ServiceAopVo {

	private static Log		log				= LogFactory.getLog(ServiceAopVo.class);

	public static String	aopArgKey		= "arg";
	public static String	aopServiceKey	= "service";
	public static String	aopResultKey	= "result";
	// public static String aopExKey = "ex";

	private String			service			= null;
	private List<AopVo>		beforeList		= null;
	private List<AopVo>		afterList		= null;

	public ServiceAopVo(String service, List<AopVo> beforeList, List<AopVo> afterList) {
		this.service = service;
		this.beforeList = beforeList;
		this.afterList = afterList;
	}

	public String getService() {
		return service;
	}

	public void execBefore(String service, Object arg) throws Throwable {
		if (CollectionUtils.isEmpty(this.beforeList)) {
			return;
		}
		try {
			for (AopVo aVo : this.beforeList) {
				log.info(TangYuanUtil.format("execute aop before: {}", aVo.getExec()));
				aVo.execBefore(service, (XCO) arg);
			}
		} catch (Throwable e) {
			log.error(e);
			throw e;
		}
		// for (AopVo aVo : this.beforeList) {
		// log.info(TangYuanUtil.format("execute aop before: {}", aVo.getExec()));
		// aVo.execBefore(service, (XCO) arg);
		// }
	}

	public void execAfter(String service, Object arg, Object result, Throwable ex) throws Throwable {
		try {
			for (AopVo aVo : this.afterList) {
				log.info(TangYuanUtil.format("execute aop after: {}", aVo.getExec()));
				aVo.execAfter(service, (XCO) arg, (XCO) result, ex);
			}
		} catch (Throwable e) {
			log.error(e);
			throw e;
		}
		// for (AopVo aVo : this.afterList) {
		// log.info(TangYuanUtil.format("execute aop after: {}", aVo.getExec()));
		// aVo.execAfter(service, (XCO) arg, (XCO) result, ex);
		// }
	}

}
