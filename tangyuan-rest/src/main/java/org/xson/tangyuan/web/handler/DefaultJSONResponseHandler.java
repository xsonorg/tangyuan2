package org.xson.tangyuan.web.handler;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.ResponseHandler;
import org.xson.tangyuan.web.WebComponent;

import com.alibaba.fastjson.JSONObject;

public class DefaultJSONResponseHandler implements ResponseHandler {

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
		Object result = context.getResult();
		if (null != result) {
			HttpServletResponse response = context.getResponse();
			if (result instanceof JSONObject) {
				result = ((JSONObject) result).toJSONString();
			}
			response.setContentType("application/json;charset=utf-8");
			response.setCharacterEncoding("UTF-8");
			Writer write = response.getWriter();
			write.write(result.toString());
			write.close();
		}
	}

	@Override
	public void onError(RequestContext context) throws IOException {
		JSONObject errorResult = new JSONObject();
		errorResult.put(TangYuanContainer.XCO_CODE_KEY, context.getCode());
		errorResult.put(TangYuanContainer.XCO_MESSAGE_KEY, context.getMessage());
		context.setResult(errorResult);
		onSuccess(context);
	}

}
