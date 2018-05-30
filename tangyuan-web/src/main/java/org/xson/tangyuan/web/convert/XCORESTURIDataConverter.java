package org.xson.tangyuan.web.convert;

import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.validate.RuleDataConverter;
import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.rest.RestURIVo;
import org.xson.tangyuan.web.util.ServletUtils;
import org.xson.tangyuan.web.xml.vo.ControllerVo;
import org.xson.tangyuan.web.xml.vo.RESTControllerVo;

/**
 * XCO REST URI Data Converter
 * 
 * get/delete request to use
 */
public class XCORESTURIDataConverter implements DataConverter {

	public final static XCORESTURIDataConverter instance = new XCORESTURIDataConverter();

	@Override
	public void convert(RequestContext requestContext, ControllerVo cVo) throws Throwable {

		// HttpServletRequest request = requestContext.getRequest();
		// String query = StringUtils.trim(request.getQueryString());

		String query = requestContext.getQueryString();

		RESTControllerVo restVo = (RESTControllerVo) cVo;
		RestURIVo restURIVo = restVo.getRestURIVo();

		// 生么情况下不需要处理URI，pathVar == null && queryString == null
		if ((null == query || 0 == query.length()) && restURIVo.isStaticURI()) {
			return;
		}

		// 1. queryString
		Map<String, String> queryVariables = restVo.getRestURIVo().getQueryVariables();
		Map<String, String> queryMap = ServletUtils.queryStringToMap(query, queryVariables);

		// 2. append path var
		if (!restURIVo.isStaticURI()) {
			// 这部分工作可以提前做,但是意义不大
			List<String> itemList = ServletUtils.parseURIPathItem(requestContext.getPath());
			Map<Integer, String> pathVariables = restURIVo.getPathVariables();
			for (Map.Entry<Integer, String> entry : pathVariables.entrySet()) {
				queryMap.put(entry.getValue(), itemList.get(entry.getKey()));
			}
		}

		// 当程序进行到这里,后续一定会产生xco参数
		XCO arg = RuleDataConverter.instance.convertRESTURI(queryMap, cVo);
		XCO old = (XCO) requestContext.getArg();
		if (null == old) {
			requestContext.setArg(arg);
		} else {
			old.append(arg);
			requestContext.setArg(old);
		}
	}

}
