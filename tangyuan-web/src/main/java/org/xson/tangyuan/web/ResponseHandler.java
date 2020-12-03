package org.xson.tangyuan.web;

public interface ResponseHandler {

	void onSuccess(RequestContext context) throws Throwable;

	void onError(RequestContext context, Throwable ex) throws Throwable;
}
