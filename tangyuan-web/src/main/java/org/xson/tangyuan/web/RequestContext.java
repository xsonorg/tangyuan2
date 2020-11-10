package org.xson.tangyuan.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.log.ext.LogExtUtil;
import org.xson.tangyuan.util.ServletUtil;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.web.util.WebUtil;

/**
 * 请求上下文
 */
public class RequestContext {

	public enum RequestTypeEnum {
		GET, POST, PUT, DELETE, HEAD, OPTIONS
	}

	private static Log            log          = LogFactory.getLog(RequestContext.class);

	private HttpServletRequest    request      = null;
	private HttpServletResponse   response     = null;

	/**
	 * @see path
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private String                url          = null;
	private String                path         = null;
	private String                queryString  = null;
	private String                view         = null;
	/** 页面跳转 */
	private boolean               redirect     = false;
	/** 页面转发 */
	private boolean               forward      = false;
	private String                contextType  = null;
	private RequestTypeEnum       requestType  = null;
	private Object                result       = null;
	private Object                arg          = null;
	/** 是否存在上下文 */
	private boolean               inThread     = false;
	/** 附加对象 */
	private Map<String, Object>   attach       = null;
	private String                cacheKey     = null;
	/** 用户自定义响应 */
	private CustomResponseHandler crh          = null;
	/**	RuntimeContext Header */
	private XCO                   rcHeader     = null;

	private ControllerVo          controllerVo = null;

	protected RequestContext(HttpServletRequest request, HttpServletResponse response, RequestTypeEnum requestType, boolean inThread) {
		this.request = request;
		this.response = response;
		this.requestType = requestType;
		this.inThread = inThread;
		pretreatment();
	}

	private void pretreatment() {
		this.path = ServletUtil.parseRequestURI(this.request);
		this.queryString = StringUtils.trimEmpty(this.request.getQueryString());
		this.contextType = this.request.getContentType();// may be is null
		if (null == this.requestType) {
			this.requestType = WebUtil.parseRequestType(this.request);
		}
		this.controllerVo = WebComponent.getInstance().getControllerVo(this.requestType, this.path);
		log.info(toString());
		if (LogExtUtil.isWebRequestHeaderPrint()) {
			WebUtil.printHttpHeader(this.request);
		}
	}

	public void init() {
		this.rcHeader = ServletUtil.parseRCInfoFromRequestHeader(this.request);
	}

	

	public void setForward(String view) {
		if (null != this.view) {
			throw new XCOWebException(TangYuanLang.get("web.RequestContext.forward.exists"));
		}
		this.view = view;
		this.forward = true;
	}

	public void setRedirect(String view) {
		if (null != this.view) {
			throw new XCOWebException(TangYuanLang.get("web.RequestContext.redirect.exists"));
		}
		this.view = view;
		this.redirect = true;
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

	public boolean isInThread() {
		return inThread;
	}

	public boolean isRedirect() {
		return redirect;
	}

	public boolean isForward() {
		return forward;
	}

	public String getQueryString() {
		return queryString;
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

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}

	public String getCacheKey() {
		return cacheKey;
	}

	public void setCustomResponseHandler(CustomResponseHandler crh) {
		if (null != this.result) {
			throw new XCOWebException(TangYuanLang.get("web.RequestContext.CustomResponseHandler.set.invalid"));
		}
		this.crh = crh;
	}

	public CustomResponseHandler getCustomResponseHandler() {
		return crh;
	}

	public XCO getRcHeader() {
		return rcHeader;
	}

	public ControllerVo getControllerVo() {
		return controllerVo;
	}

	//////////////////////////////////////////////////////////////////////////

//	protected RequestContext(HttpServletRequest request, HttpServletResponse response, boolean inThread, String path) {
	//		this.request = request;
	//		this.response = response;
	//		this.inThread = inThread;
	//		this.path = path;
	//	}	
	//	public RequestContext(HttpServletRequest request, HttpServletResponse response, String path) {
	//		this(request, response, true, path);
	//	}
	//	protected RequestContext(HttpServletRequest request, HttpServletResponse response, boolean inThread, String path) {
	//		this.request = request;
	//		this.response = response;
	//		this.inThread = inThread;
	//		this.path = path;
	//	}

	//	public void init() {
	//		this.contextType = this.request.getContentType();// may be is null
	//		this.queryString = StringUtils.trimEmpty(this.request.getQueryString());
	//		this.rcHeader = ServletUtil.parseRCInfoFromRequestHeader(this.request);
	//		log.info(toString());
	//		if (LogExtUtil.isWebRequestHeaderPrint()) {
	//			WebUtil.printHttpHeader(this.request);
	//		}
	//	}
	
	/** 返回数据类型 */
	//	public enum ReturnDataType {
	//		XCO, JSON, TEXT, STREAM
	//	}
	/** 视图请求 */
	//	private boolean               viewRequest    = false;
	//	private Integer               code           = null;
	//	private String                message        = null;
	/** 返回对象类型 */
	//	private ReturnDataType        returnDataType = null;

	//	public ReturnDataType getReturnDataType() {
	//		return returnDataType;
	//	}
	//
	//	public void setReturnDataType(ReturnDataType returnDataType) {
	//		this.returnDataType = returnDataType;
	//	}

	//	public void setErrorInfo(int code, String message) {
	//		// if (this.code != code) {
	//		// this.code = code;
	//		// }
	//		// if (null == this.message || !this.message.equals(message)) {
	//		// this.message = message;
	//		// }
	//		if (null != this.code || null != this.message) {
	//			log.warn("The current controller context has been set code and message. uri: " + this.path);
	//		}
	//		this.code = code;
	//		this.message = message;
	//	}

	//	public Integer getCode() {
	//		return code;
	//	}
	//
	//	public String getMessage() {
	//		return message;
	//	}
	//	public boolean isViewRequest() {
	//		return viewRequest;
	//	}

	//	public void setResult(Object result) { handler
	//		this.result = result;
	//	}

	//	public void forward(String view) {
	//		if (null != this.view) {
	//			throw new XCOWebException("The view already exists in the current context.");
	//		}
	//		this.view = view;
	//		this.redirect = false;
	//		this.viewRequest = true;
	//	}
	//	public void sendRedirect(String view) {
	//		if (null != this.view) {
	//			throw new XCOWebException("The view already exists in the current context.");
	//		}
	//		this.view = view;
	//		this.redirect = true;
	//		this.viewRequest = true;
	//	}

	//		this.viewRequest = ServletUtils.isViewRequest(this);
	//		this.returnDataType = ServletUtils.parseReturnDataType(this.contextType);
	//		rcHeader = org.xson.tangyuan.util.ServletUtil.parseRCInfoFromRequestHeader(this.request);

	//	protected static RequestContext create(HttpServletRequest request, HttpServletResponse response, RequestTypeEnum requestType, boolean inThread) {
	//		RequestContext rc = new RequestContext(request, response, requestType, inThread);
	//		return rc;
	//	}
}
