package org.xson.tangyuan.aop;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.util.TangYuanUtil;

/**
 * 切面和服务映射
 */
public class ServiceAopVo {

	private static Log		log				= LogFactory.getLog(ServiceAopVo.class);

	protected static String	aopArgKey		= "arg";
	protected static String	aopServiceKey	= "service";
	protected static String	aopResultKey	= "result";
	protected static String	aopExKey		= "ex";

	private String			service			= null;

	private List<AopVo>		beforeList		= null;
	private List<AopVo>		afterExtendList	= null;
	private List<AopVo>		afterList		= null;

	public ServiceAopVo(String service, List<AopVo> beforeList, List<AopVo> afterExtendList, List<AopVo> afterList) {
		this.service = service;
		this.beforeList = beforeList;
		this.afterExtendList = afterExtendList;
		this.afterList = afterList;
	}

	public String getService() {
		return service;
	}

	/** 组装AOP参数:before */
	private Object assembleAopArg(String service, Object arg) {
		if (arg instanceof XCO) {
			XCO pkgArg = new XCO();
			pkgArg.setStringValue(aopServiceKey, service);
			pkgArg.setXCOValue(aopArgKey, (XCO) arg);
			return pkgArg;
		}
		// if (arg instanceof Map) {
		// Map pkgArg = new HashMap();
		// pkgArg.put(aopServiceKey, service);
		// pkgArg.put(aopArgKey, arg);
		// return pkgArg;
		// }
		// return arg;

		throw new TangYuanException("AOP components do not support data types other than XCO.");
	}

	/** 组装AOP参数:after */
	private Object assembleAopArg(String service, Object arg, Object result, Throwable ex) {
		if (arg instanceof XCO) {
			XCO pkgArg = new XCO();
			pkgArg.setStringValue(aopServiceKey, service);
			pkgArg.setXCOValue(aopArgKey, (XCO) arg);

			int errorCode = TangYuanContainer.SUCCESS_CODE;
			String errorMessage = null;
			if (null != result) {
				pkgArg.setObjectValue(aopResultKey, result);
				if (result instanceof XCO) {
					XCO resultXCO = (XCO) result;
					Integer code = resultXCO.getCode();
					if (null != code && 0 != code.intValue()) {
						errorCode = code.intValue();
						errorMessage = resultXCO.getMessage();
					}
				}
			}
			if (null != ex) {
				pkgArg.setAttachObject(ex);
				if (ex instanceof ServiceException) {
					ServiceException e = (ServiceException) ex;
					errorCode = e.getErrorCode();
					errorMessage = e.getErrorMessage();
				} else {
					errorCode = TangYuanContainer.getInstance().getErrorCode();
					errorMessage = ex.getMessage();
				}
			}
			pkgArg.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, errorCode);
			if (null != errorMessage) {
				pkgArg.setStringValue(TangYuanContainer.XCO_MESSAGE_KEY, errorMessage);
			}
			return pkgArg;
		}

		throw new TangYuanException("AOP components do not support data types other than XCO.");
	}

	public void execBefore(String service, Object arg) throws Throwable {
		if (null == this.beforeList || 0 == this.beforeList.size()) {
			return;
		}

		Object pkgArg = assembleAopArg(service, arg);
		String exec = null;

		try {
			for (AopVo aVo : this.beforeList) {
				exec = aVo.getExec();
				log.info(TangYuanUtil.format("execute aop before: {}, service: {}", exec, service));
				aVo.execBefore(pkgArg);
			}
		} catch (Throwable e) {
			//			log.error("service[" + service + "] execute aop before error. due to:" + exec, e);
			//			log.error(TangYuanUtil.format("execute service exception: {}", exec), e);
			log.error(TangYuanUtil.format("execute service exception: {}", exec));
			throw e;
		}
	}

	public void execAfter(ServiceContext parent, String service, Object arg, Object result, Throwable ex, boolean extend) {

		List<AopVo> tempAfterList = null;
		if (extend) {
			tempAfterList = this.afterExtendList;
		} else {
			tempAfterList = this.afterList;
		}

		if (null == tempAfterList || 0 == tempAfterList.size()) {
			return;
		}

		Object pkgArg = assembleAopArg(service, arg, result, ex);
		String exec = null;
		try {
			for (AopVo aVo : tempAfterList) {
				exec = aVo.getExec();
				log.info(TangYuanUtil.format("execute aop after: {}, service: {}", exec, service));
				//				aVo.execAfter(pkgArg, ex);
				aVo.execAfter(parent, pkgArg, ex);
			}
		} catch (Throwable e) {
			//			log.error("service[" + service + "] execute aop after error. due to:" + exec);
			//			log.error(TangYuanUtil.format("execute service exception: {}", exec), e);
			log.error(TangYuanUtil.format("execute service exception: {}", exec));
			throw e;
		}
	}
}
