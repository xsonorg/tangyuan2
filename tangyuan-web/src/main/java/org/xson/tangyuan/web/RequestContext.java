package org.xson.tangyuan.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.web.util.ServletUtils;

/**
 * 请求上下文
 */
public class RequestContext {

	public enum RequestTypeEnum {
		GET, POST, PUT, DELETE, HEAD, OPTIONS
	}

	/** 返回数据类型 */
	public enum ReturnDataType {
		XCO, JSON
	}

	private static Log			log				= LogFactory.getLog(RequestContext.class);

	private HttpServletRequest	request			= null;
	private HttpServletResponse	response		= null;

	/**
	 * @see path
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private String				url				= null;
	private String				path			= null;
	private String				queryString		= null;
	private String				view			= null;
	/** 视图请求 */
	private boolean				viewRequest		= false;
	/** 页面跳转 */
	private boolean				redirect		= false;
	private String				contextType		= null;
	private RequestTypeEnum		requestType		= null;
	private Object				result			= null;
	private Object				arg				= null;
	/** 是否存在上下文 */
	private boolean				inThread		= false;
	private Integer				code			= null;
	private String				message			= null;
	/** 附加对象 */
	private Map<String, Object>	attach			= null;
	/** 返回对象类型 */
	private ReturnDataType		returnDataType	= null;

	public RequestContext(HttpServletRequest request, HttpServletResponse response, String path) {
		this(request, response, true, path);
	}

	protected RequestContext(HttpServletRequest request, HttpServletResponse response, boolean inThread, String path) {
		this.request = request;
		this.response = response;
		this.inThread = inThread;
		this.path = path;
	}

	public void init() {
		this.contextType = request.getContentType();// may be is null
		this.queryString = StringUtils.trim(request.getQueryString());
		this.viewRequest = ServletUtils.isViewRequest(this);
		this.returnDataType = ServletUtils.parseReturnDataType(this.contextType);
		log.info(toString());
		if (log.isDebugEnabled()) {
			ServletUtils.printHttpHeader(request);
		}
	}

	public void forward(String view) {
		if (null != this.view) {
			throw new XCOWebException("The view already exists in the current context.");
		}
		this.view = view;
		this.redirect = false;
		this.viewRequest = true;
	}

	public void sendRedirect(String view) {
		if (null != this.view) {
			throw new XCOWebException("The view already exists in the current context.");
		}
		this.view = view;
		this.redirect = true;
		this.viewRequest = true;
	}

	public String getView() {
		return view;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * @deprecated Use {@link #getPath()} instead.
	 */
	@Deprecated
	public String getUrl() {
		return this.path;
	}

	public String getPath() {
		return this.path;
	}

	public String getContextType() {
		return contextType;
	}

	public RequestTypeEnum getRequestType() {
		return requestType;
	}

	protected void setRequestType(RequestTypeEnum requestType) {
		this.requestType = requestType;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Object getResult() {
		return result;
	}

	public Object getArg() {
		return arg;
	}

	public void setArg(Object arg) {
		this.arg = arg;
	}

	public void setErrorInfo(int code, String message) {
		// if (this.code != code) {
		// this.code = code;
		// }
		// if (null == this.message || !this.message.equals(message)) {
		// this.message = message;
		// }
		if (null != this.code || null != this.message) {
			log.warn("The current controller context has been set code and message. uri: " + this.path);
		}
		this.code = code;
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public boolean isInThread() {
		return inThread;
	}

	public boolean isRedirect() {
		return redirect;
	}

	public boolean isViewRequest() {
		return viewRequest;
	}

	public String getQueryString() {
		return queryString;
	}

	public ReturnDataType getReturnDataType() {
		return returnDataType;
	}

	public void setReturnDataType(ReturnDataType returnDataType) {
		this.returnDataType = returnDataType;
	}

	public Map<String, Object> getAttach() {
		if (null == attach) {
			attach = new HashMap<String, Object>();
		}
		return attach;
	}

	@Override
	public String toString() {
		return requestType + " " + this.path + ", contextType: " + this.contextType;
	}

}
