package org.xson.tangyuan.web.convert;

import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.xml.vo.ControllerVo;

/**
 * JSON REST URI & Body Data Converter
 * 
 * post/put request to use
 */
public class JSONRESTURIBodyDataConverter implements DataConverter {

	public final static JSONRESTURIBodyDataConverter instance = new JSONRESTURIBodyDataConverter();

	@Override
	public void convert(RequestContext requestContext, ControllerVo cVo) throws Throwable {
		JSONRESTURIDataConverter.instance.convert(requestContext, cVo);
		JSONDataConverter.instance.convert(requestContext, cVo);
	}

}
