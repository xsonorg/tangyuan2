package org.xson.tangyuan.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.web.util.ServletResponseUtil;

public abstract class AbstractPermissionFilter implements Filter {

	private Log log = LogFactory.getLog(getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		RequestContext requestContext = new RequestContext((HttpServletRequest) request, (HttpServletResponse) response, null, true);
		ControllerVo   cVo            = requestContext.getControllerVo();
		if (null == cVo) {
			log.errorLang("web.servlet.invalid.controller", ((HttpServletRequest) request).getRequestURI());
			WebComponent     container = WebComponent.getInstance();
			ServiceException se        = new ServiceException(container.getErrorCode(), container.getErrorMessage());
			ServletResponseUtil.doResponseError((HttpServletRequest) request, (HttpServletResponse) response, se);
			return;
		}
		if (permissionCheck(cVo.getPermission(), requestContext)) {
			WebComponent.getInstance().requestContextThreadLocal.set(requestContext);
			chain.doFilter(request, response);
		} else {
			authFailed(requestContext);
		}
	}

	/**
	 * 权限检测
	 * 
	 * @param permission
	 *            权限标记
	 * @param requestContext
	 *            请求上下文
	 * @return 权限验证结果,true代表验证通过
	 */
	abstract public boolean permissionCheck(String permission, RequestContext requestContext);

	/**
	 * 权限检测失败后的处理
	 * 
	 * @param requestContext
	 *            请求上下文
	 */
	abstract public void authFailed(RequestContext requestContext);

	//	private RequestTypeEnum parseRequestType(HttpServletRequest request) {
	//		if (WebComponent.getInstance().isRestMode()) {
	//			String requestMethod = request.getMethod();
	//			if (RequestTypeEnum.GET.toString().equals(requestMethod)) {
	//				return RequestTypeEnum.GET;
	//			}
	//			if (RequestTypeEnum.POST.toString().equals(requestMethod)) {
	//				return RequestTypeEnum.POST;
	//			}
	//			if (RequestTypeEnum.PUT.toString().equals(requestMethod)) {
	//				return RequestTypeEnum.PUT;
	//			}
	//			if (RequestTypeEnum.DELETE.toString().equals(requestMethod)) {
	//				return RequestTypeEnum.DELETE;
	//			}
	//			if (RequestTypeEnum.HEAD.toString().equals(requestMethod)) {
	//				return RequestTypeEnum.HEAD;
	//			}
	//			if (RequestTypeEnum.OPTIONS.toString().equals(requestMethod)) {
	//				return RequestTypeEnum.OPTIONS;
	//			}
	//
	//			log.warn("Unsupported request type[" + requestMethod + "], uri: " + request.getRequestURI());
	//		}
	//		return null;
	//	}
	//	@Override
	//	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
	//		//		String uriPath = ServletUtils.parseRequestURI((HttpServletRequest) request);
	//		String       uriPath = ServletUtil.parseRequestURI((HttpServletRequest) request);
	//		ControllerVo cVo     = WebComponent.getInstance().getControllerVo(parseRequestType((HttpServletRequest) request), uriPath);
	//		if (null == cVo) {
	//			log.error("Mismatched URL path: " + uriPath);
	//			return;
	//		}
	//		RequestContext requestContext = new RequestContext((HttpServletRequest) request, (HttpServletResponse) response, uriPath);
	//		if (permissionCheck(cVo.getPermission(), requestContext)) {
	//			WebComponent.getInstance().requestContextThreadLocal.set(requestContext);
	//			chain.doFilter(request, response);
	//		} else {
	//			authFailed(requestContext);
	//		}
	//	}

}
