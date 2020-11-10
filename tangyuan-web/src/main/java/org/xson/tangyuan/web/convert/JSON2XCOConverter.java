package org.xson.tangyuan.web.convert;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.xson.common.object.XCO;
import org.xson.tangyuan.util.FastJsonUtil;
import org.xson.tangyuan.web.RequestContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JSON2XCOConverter extends AbstractDataConverter {

	public final static JSON2XCOConverter instance = new JSON2XCOConverter();

	@Override
	public void convert(RequestContext requestContext) throws Throwable {
		HttpServletRequest request = requestContext.getRequest();
		byte[]             buffer  = IOUtils.toByteArray(request.getInputStream());
		JSONObject         obj     = (JSONObject) JSON.parse(new String(buffer, encoding));
		XCO                arg     = FastJsonUtil.toXCO(obj);

		setArg(requestContext, arg);
	}

	//	@Override
	//	public void convert(RequestContext requestContext, ControllerVo cVo) throws Throwable {
	//		HttpServletRequest request = requestContext.getRequest();
	//		byte[]             buffer  = IOUtils.toByteArray(request.getInputStream());
	//		JSONObject         obj     = (JSONObject) JSON.parse(new String(buffer, encoding));
	//		XCO                arg     = FastJsonUtil.toXCO(obj);
	//
	//		setArg(requestContext, arg);
	//	}

}
