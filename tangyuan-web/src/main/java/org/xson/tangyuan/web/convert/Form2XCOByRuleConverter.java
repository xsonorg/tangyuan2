package org.xson.tangyuan.web.convert;

import javax.servlet.http.HttpServletRequest;

import org.xson.common.object.XCO;
import org.xson.tangyuan.web.RequestContext;

/**
 * Form K/V By Rule Data Converter
 */
public class Form2XCOByRuleConverter extends AbstractDataConverter {

	public final static Form2XCOByRuleConverter instance = new Form2XCOByRuleConverter();

	@Override
	public void convert(RequestContext requestContext) throws Throwable {
		HttpServletRequest request = requestContext.getRequest();
		XCO                arg     = new XCO();
		this.ruleSupport.convertFromRequest(arg, requestContext.getControllerVo(), request);
		setArg(requestContext, arg);
	}

}
