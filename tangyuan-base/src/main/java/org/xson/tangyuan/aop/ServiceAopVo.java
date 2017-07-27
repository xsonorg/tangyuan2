package org.xson.tangyuan.aop;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.aop.AspectVo.PointCut;
import org.xson.tangyuan.executor.ServiceException;

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

	private List<AspectVo>	beforeCheckList	= null;
	private List<AspectVo>	beforeJoinList	= null;
	private List<AspectVo>	afterJoinList	= null;
	private List<AspectVo>	beforeAloneList	= null;
	private List<AspectVo>	afterAloneList	= null;

	public ServiceAopVo(String service, List<AspectVo> beforeCheckList, List<AspectVo> beforeJoinList, List<AspectVo> afterJoinList,
			List<AspectVo> beforeAloneList, List<AspectVo> afterAloneList) {
		this.service = service;
		this.beforeCheckList = beforeCheckList;
		this.beforeJoinList = beforeJoinList;
		this.afterJoinList = afterJoinList;
		this.beforeAloneList = beforeAloneList;
		this.afterAloneList = afterAloneList;
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

		// TODO
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

		// TODO
		throw new TangYuanException("AOP components do not support data types other than XCO.");
	}

	public void execBefore(String service, Object arg, PointCut pointCut) {
		List<AspectVo> beforeList = null;
		if (PointCut.BEFORE_CHECK == pointCut) {
			beforeList = beforeCheckList;
		} else if (PointCut.BEFORE_JOIN == pointCut) {
			beforeList = beforeJoinList;
		} else if (PointCut.BEFORE_ALONE == pointCut) {
			beforeList = beforeAloneList;
		}
		if (null == beforeList) {
			return;
		}

		Object pkgArg = assembleAopArg(service, arg);
		String exec = null;
		try {
			for (AspectVo aVo : beforeList) {
				exec = aVo.getExec();
				aVo.execBefore(service, pkgArg);
			}
		} catch (Throwable e) {
			if (PointCut.BEFORE_ALONE == pointCut) {
				log.error("service[" + service + "] execute before-alone error. for:" + exec, e);
			} else {
				if (e instanceof ServiceException) {
					throw (ServiceException) e;
				} else {
					throw new TangYuanException(e);
				}
			}
		}
	}

	public void execAfter(String service, Object arg, Object result, Throwable ex, PointCut pointCut) {
		List<AspectVo> afterList = null;
		if (PointCut.AFTER_JOIN == pointCut) {
			afterList = afterJoinList;
		} else if (PointCut.AFTER_ALONE == pointCut) {
			afterList = afterAloneList;
		}
		if (null == afterList) {
			return;
		}

		Object pkgArg = assembleAopArg(service, arg, result, ex);
		String exec = null;
		try {
			for (AspectVo aVo : afterList) {
				exec = aVo.getExec();
				aVo.execAfter(service, pkgArg, result, ex);
			}
		} catch (Throwable e) {
			if (PointCut.AFTER_ALONE == pointCut) {
				log.error("service[" + service + "] execute after-alone error. for:" + exec, e);
			} else {
				if (e instanceof ServiceException) {
					throw (ServiceException) e;
				} else {
					throw new TangYuanException(e);
				}
			}
		}
	}
}
