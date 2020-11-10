package org.xson.tangyuan.web.convert;

import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.util.ServletUtil;
import org.xson.tangyuan.web.RequestContext;

/**
 * Normal URI Data Converter
 * 
 * get/delete request to use
 */
public class URL2XCOByRuleConverter extends AbstractDataConverter {

	public final static URL2XCOByRuleConverter instance = new URL2XCOByRuleConverter();

	@Override
	public void convert(RequestContext requestContext) throws Throwable {
		String queryString = requestContext.getQueryString();
		Map<String, String> queryMap = ServletUtil.queryStringToMap(queryString);

		XCO arg = new XCO();
		this.ruleSupport.convertFromMap(arg, requestContext.getControllerVo(), queryMap);

		setArg(requestContext, arg);
	}

}
