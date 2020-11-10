package org.xson.tangyuan.util;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.xson.common.object.XCO;

public class ServletUtil {

	public static final String URI_SYMBOL_FOLDER_SEPARATOR = "/";
	public static final String URI_SYMBOL_ASTERISK         = "*";
	public static final String URI_SYMBOL_HASHTAG          = "#";
	public static final String URI_SYMBOL_QUESTION_MARK    = "?";
	public static final String URI_SYMBOL_AND              = "&";
	public static final String URI_SYMBOL_EQUAL            = "=";

	public static String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		// TODO 考虑多IP的兼容
		return ip;
	}

	public static XCO getXCOArg(HttpServletRequest request) throws IOException {
		byte[] buffer = IOUtils.toByteArray(request.getInputStream());
		return XCO.fromXML(new String(buffer, "UTF-8"));
	}

	public static void reponse(HttpServletResponse response, XCO result) throws IOException {
		if (null != result) {
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			// response.setContentType("text/xml;charset=utf-8");
			response.setCharacterEncoding("UTF-8");
			Writer write = response.getWriter();
			write.write(result.toXMLString());
			write.close();
		}
	}

	public static Map<String, String> queryStringToMap(String query) throws Throwable {
		Map<String, String> queryMap = new HashMap<String, String>();
		if (StringUtils.isEmptySafe(query)) {
			return queryMap;
		}

		query = java.net.URLDecoder.decode(query, "UTF-8");

		List<String> itemList = StringUtils.splitStringToList(query, URI_SYMBOL_AND);
		if (CollectionUtils.isEmpty(itemList)) {
			return queryMap;
		}
		int size = itemList.size();
		for (int i = 0; i < size; i++) {
			String item = itemList.get(i);
			int    pos  = item.indexOf(URI_SYMBOL_EQUAL);
			if (pos < 0) {
				continue;
			}
			String name  = item.substring(0, pos);
			String value = item.substring(pos + 1);
			queryMap.put(name, value);
		}
		return queryMap;
	}

	public static String parseServiceURI(HttpServletRequest request) {

		String uri         = StringUtils.trimEmpty(request.getRequestURI());
		String contextPath = StringUtils.trimEmpty(request.getContextPath());
		if (null == uri || "/".equals(uri)) {
			return uri;
		}

		// 1. 去除后缀名
		int pos = uri.lastIndexOf(".");
		if (pos > -1) {
			uri = uri.substring(0, pos);
		}

		// 2. 去除contextPath
		if (null != contextPath) {
			uri = uri.substring(contextPath.length());
		}

		// 3. 去除"/"
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}
		return uri;
	}

	public static String parseRequestURI(HttpServletRequest request) {

		String uri         = StringUtils.trimEmpty(request.getRequestURI());
		String contextPath = StringUtils.trimEmpty(request.getContextPath());
		if (null == uri || "/".equals(uri)) {
			return uri;
		}

		// 1. 去除后缀名
		int pos = uri.lastIndexOf(".");
		if (pos > -1) {
			uri = uri.substring(0, pos);
		}

		// 2. 去除contextPath
		if (null != contextPath) {
			uri = uri.substring(contextPath.length());
		}

		//		// 3. 去除"/"
		//		if (uri.startsWith("/")) {
		//			uri = uri.substring(1);
		//		}
		return uri;
	}

	/**
	 * 从Http的Header中分析上下文相关的内容
	 */
	public static XCO parseRCInfoFromRequestHeader(HttpServletRequest request) {
		// TODO 
		return null;
	}
}
