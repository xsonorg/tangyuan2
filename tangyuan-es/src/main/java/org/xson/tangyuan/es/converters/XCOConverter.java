package org.xson.tangyuan.es.converters;

import org.xson.tangyuan.util.FastJsonUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class XCOConverter extends AbstractEsResultConverter {

	public static String key = "@xco";

	@Override
	public Object convert(String json) throws Throwable {
		JSONObject jsonObject = JSON.parseObject(json);
		return FastJsonUtil.toXCO(jsonObject);
	}

}
