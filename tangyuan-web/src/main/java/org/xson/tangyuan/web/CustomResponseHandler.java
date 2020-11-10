package org.xson.tangyuan.web;

/**
 * 用户自定义响应处理器，用作于自定义控制器输出
 */
public interface CustomResponseHandler {

	void response(RequestContext context);

}
