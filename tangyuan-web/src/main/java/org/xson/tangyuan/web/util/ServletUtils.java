package org.xson.tangyuan.web.util;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.validate.RuleDataConvert;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.WebComponent;
import org.xson.tangyuan.web.RequestContext.DataFormatEnum;
import org.xson.tangyuan.web.convert.KVDataConvert;
import org.xson.tangyuan.web.xml.ControllerVo;
import org.xson.tangyuan.web.xml.ControllerVo.DataConvertEnum;

import com.alibaba.fastjson.JSON;

public class ServletUtils {

	private static Log		log			= LogFactory.getLog(ServletUtils.class);

	private static String	encoding	= "UTF-8";

	public static String parseRequestURI(HttpServletRequest request) {
		String uri = request.getRequestURI();
		int pos = uri.lastIndexOf(".");
		if (pos > -1) {
			return uri.substring(0, pos);
		}
		return uri;
	}

	public static boolean isAjax(HttpServletRequest request, DataFormatEnum dataFormat) {
		String requestType = request.getHeader("X-Requested-With");
		if (null != requestType && requestType.equalsIgnoreCase("XMLHttpRequest")) {
			return true;
		}
		if (DataFormatEnum.XCO == dataFormat) {
			return true;
		}
		if (DataFormatEnum.JSON == dataFormat) {
			return true;
		}
		return false;
	}

	public static DataFormatEnum parseDataFormat(String contextType) {
		if (null == contextType) {
			return DataFormatEnum.KV;
		}
		if (contextType.indexOf("xco") > -1) {
			return DataFormatEnum.XCO;
		}
		if (contextType.indexOf("json") > -1) {
			return DataFormatEnum.JSON;
		}
		if (contextType.indexOf("multipart/form-data") > -1) {
			return DataFormatEnum.FILE;
		}
		return DataFormatEnum.KV;
	}

	public static Object parseArgFromPostRequest(ControllerVo cVo, HttpServletRequest request, RequestContext context) throws Exception {
		if (DataFormatEnum.XCO == context.getDataFormat()) {
			byte[] buffer = IOUtils.toByteArray(request.getInputStream());
			String xml = new String(buffer, encoding);
			xml = java.net.URLDecoder.decode(xml, encoding);
			return XCO.fromXML(xml);
		} else if (DataFormatEnum.JSON == context.getDataFormat()) {
			byte[] buffer = IOUtils.toByteArray(request.getInputStream());
			return JSON.parse(new String(buffer, encoding));
		} else if (DataFormatEnum.KV == context.getDataFormat()) {
			// if (DataConvertEnum.KV == cVo.getConvert()) {
			// return KVDataConvert.convert(request);
			// } else if (DataConvertEnum.RULE == cVo.getConvert()) {
			// return RuleDataConvert.convert(request, cVo.getValidate());
			// }
			if (DataConvertEnum.KV_RULE_XCO == cVo.getConvert()) {
				return RuleDataConvert.convert(request, cVo.getValidate());
			} else if (DataConvertEnum.KV_XCO == cVo.getConvert()) {
				return KVDataConvert.convert(request);
			} else if (WebComponent.getInstance().isKvAutoConvert()) {
				if (null != cVo.getValidate()) {
					return RuleDataConvert.convert(request, cVo.getValidate());
				} else {
					return KVDataConvert.convert(request);
				}
			}
		}
		return null;
	}

	public static Object parseArgFromGetRequest(ControllerVo cVo, HttpServletRequest request) throws Exception {
		// if (DataConvertEnum.KV == cVo.getConvert()) {
		// return KVDataConvert.convert(request);
		// } else if (DataConvertEnum.RULE == cVo.getConvert()) {
		// return RuleDataConvert.convert(request, cVo.getValidate());
		// }
		if (DataConvertEnum.KV_RULE_XCO == cVo.getConvert()) {
			return RuleDataConvert.convert(request, cVo.getValidate());
		} else if (DataConvertEnum.KV_XCO == cVo.getConvert()) {
			return KVDataConvert.convert(request);
		} else if (WebComponent.getInstance().isKvAutoConvert()) {
			if (null != cVo.getValidate()) {
				return RuleDataConvert.convert(request, cVo.getValidate());
			} else {
				return KVDataConvert.convert(request);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static void printHttpHeader(HttpServletRequest request) {
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			log.debug(key + ":" + value);
		}
	}

}
