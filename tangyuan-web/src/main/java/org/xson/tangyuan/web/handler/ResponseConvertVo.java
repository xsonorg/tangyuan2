package org.xson.tangyuan.web.handler;

import java.util.List;

import org.xson.tangyuan.util.PatternMatchUtils;
import org.xson.tangyuan.web.ResponseHandler;

/**
 * response-handler拦截器VO
 */
public class ResponseConvertVo {

	private ResponseHandler	handler;
	private List<String>	includeList;
	private List<String>	excludeList;

	public ResponseConvertVo(ResponseHandler handler, List<String> includeList, List<String> excludeList) {
		this.handler = handler;
		this.includeList = includeList;
		this.excludeList = excludeList;
	}

	public boolean match(String url) {
		if (null != excludeList) {
			for (String pattern : excludeList) {
				if (PatternMatchUtils.simpleMatch(pattern, url)) {
					return false;
				}
			}
		}
		// fix bug
		if (null != includeList) {
			for (String pattern : includeList) {
				if (PatternMatchUtils.simpleMatch(pattern, url)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	public ResponseHandler getResponseHandler() {
		return this.handler;
	}

}
