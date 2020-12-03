package org.xson.tangyuan.web.handler;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.ext.LogExtUtil;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.ResponseHandler;
import org.xson.tangyuan.web.WebComponent;
import org.xson.tangyuan.web.util.ServletResponseUtil;

/**
 * 默认的响应处理器，仅支持XCO类型的响应，其他需要自定义实现
 */
public class DefaultResponseHandler implements ResponseHandler {

	private Log log = LogFactory.getLog(DefaultResponseHandler.class);

	private void printResult(RequestContext context, Object result) {
		if (null == result) {
			//			log.info(context.getContextType() + " " + context.getPath() + ", result: Null");
			log.info("Result[" + context.getPath() + "]: Null");
		} else {
			//log.info(context.getContextType() + " " + context.getPath() + ", result: " + result.toString());
			log.info("Result[" + context.getPath() + "]: " + result.toString());
		}
	}

	private XCO convertResult(RequestContext context) {
		Object result = context.getResult();
		// 有可能转换出异常，交由上层处理
		result = TangYuanUtil.retObjToXco(result);
		if (LogExtUtil.isWebResponseResultPrint()) {
			printResult(context, result);
		}
		return (XCO) result;
	}

	@Override
	public void onSuccess(RequestContext context) throws Throwable {
		XCO result = convertResult(context);
		ServletResponseUtil.responseXCO(context, result);
	}

	@Override
	public void onError(RequestContext context, Throwable ex) throws Throwable {
		XCO errorResult = null;
		if (ex instanceof ServiceException) {
			errorResult = TangYuanUtil.getExceptionResult(ex);
		} else {
			errorResult = new XCO();
			setXCOResult(errorResult, WebComponent.getInstance().getErrorCode(), WebComponent.getInstance().getErrorMessage());
		}
		context.setResult(errorResult);
		onSuccess(context);
	}

	private void setXCOResult(XCO xco, int code, String message) {
		xco.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, code);
		xco.setStringValue(TangYuanContainer.XCO_MESSAGE_KEY, message);
	}

}
