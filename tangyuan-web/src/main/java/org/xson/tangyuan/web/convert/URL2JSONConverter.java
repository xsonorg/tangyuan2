package org.xson.tangyuan.web.convert;

import java.util.Map;
import java.util.Map.Entry;

import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.util.ServletUtil;
import org.xson.tangyuan.web.RequestContext;

import com.alibaba.fastjson.JSONObject;

/**
 * Normal URI Data Converter
 * 
 * get/delete request to use
 */
public class URL2JSONConverter extends AbstractDataConverter {

	public final static URL2JSONConverter instance = new URL2JSONConverter();

	@Override
	public void convert(RequestContext requestContext) throws Throwable {
		String              queryString = requestContext.getQueryString();
		Map<String, String> queryMap    = ServletUtil.queryStringToMap(queryString);

		if (CollectionUtils.isEmpty(queryMap)) {
			return;
		}

		JSONObject arg = new JSONObject();
		for (Entry<String, String> entry : queryMap.entrySet()) {
			arg.put(entry.getKey(), entry.getValue());
		}
		setArg(requestContext, arg);
	}

}
