package org.xson.tangyuan.web.convert;

import javax.servlet.http.HttpServletRequest;

import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.xml.vo.ControllerVo;

/**
 * JSON REST URI Data Converter
 * 
 * get/delete request to use
 */
public class JSONRESTURIDataConverter implements DataConverter {

	public final static JSONRESTURIDataConverter instance = new JSONRESTURIDataConverter();

	@Override
	public void convert(RequestContext requestContext, ControllerVo cVo) throws Throwable {
		HttpServletRequest request = requestContext.getRequest();
		String query = StringUtils.trim(request.getQueryString());
		if (null == query || 0 == query.length()) {
			return;
		}
		// TODO
	}

}
