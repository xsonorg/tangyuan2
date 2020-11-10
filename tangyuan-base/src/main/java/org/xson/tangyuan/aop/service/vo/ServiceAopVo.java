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

	private static Log   log           = LogFactory.getLog(ServiceAopVo.class);

	public static String aopArgKey     = "arg";
	public static String aopServiceKey = "service";
	public static String aopResultKey  = "result";
	//	public static String aopExKey      = "ex";

	private String       service       = null;
	private List<AopVo>  beforeList    = null;
	private List<AopVo>  afterList     = null;

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
		//		for (AopVo aVo : this.beforeList) {
		//			log.info(TangYuanUtil.format("execute aop before: {}", aVo.getExec()));
		//			aVo.execBefore(service, (XCO) arg);
		//		}
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
		//		for (AopVo aVo : this.afterList) {
		//			log.info(TangYuanUtil.format("execute aop after: {}", aVo.getExec()));
		//			aVo.execAfter(service, (XCO) arg, (XCO) result, ex);
		//		}
	}

	////////////////////////////////////////////////////////////////
	//	private List<AopVo>  afterExtendList = null;
	//	public ServiceAopVo(String service, List<AopVo> beforeList, List<AopVo> afterExtendList, List<AopVo> afterList) {
	//		this.service = service;
	//		this.beforeList = beforeList;
	//		this.afterExtendList = afterExtendList;
	//		this.afterList = afterList;
	//	}

	//	/**
	//	 * 组装AOP参数:before
	//	 * @param service
	//	 * @param arg
	//	 * @return
	//	 */
	//	private Object assembleAopArg(String service, Object arg) {
	//		if (!(arg instanceof XCO)) {
	//			throw new TangYuanException("AOP components do not support data types other than XCO.");
	//		}
	//		// if (arg instanceof Map) {
	//		// Map pkgArg = new HashMap();
	//		// pkgArg.put(aopServiceKey, service);
	//		// pkgArg.put(aopArgKey, arg);
	//		// return pkgArg;
	//		// }
	//		// return arg;
	//
	//		XCO pkgArg = new XCO();
	//		pkgArg.setStringValue(aopServiceKey, service);
	//		pkgArg.setXCOValue(aopArgKey, (XCO) arg);
	//		return pkgArg;
	//	}

	//	/** 组装AOP参数:after */
	//	private Object assembleAopArg(String service, Object arg, Object result, Throwable ex) {
	//		if (!(arg instanceof XCO)) {
	//			throw new TangYuanException("AOP components do not support data types other than XCO.");
	//		}
	//
	//		XCO pkgArg = new XCO();
	//		pkgArg.setStringValue(aopServiceKey, service);
	//		pkgArg.setXCOValue(aopArgKey, (XCO) arg);
	//
	//		int    errorCode    = TangYuanContainer.SUCCESS_CODE;
	//		String errorMessage = null;
	//		if (null != result) {
	//			pkgArg.setObjectValue(aopResultKey, result);
	//			if (result instanceof XCO) {
	//				XCO     resultXCO = (XCO) result;
	//				Integer code      = resultXCO.getCode();
	//				if (null != code && 0 != code.intValue()) {
	//					errorCode = code.intValue();
	//					errorMessage = resultXCO.getMessage();
	//				}
	//			}
	//		}
	//		if (null != ex) {
	//			pkgArg.setAttachObject(ex);
	//			if (ex instanceof ServiceException) {
	//				ServiceException e = (ServiceException) ex;
	//				errorCode = e.getErrorCode();
	//				errorMessage = e.getErrorMessage();
	//			} else {
	//				errorCode = TangYuanContainer.getInstance().getErrorCode();
	//				errorMessage = ex.getMessage();
	//			}
	//		}
	//		pkgArg.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, errorCode);
	//		if (null != errorMessage) {
	//			pkgArg.setStringValue(TangYuanContainer.XCO_MESSAGE_KEY, errorMessage);
	//		}
	//		return pkgArg;
	//
	//	}

	//	public void execBefore(String service, Object arg) throws Throwable {
	//		//		if (null == this.beforeList || 0 == this.beforeList.size()) {
	//		//			return;
	//		//		}
	//
	//		if (CollectionUtils.isEmpty(this.beforeList)) {
	//			return;
	//		}
	//
	//		Object pkgArg = assembleAopArg(service, arg);
	//		String exec   = null;
	//
	//		try {
	//			for (AopVo aVo : this.beforeList) {
	//				exec = aVo.getExec();
	//				log.info(TangYuanUtil.format("execute aop before: {}, service: {}", exec, service));
	//				aVo.execBefore(pkgArg);
	//			}
	//		} catch (Throwable e) {
	//			log.error(TangYuanUtil.format("execute service exception: {}", exec));
	//			throw e;
	//		}
	//	}

	//	public void execAfter(ActuatorContext parent, String service, Object arg, Object result, Throwable ex, boolean extend) {
	//		List<AopVo> tempAfterList = null;
	//		if (extend) {
	//			tempAfterList = this.afterExtendList;
	//		} else {
	//			tempAfterList = this.afterList;
	//		}
	//		if (null == tempAfterList || 0 == tempAfterList.size()) {
	//			return;
	//		}
	//
	//		Object pkgArg = assembleAopArg(service, arg, result, ex);
	//		String exec   = null;
	//		try {
	//			for (AopVo aVo : tempAfterList) {
	//				exec = aVo.getExec();
	//				log.info(TangYuanUtil.format("execute aop after: {}, service: {}", exec, service));
	//				//				aVo.execAfter(pkgArg, ex);
	//				aVo.execAfter(parent, pkgArg, ex);
	//			}
	//		} catch (Throwable e) {
	//			// 这里抛出异常的只有Extend. 对于Alone和ASYNC,是不会抛出异常的，所以是对的
	//			//			log.error("service[" + service + "] execute aop after error. due to:" + exec);
	//			log.error(TangYuanUtil.format("execute service exception: {}", exec));
	//			throw e;
	//		}
	//	}
}
