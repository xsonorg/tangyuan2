package org.xson.tangyuan.es.converters;

import org.xson.tangyuan.es.EsResultConverter;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;

import com.alibaba.fastjson.JSON;

public abstract class AbstractEsResultConverter implements EsResultConverter {

	protected Log log = LogFactory.getLog(getClass());

	@Override
	public Object convertOnError(String json, int httpState) throws Throwable {
		//		if (LogExtUtil.isEsResponseResultErrorPrint()) {
		//			log.warn("http response state: " + httpState);
		//			log.warn("http response content: " + json);
		//		}
		try {
			// 如果是JSON类型字符串，可认为属于正常返回
			JSON.parseObject(json);
		} catch (Throwable e) {
			throw e;
		}
		return null;
	}
}
