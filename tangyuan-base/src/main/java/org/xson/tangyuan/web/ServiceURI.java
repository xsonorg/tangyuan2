package org.xson.tangyuan.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.xson.tangyuan.util.ServletUtil;
import org.xson.tangyuan.util.StringUtils;

// service+mode

public class ServiceURI {

	private static String markMode    = "mode";
	private static String markVersion = "version";

	private String        service;
	private String        version;
	private String        mode;

	private ServiceURI(String service, String version, String mode) {
		this.service = service;
		this.version = version;
		this.mode = mode;
	}

	/**
	 * 从request中分析service信息
	 * 
	 * http://www.xson.com/service/ID.xco?mode=async&version=1.0
	 */
	public static ServiceURI parse(HttpServletRequest request) throws Throwable {
		String              queryString = StringUtils.trimEmpty(request.getQueryString());
		Map<String, String> queryMap    = ServletUtil.queryStringToMap(queryString);

		String              service     = ServletUtil.parseServiceURI(request);
		String              version     = queryMap.get(markVersion);
		String              mode        = queryMap.get(markMode);
		return new ServiceURI(service, version, mode);
	}

	public String getService() {
		return service;
	}

	public String getVersion() {
		return version;
	}

	public String getMode() {
		return mode;
	}

}
