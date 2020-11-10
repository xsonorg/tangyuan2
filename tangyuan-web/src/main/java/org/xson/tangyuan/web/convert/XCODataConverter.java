package org.xson.tangyuan.web.convert;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.xson.common.object.XCO;
import org.xson.tangyuan.web.RequestContext;

public class XCODataConverter extends AbstractDataConverter {

	public final static XCODataConverter instance = new XCODataConverter();

	@Override
	public void convert(RequestContext requestContext) throws Throwable {
		HttpServletRequest request = requestContext.getRequest();
		byte[]             buffer  = IOUtils.toByteArray(request.getInputStream());
		String             xml     = new String(buffer, encoding);
		xml = java.net.URLDecoder.decode(xml, encoding);
		XCO arg = XCO.fromXML(xml);

		setArg(requestContext, arg);
	}

	//	@Override
	//	public void convert(RequestContext requestContext, ControllerVo cVo) throws Throwable {
	//		HttpServletRequest request = requestContext.getRequest();
	//		byte[]             buffer  = IOUtils.toByteArray(request.getInputStream());
	//		String             xml     = new String(buffer, encoding);
	//		xml = java.net.URLDecoder.decode(xml, encoding);
	//		XCO arg = XCO.fromXML(xml);
	//		XCO old = (XCO) requestContext.getArg();
	//		if (null == old) {
	//			requestContext.setArg(arg);
	//		} else {
	//			old.append(arg);
	//			requestContext.setArg(old);
	//		}
	//	}

}
