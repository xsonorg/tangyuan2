package org.xson.tangyuan.web.convert;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.xson.common.object.XCO;
import org.xson.tangyuan.web.RequestContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Normal URI Data Converter
 * 
 * get/delete request to use
 */
public class JSON2XCOByRuleConverter extends AbstractDataConverter {

	public final static JSON2XCOByRuleConverter instance = new JSON2XCOByRuleConverter();

	@Override
	public void convert(RequestContext requestContext) throws Throwable {

		HttpServletRequest request = requestContext.getRequest();
		byte[] buffer = IOUtils.toByteArray(request.getInputStream());
		JSONObject obj = (JSONObject) JSON.parse(new String(buffer, encoding));

		XCO arg = new XCO();
		this.ruleSupport.convertFromJSON(arg, requestContext.getControllerVo(), obj);
		setArg(requestContext, arg);
	}

}