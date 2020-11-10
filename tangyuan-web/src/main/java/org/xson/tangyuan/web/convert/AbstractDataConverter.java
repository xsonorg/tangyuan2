package org.xson.tangyuan.web.convert;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.validate.RuleConverterSupport;
import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.XCOWebException;

import com.alibaba.fastjson.JSONObject;

public abstract class AbstractDataConverter implements DataConverter {

	protected RuleConverterSupport ruleSupport = new RuleConverterSupport();

	protected void setArg(RequestContext requestContext, Object arg) {
		if (null == arg) {
			return;
		}
		Object old = requestContext.getArg();
		if (null == old) {
			requestContext.setArg(arg);
			return;
		}
		if (old instanceof XCO && arg instanceof XCO) {
			((XCO) old).append((XCO) arg);
			requestContext.setArg(old);
		} else if (old instanceof JSONObject && arg instanceof JSONObject) {
			((JSONObject) old).putAll((JSONObject) arg);
			requestContext.setArg(old);
		} else {
			throw new XCOWebException(TangYuanLang.get("web.converter.arg.type.diff"));
		}
	}
}
