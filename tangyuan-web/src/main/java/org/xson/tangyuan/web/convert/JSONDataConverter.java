package org.xson.tangyuan.web.convert;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.xson.tangyuan.web.RequestContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JSONDataConverter extends AbstractDataConverter {

	public final static JSONDataConverter instance = new JSONDataConverter();

	@Override
	public void convert(RequestContext requestContext) throws Throwable {
		HttpServletRequest request = requestContext.getRequest();
		byte[]             buffer  = IOUtils.toByteArray(request.getInputStream());
		JSONObject         arg     = (JSONObject) JSON.parse(new String(buffer, encoding));
		setArg(requestContext, arg);
	}

	//	@Override
	//	public void convert(RequestContext requestContext, ControllerVo cVo) throws Throwable {
	//		HttpServletRequest request = requestContext.getRequest();
	//		byte[] buffer = IOUtils.toByteArray(request.getInputStream());
	//		JSONObject arg = (JSONObject) JSON.parse(new String(buffer, encoding));
	//		JSONObject old = (JSONObject) requestContext.getArg();
	//		if (null == old) {
	//			requestContext.setArg(arg);
	//		} else {
	//			old.putAll(arg);
	//			requestContext.setArg(old);
	//		}
	//	}

}
