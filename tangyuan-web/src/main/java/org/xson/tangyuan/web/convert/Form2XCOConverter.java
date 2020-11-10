package org.xson.tangyuan.web.convert;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.xson.common.object.XCO;
import org.xson.tangyuan.web.RequestContext;

/**
 * Form K/V Data Converter
 */
public class Form2XCOConverter extends AbstractDataConverter {

	public final static Form2XCOConverter instance = new Form2XCOConverter();

	@SuppressWarnings("unchecked")
	@Override
	public void convert(RequestContext requestContext) throws Throwable {
		HttpServletRequest  request     = requestContext.getRequest();
		Enumeration<String> enumeration = request.getParameterNames();
		XCO                 arg         = new XCO();
		while (enumeration.hasMoreElements()) {
			String field = enumeration.nextElement();
			arg.setStringValue(field, request.getParameter(field));
		}
		setArg(requestContext, arg);
	}

	//		String              queryString = requestContext.getQueryString();
	//		Map<String, String> queryMap    = ServletUtil.queryStringToMap(queryString);
	//		if (CollectionUtils.isEmpty(queryMap)) {
	//			return;
	//		}
	//		Map                parameterMap = request.getParameterMap();
	//		for (Entry entry : (Entry) parameterMap.entrySet()) {
	//
	//		}
	//		for (Entry<String, String> entry : queryMap.entrySet()) {
	//			arg.setStringValue(entry.getKey(), entry.getValue());
	//		}
}
