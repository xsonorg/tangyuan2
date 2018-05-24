package org.xson.tangyuan.aop;

import org.xson.common.object.XCO;

/**
 * AOP包装对象工具类
 */
public class AopPkgUtil {

	//	public static XCO getXCOArg(XCO pkgObj) {
	//		return pkgObj.getXCOValue(ServiceAopVo.aopArgKey);
	//	}
	//
	//	public static String getXCOService(XCO pkgObj) {
	//		return pkgObj.getStringValue(ServiceAopVo.aopServiceKey);
	//	}
	//
	//	public static Object getXCOResult(XCO pkgObj) {
	//		return pkgObj.getObjectValue(ServiceAopVo.aopResultKey);
	//	}
	//
	//	public static Object getXCOException(XCO pkgObj) {
	//		return pkgObj.getAttachObject();
	//	}

	public static XCO getArg(XCO pkgObj) {
		return pkgObj.getXCOValue(ServiceAopVo.aopArgKey);
	}

	public static String getService(XCO pkgObj) {
		return pkgObj.getStringValue(ServiceAopVo.aopServiceKey);
	}

	public static Object getResult(XCO pkgObj) {
		return pkgObj.getObjectValue(ServiceAopVo.aopResultKey);
	}

	public static Object getException(XCO pkgObj) {
		return pkgObj.getAttachObject();
	}
}
