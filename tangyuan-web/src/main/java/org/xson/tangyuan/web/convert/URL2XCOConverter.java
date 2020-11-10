package org.xson.tangyuan.web.convert;

import java.util.Map;
import java.util.Map.Entry;

import org.xson.common.object.XCO;
import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.util.ServletUtil;
import org.xson.tangyuan.web.RequestContext;

/**
 * Normal URI Data Converter
 * 
 * get/delete request to use
 */
public class URL2XCOConverter extends AbstractDataConverter {

	public final static URL2XCOConverter instance = new URL2XCOConverter();

	@Override
	public void convert(RequestContext requestContext) throws Throwable {
		String queryString = requestContext.getQueryString();
		Map<String, String> queryMap = ServletUtil.queryStringToMap(queryString);

		if (CollectionUtils.isEmpty(queryMap)) {
			return;
		}

		XCO arg = new XCO();
		for (Entry<String, String> entry : queryMap.entrySet()) {
			arg.setStringValue(entry.getKey(), entry.getValue());
		}
		setArg(requestContext, arg);
	}

}
