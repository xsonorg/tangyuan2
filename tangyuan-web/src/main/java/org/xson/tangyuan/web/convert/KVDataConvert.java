package org.xson.tangyuan.web.convert;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.xson.common.object.XCO;
import org.xson.tangyuan.util.StringUtils;

public class KVDataConvert {

	@SuppressWarnings("unchecked")
	public static XCO convert(HttpServletRequest request) {
		Map<String, String> parameterMap = request.getParameterMap();
		XCO xco = new XCO();
		if (null == parameterMap || 0 == parameterMap.size()) {
			return xco;
		}
		for (Entry<String, String> entry : parameterMap.entrySet()) {
			xco.setStringValue(entry.getKey().trim(), StringUtils.trim(entry.getValue()));
		}
		return xco;
	}

}
