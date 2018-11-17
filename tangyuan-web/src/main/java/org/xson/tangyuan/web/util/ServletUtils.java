package org.xson.tangyuan.web.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;
import org.xson.tangyuan.web.RequestContext.ReturnDataType;
import org.xson.tangyuan.web.WebComponent;
import org.xson.tangyuan.web.convert.JSONDataConverter;
import org.xson.tangyuan.web.convert.XCODataConverter;
import org.xson.tangyuan.web.convert.XCORESTURIBodyDataConverter;
import org.xson.tangyuan.web.convert.XCORESTURIDataConverter;
import org.xson.tangyuan.web.rest.RestURIVo;
import org.xson.tangyuan.web.xml.vo.ControllerVo;

public class ServletUtils {

	private static Log log = LogFactory.getLog(ServletUtils.class);

	public static String parseRequestURI(HttpServletRequest request) {
		// TODO ''空测试
		String uri = request.getRequestURI();
		int pos = uri.lastIndexOf(".");
		if (pos > -1) {
			return uri.substring(0, pos);
		}
		return uri;
	}

	public static boolean isViewRequest(RequestContext context) {
		String contextType = context.getContextType();
		if (null != contextType) {
			if (contextType.indexOf("xco") > -1 || contextType.indexOf("XCO") > -1) {
				return false;
			}
			if (contextType.indexOf("json") > -1) {
				return false;
			}
		}
		String ajaxHeader = context.getRequest().getHeader("X-Requested-With");
		if (null != ajaxHeader && ajaxHeader.equalsIgnoreCase("XMLHttpRequest")) {
			return false;
		}
		return true;
	}

	public static DataConverter getDefaultDataConverter(ControllerVo cVo, String contextType) {
		if (null == contextType) {
			return null;
		}
		if (WebComponent.getInstance().isRestMode()) {
			RequestTypeEnum requestType = cVo.getRequestType();
			if (contextType.indexOf("xco") > -1 || contextType.indexOf("XCO") > -1) {
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
			if (contextType.indexOf("xco") > -1 || contextType.indexOf("XCO") > -1) {
				return XCODataConverter.instance;
			}
			if (contextType.indexOf("json") > -1) {
				return JSONDataConverter.instance;
			}
			// TODO KV
		}
		return null;
	}

	public static ReturnDataType parseReturnDataType(String contextType) {
		if (null != contextType) {
			if (contextType.indexOf("xco") > -1 || contextType.indexOf("XCO") > -1) {
				return ReturnDataType.XCO;
			}
			if (contextType.indexOf("json") > -1) {
				return ReturnDataType.JSON;
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
		return RestUtil.splitToStringList(rPath, RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR);
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

	@SuppressWarnings("unchecked")
	public static String getHttpHeaderContext(HttpServletRequest request) {
		Enumeration<String> headerNames = request.getHeaderNames();
		StringBuilder sb = new StringBuilder();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			sb.append(key + ":" + value);
			sb.append("\n");
		}
		return sb.toString();
	}

	public static Map<String, String> queryStringToMap(String query) {
		Map<String, String> queryMap = new HashMap<String, String>();
		if (null == query || 0 == query.length()) {
			return queryMap;
		}
		List<String> itemList = RestUtil.splitToStringList(query, RestURIVo.URI_SYMBOL_AND);
		if (null == itemList || 0 == itemList.size()) {
			return queryMap;
		}
		int size = itemList.size();
		for (int i = 0; i < size; i++) {
			String item = itemList.get(i);
			int pos = item.indexOf(RestURIVo.URI_SYMBOL_EQUAL);
			if (pos < 0) {
				log.warn("Invalid query string: " + query);
				continue;
			}
			String name = item.substring(0, pos);
			String value = item.substring(pos + 1);
			queryMap.put(name, value);
		}
		return queryMap;
	}

	public static Map<String, String> queryStringToMap(String query, Map<String, String> queryVariables) {
		Map<String, String> queryMap = new HashMap<String, String>();
		if (null == query || 0 == query.length()) {
			return queryMap;
		}
		List<String> itemList = RestUtil.splitToStringList(query, RestURIVo.URI_SYMBOL_AND);
		if (null == itemList || 0 == itemList.size()) {
			return queryMap;
		}
		int size = itemList.size();
		for (int i = 0; i < size; i++) {
			String item = itemList.get(i);
			int pos = item.indexOf(RestURIVo.URI_SYMBOL_EQUAL);
			if (pos < 0) {
				log.warn("Invalid query string: " + query);
				continue;
			}
			String name = item.substring(0, pos);
			String value = item.substring(pos + 1);
			if (null != queryVariables && queryVariables.containsKey(name)) {
				name = queryVariables.get(name);
			}
			queryMap.put(name, value);
		}
		return queryMap;
	}

}
