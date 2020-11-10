package org.xson.tangyuan.aop;

import org.xson.common.object.XCO;
import org.xson.tangyuan.aop.service.vo.ServiceAopVo;

/**
 * AOP包装对象工具类
 */
public class AopPkgUtil {

	public static XCO getArg(XCO pkgObj) {
		return pkgObj.getXCOValue(ServiceAopVo.aopArgKey);
	}

	public static String getService(XCO pkgObj) {
		return pkgObj.getStringValue(ServiceAopVo.aopServiceKey);
	}

	public static Object getResult(XCO pkgObj) {
		return pkgObj.getObjectValue(ServiceAopVo.aopResultKey);
	}

}
