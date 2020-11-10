package org.xson.tangyuan.web.convert;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.xson.common.object.XCO;
import org.xson.tangyuan.web.RequestContext;

public class XCOXSONDataConverter extends AbstractDataConverter {

	public final static XCOXSONDataConverter instance = new XCOXSONDataConverter();

	@Override
	public void convert(RequestContext requestContext) throws Throwable {
		HttpServletRequest request = requestContext.getRequest();
		byte[]             buffer  = IOUtils.toByteArray(request.getInputStream());
		XCO                arg     = XCO.fromBytes(buffer);
		setArg(requestContext, arg);
	}

}
