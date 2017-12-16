package org.xson.tangyuan.es.converters;

import org.xson.tangyuan.es.ResultConverter;
import org.xson.tangyuan.es.util.FastJsonUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class XCOConverter implements ResultConverter {

	public static String key = "@xco";

	@Override
	public Object convert(String json) {
		JSONObject jsonObject = JSON.parseObject(json);
		return FastJsonUtil.toXCO(jsonObject);
	}

}
