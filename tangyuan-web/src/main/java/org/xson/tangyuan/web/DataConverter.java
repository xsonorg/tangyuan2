package org.xson.tangyuan.web;

public interface DataConverter {

	String encoding = "UTF-8";

	void convert(RequestContext requestContext) throws Throwable;

}
