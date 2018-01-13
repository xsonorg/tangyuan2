package org.xson.tangyuan.web;

import org.xson.tangyuan.web.xml.vo.ControllerVo;

public interface DataConverter {

	String encoding = "UTF-8";

	public void convert(RequestContext requestContext, ControllerVo cVo) throws Throwable;

}
