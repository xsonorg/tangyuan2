package org.xson.tangyuan.web;

import java.io.IOException;

public interface ResponseHandler {

	void onSuccess(RequestContext context) throws IOException;

	// void onError(RequestContext context) throws IOException;

	void onError(RequestContext context, Throwable ex) throws IOException;
}
