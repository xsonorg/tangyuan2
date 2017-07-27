package org.xson.tangyuan.aop;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.ognl.vars.ArgSelfVo;

public class DefaultPostCutAssembly implements PostCutAssembly {

	@Override
	public Object assembly(String service, Object arg, Object result, Throwable ex) {
		XCO xco = new XCO();
		xco.setObjectValue(ArgSelfVo.AEG_SELF_MARK, arg);
		xco.setObjectValue("$$RETURN", result);// TODO

		// int errorCode = TangYuanContainer.SUCCESS_CODE_RPC;
		int errorCode = TangYuanContainer.SUCCESS_CODE;

		String errorMessage = null;
		if (null == ex) {
			if (null != result && result instanceof XCO) {
				XCO resultXCO = (XCO) result;
				Integer code = resultXCO.getCode();
				if (null != code && 0 != code.intValue()) {
					xco.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, code.intValue());
					xco.setStringValue(TangYuanContainer.XCO_MESSAGE_KEY, resultXCO.getMessage());
					return xco;
				}
			}
			xco.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, errorCode);
		} else {
			xco.setAttachObject(ex);
			if (ex instanceof ServiceException) {
				ServiceException e = (ServiceException) ex;
				errorCode = e.getErrorCode();
				errorMessage = e.getErrorMessage();
			} else {
				errorCode = TangYuanContainer.getInstance().getErrorCode();
				errorMessage = ex.getMessage();
			}
			xco.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, errorCode);
			xco.setStringValue(TangYuanContainer.XCO_MESSAGE_KEY, errorMessage);
		}
		return xco;
	}

}
