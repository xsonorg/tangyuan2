package org.xson.tangyuan.es.converters;

import org.xson.tangyuan.es.ResultConverter;

import com.alibaba.fastjson.JSON;

public class JSONConverter implements ResultConverter {

	public static String key = "@json";

	@Override
	public Object convert(String json) {
		return JSON.parseObject(json);
	}

}
