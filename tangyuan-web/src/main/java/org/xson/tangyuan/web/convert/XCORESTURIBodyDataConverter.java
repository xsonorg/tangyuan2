package org.xson.tangyuan.web.convert;

import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.xml.vo.ControllerVo;

/**
 * XCO REST URI & Body Data Converter
 * 
 * post/put request to use
 */
public class XCORESTURIBodyDataConverter implements DataConverter {

	public final static XCORESTURIBodyDataConverter instance = new XCORESTURIBodyDataConverter();

	@Override
	public void convert(RequestContext requestContext, ControllerVo cVo) throws Throwable {
		XCORESTURIDataConverter.instance.convert(requestContext, cVo);
		XCODataConverter.instance.convert(requestContext, cVo);
	}

}
