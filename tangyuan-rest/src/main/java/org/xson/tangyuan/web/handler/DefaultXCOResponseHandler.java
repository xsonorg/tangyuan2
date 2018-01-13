package org.xson.tangyuan.web.handler;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.ResponseHandler;
import org.xson.tangyuan.web.WebComponent;

public class DefaultXCOResponseHandler implements ResponseHandler {

	private Log log = LogFactory.getLog(DefaultXCOResponseHandler.class);

	private void printResult(RequestContext context) {
		if (WebComponent.getInstance().isPrintResultLog()) {
			Object result = context.getResult();
			if (null == result) {
				log.info("controller[" + context.getPath() + "] result: null");
			} else {
				log.info("controller[" + context.getPath() + "] result: " + result.toString());
			}
		}
	}

	@Override
	public void onSuccess(RequestContext context) throws IOException {
		printResult(context);
		XCO result = (XCO) context.getResult();
		if (null != result) {
			HttpServletResponse response = context.getResponse();
			response.setContentType("text/xml;charset=utf-8");
			response.setCharacterEncoding("UTF-8");
			Writer write = response.getWriter();
			write.write(result.toString());
			write.close();
		}
	}

	@Override
	public void onError(RequestContext context) throws IOException {
		XCO errorResult = new XCO();
		setXCOResult(errorResult, context.getCode(), context.getMessage());
		context.setResult(errorResult);
		onSuccess(context);
	}

	private void setXCOResult(XCO xco, int code, String message) {
		xco.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, code);
		xco.setStringValue(TangYuanContainer.XCO_MESSAGE_KEY, message);
	}

}
