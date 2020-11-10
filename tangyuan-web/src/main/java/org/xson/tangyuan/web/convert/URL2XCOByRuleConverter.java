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
		String              queryString = requestContext.getQueryString();
		Map<String, String> queryMap    = ServletUtil.queryStringToMap(queryString);

		XCO                 arg         = new XCO();
		this.ruleSupport.convertFromMap(arg, requestContext.getControllerVo(), queryMap);

		setArg(requestContext, arg);
	}

	//	@Override
	//	public void convert(RequestContext requestContext, ControllerVo cVo) throws Throwable {
	//		String              queryString = requestContext.getQueryString();
	//		Map<String, String> queryMap    = ServletUtil.queryStringToMap(queryString);
	//
	//		XCO                 arg         = new XCO();
	//		this.ruleSupport.convertFromMap(arg, cVo, queryMap);
	//
	//		setArg(requestContext, arg);
	//	}

	//	@Override
	//	public void convert(RequestContext requestContext, ControllerVo cVo) throws Throwable {
	//		String                     queryString = requestContext.getQueryString();
	//		Map<String, String>        queryMap    = ServletUtil.queryStringToMap(queryString);
	//
	//		// 附带检查的效果
	//		Map<String, RuleGroupItem> ruleMap     = this.ruleSupport.getRuleMap(cVo);
	//		if (CollectionUtils.isEmpty(queryMap)) {
	//			return;
	//		}
	//
	//		XCO arg = new XCO();
	//		for (Entry<String, String> entry : queryMap.entrySet()) {
	//			String        fieldName = entry.getKey();
	//			RuleGroupItem item      = ruleMap.get(fieldName);
	//			TypeEnum      type      = (null == item) ? null : item.getType();
	//			this.ruleSupport.setXCOValueSimple(arg, fieldName, type, entry.getValue());
	//		}
	//		setArg(requestContext, arg);
	//	}

}
