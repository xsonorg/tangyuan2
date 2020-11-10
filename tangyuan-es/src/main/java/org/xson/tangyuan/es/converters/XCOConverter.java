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

	//	protected Log        log = LogFactory.getLog(getClass());
	//	@Override
	//	public Object convertOnError(String json, int httpState) throws Throwable {
	//		if (LogExtUtil.isEsResponseResultErrorPrint()) {
	//			log.warn("http response state: " + httpState);
	//			log.warn("http response content: " + json);
	//		}
	//		try {
	//			// 如果是JSON类型字符串，可认为属于正常返回
	//			JSON.parseObject(json);
	//		} catch (Throwable e) {
	//			throw e;
	//		}
	//		return null;
	//	}
}
