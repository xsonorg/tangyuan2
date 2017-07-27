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

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.web.util.ServletUtils;
import org.xson.tangyuan.web.xml.ControllerVo;

public abstract class AbstractPermissionFilter implements Filter {

	private Log log = LogFactory.getLog(getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String urlPath = ServletUtils.parseRequestURI((HttpServletRequest) request);
		ControllerVo cVo = WebComponent.getInstance().getControllerVo(urlPath);
		if (null == cVo) {
			log.error("Mismatched URL path: " + urlPath);
			return;
		}
		RequestContext requestContext = new RequestContext((HttpServletRequest) request, (HttpServletResponse) response);
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
	 * @param permission 		权限标记
	 * @param requestContext	请求上下文
	 * @return 权限验证结果,true代表验证通过
	 */
	abstract public boolean permissionCheck(String permission, RequestContext requestContext);

	/**
	 * 权限检测失败后的处理
	 * 
	 * @param requestContext	请求上下文
	 */
	abstract public void authFailed(RequestContext requestContext);

}
