package org.xson.tangyuan.web.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.web.ControllerVo;
import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;
import org.xson.tangyuan.web.WebComponent;
import org.xson.tangyuan.web.convert.JSONDataConverter;
import org.xson.tangyuan.web.convert.XCODataConverter;
import org.xson.tangyuan.web.convert.XCORESTURIBodyDataConverter;
import org.xson.tangyuan.web.convert.XCORESTURIDataConverter;
import org.xson.tangyuan.web.convert.XCOXSONDataConverter;
import org.xson.tangyuan.web.rest.RestURIVo;

public class WebUtil {

	private static Log log = LogFactory.getLog(WebUtil.class);

	public static String getRestKey(RequestTypeEnum requestType, String path) {
		return requestType.toString() + " " + path;
	}

	public static DataConverter getDefaultDataConverter(ControllerVo cVo, String contextType) {
		if (null == contextType) {
			return null;
		}

		contextType = contextType.toLowerCase();

		if (WebComponent.getInstance().isRestMode()) {
			RequestTypeEnum requestType = cVo.getRequestType();
			if (contextType.indexOf("xco") > -1) {
				if (RequestTypeEnum.GET == requestType) {
					return XCORESTURIDataConverter.instance;
				} else if (RequestTypeEnum.POST == requestType) {
					return XCORESTURIBodyDataConverter.instance;
				} else if (RequestTypeEnum.PUT == requestType) {
					return XCORESTURIBodyDataConverter.instance;
				} else if (RequestTypeEnum.DELETE == requestType) {
					return XCORESTURIDataConverter.instance;
				}
			}
			if (contextType.indexOf("json") > -1) {
				// TODO
			}
		} else {
			if (contextType.indexOf("xco-xson") > -1) {
				return XCOXSONDataConverter.instance;
			}
			if (contextType.indexOf("xco") > -1) {
				return XCODataConverter.instance;
			}
			if (contextType.indexOf("xml") > -1) {
				return XCODataConverter.instance;
			}
			if (contextType.indexOf("json") > -1) {
				return JSONDataConverter.instance;
			}
		}
		return null;
	}

	public static List<String> parseURIPathItem(String path) {
		// 单独处理'/'
		if (RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR.equals(path)) {
			List<String> itemList = new ArrayList<String>();
			itemList.add(RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR);
			return itemList;
		}
		String rPath = path;
		if (rPath.length() > 1 && rPath.startsWith(RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR)) {
			rPath = rPath.substring(1);
		}
		if (rPath.endsWith(RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR)) {
			rPath = rPath.substring(0, rPath.length() - 1);
		}
		//		return RestUtil.splitToStringList(rPath, RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR);
		return StringUtils.splitStringToList(rPath, RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR);
	}

	public static Map<String, String> queryStringToMap(String query, Map<String, String> queryVariables) {
		Map<String, String> queryMap = new HashMap<String, String>();
		if (null == query || 0 == query.length()) {
			return queryMap;
		}
		//		List<String> itemList = RestUtil.splitToStringList(query, RestURIVo.URI_SYMBOL_AND);
		List<String> itemList = StringUtils.splitStringToList(query, RestURIVo.URI_SYMBOL_AND);
		if (null == itemList || 0 == itemList.size()) {
			return queryMap;
		}
		int size = itemList.size();
		for (int i = 0; i < size; i++) {
			String item = itemList.get(i);
			int    pos  = item.indexOf(RestURIVo.URI_SYMBOL_EQUAL);
			if (pos < 0) {
				log.warn("Invalid query string: " + query);
				continue;
			}
			String name  = item.substring(0, pos);
			String value = item.substring(pos + 1);
			if (null != queryVariables && queryVariables.containsKey(name)) {
				name = queryVariables.get(name);
			}
			queryMap.put(name, value);
		}
		return queryMap;
	}

	@SuppressWarnings("unchecked")
	public static void printHttpHeader(HttpServletRequest request) {
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key   = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			//			log.debug(key + ":" + value);
			log.info(key + ":" + value);
		}
	}

	@SuppressWarnings("unchecked")
	public static String getHttpHeaderContext(HttpServletRequest request) {
		Enumeration<String> headerNames = request.getHeaderNames();
		StringBuilder       sb          = new StringBuilder();
		while (headerNames.hasMoreElements()) {
			String key   = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			sb.append(key + ":" + value);
			sb.append("\n");
		}
		return sb.toString();
	}

	public static int inferredReturnType(HttpServletRequest request) {
		String contextType = request.getContentType();
		if (null == contextType) {
			return 2;//XCO
		}
		contextType = contextType.toLowerCase();
		if (contextType.indexOf("xco") > -1 || contextType.indexOf("xml") > -1) {
			return 0;
		}
		if (contextType.indexOf("json") > -1) {
			return 1;
		}
		return 2;
	}

	public static RequestTypeEnum parseRequestType(HttpServletRequest request) {
		//		if (WebComponent.getInstance().isRestMode()) {	}
		String requestMethod = request.getMethod();
		if (RequestTypeEnum.GET.toString().equals(requestMethod)) {
			return RequestTypeEnum.GET;
		}
		if (RequestTypeEnum.POST.toString().equals(requestMethod)) {
			return RequestTypeEnum.POST;
		}
		if (RequestTypeEnum.PUT.toString().equals(requestMethod)) {
			return RequestTypeEnum.PUT;
		}
		if (RequestTypeEnum.DELETE.toString().equals(requestMethod)) {
			return RequestTypeEnum.DELETE;
		}
		if (RequestTypeEnum.HEAD.toString().equals(requestMethod)) {
			return RequestTypeEnum.HEAD;
		}
		if (RequestTypeEnum.OPTIONS.toString().equals(requestMethod)) {
			return RequestTypeEnum.OPTIONS;
		}

		log.warn("Unsupported request type[" + requestMethod + "], uri: " + request.getRequestURI());
		return null;
	}
}
