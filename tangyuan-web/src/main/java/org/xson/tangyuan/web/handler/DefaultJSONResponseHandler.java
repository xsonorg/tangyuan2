package org.xson.tangyuan.web.handler;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.ResponseHandler;

import com.alibaba.fastjson.JSONObject;

public class DefaultJSONResponseHandler implements ResponseHandler {

	@Override
	public void onSuccess(RequestContext context) throws IOException {
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
		// TODO
	}

}
