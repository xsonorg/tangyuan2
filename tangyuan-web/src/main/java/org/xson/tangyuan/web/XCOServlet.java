package org.xson.tangyuan.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.manager.TangYuanManager;
import org.xson.tangyuan.manager.trace.TraceManager;
import org.xson.tangyuan.service.runtime.RuntimeContext;
import org.xson.tangyuan.util.ServletUtil;
import org.xson.tangyuan.validate.XCOValidate;
import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;
import org.xson.tangyuan.web.handler.DefaultResponseHandler;
import org.xson.tangyuan.web.util.ServletResponseUtil;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class XCOServlet extends HttpServlet {

	private static final long serialVersionUID       = 1L;

	private static Log        log                    = LogFactory.getLog(XCOServlet.class);
	private ResponseHandler   defaultResponseHandler = new DefaultResponseHandler();

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handler(req, resp, RequestTypeEnum.PUT);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handler(req, resp, RequestTypeEnum.DELETE);
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handler(req, resp, RequestTypeEnum.GET);
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handler(req, resp, RequestTypeEnum.POST);
	}

	private void handler(HttpServletRequest req, HttpServletResponse resp, RequestTypeEnum requestType) {

		long           now        = System.currentTimeMillis();
		WebComponent   container  = WebComponent.getInstance();
		String         requestURI = req.getRequestURI();
		RequestContext context    = pretreatmentContext(req, resp, requestType);
		ControllerVo   cVo        = context.getControllerVo();

		try {
			if (!container.isRunning()) {
				log.errorLang("web.servlet.invalid.state", requestURI);
				throw new ServiceException(container.getErrorCode(), container.getErrorMessage());
			}
			if (!container.isRestMode()) {
				if (RequestTypeEnum.POST != requestType && RequestTypeEnum.GET != requestType) {
					log.errorLang("web.servlet.invalid.requestType", requestURI, requestType);
					throw new ServiceException(container.getErrorCode(), container.getErrorMessage());
				}
			}
		} catch (ServiceException se) {
			ServletResponseUtil.doResponseError(context, se);
			return;
		}

		if (null == cVo) {
			log.errorLang("web.servlet.invalid.controller", requestURI);
			ServiceException se = new ServiceException(container.getErrorCode(), container.getErrorMessage());
			ServletResponseUtil.doResponseError(context, se);
			return;
		}

		String remoteIp = ServletUtil.getIpAddress(req);

		try {
			checkAll(cVo, remoteIp);
		} catch (ServiceException se) {
			log.errorLang(se, "web.servlet.invalid.access", requestURI);
			ServletResponseUtil.doResponseError(context, se);
			return;
		}

		// 服务跟踪在这里开始,无效的URL对于服务跟踪是无意义的 (之前日志中可能会丢丢失, 但不影响)
		Object traceContext = null;
		RuntimeContext.beginFromHeader(context.getRcHeader(), TraceManager.TRACE_ORIGIN_WEB);

		// data convert
		try {
			cVo.dataConvert(context);
			if (log.isInfoEnabled()) {
				log.info(requestType + " " + context.getPath() + ", arg: " + context.getArg());
			}
		} catch (Throwable e) {
			log.errorLang(e, "web.servlet.invalid.convert", requestURI);
			doResponseError(traceContext, context, e);
			return;
		}

		// 记录追踪信息
		traceContext = startTracking(cVo, context, now);

		// assembly
		try {
			cVo.assembly(context);
		} catch (Throwable e) {
			log.errorLang(e, "web.servlet.invalid.assembly", requestURI);
			doResponseError(traceContext, context, e);
			return;
		}

		// validate
		String validateId = cVo.getValidate();
		if (null != validateId) {
			try {
				XCOValidate.validate(validateId, (XCO) context.getArg(), true);
			} catch (Throwable e) {
				log.errorLang(e, "web.servlet.invalid.validate", requestURI);
				doResponseError(traceContext, context, e);
				return;
			}
		}

		exec(traceContext, context, cVo);
	}

	private void exec(Object traceContext, RequestContext context, ControllerVo cVo) {
		try {
			if (cVo.isCacheInAop()) {
				// 新的执行顺序
				cVo.before(context);
				boolean cache = cVo.cacheGet(context);
				if (!cache) {
					cVo.exec(context);
					cVo.cachePut(context);
				}
				cVo.after(context);
			} else {
				boolean cache = cVo.cacheGet(context);
				if (!cache) {
					cVo.before(context);
					cVo.exec(context);
					cVo.after(context);
					cVo.cachePut(context);
				}
			}
			// Response
			doResponseSuccess(traceContext, context, cVo);
		} catch (Throwable ex) {
			log.errorLang(ex, "web.servlet.invalid.exec", context.getRequest().getRequestURI());
			doResponseError(traceContext, context, ex);
		}
	}

	/**
	 * 对上下文进行预处理
	 */
	private RequestContext pretreatmentContext(HttpServletRequest request, HttpServletResponse response, RequestTypeEnum requestType) {
		RequestContext context = WebComponent.getInstance().requestContextThreadLocal.get();
		if (null == context) {
			context = new RequestContext(request, response, requestType, false);
		}
		context.init();
		return context;
	}

	private void doResponseError(Object traceContext, RequestContext context, Throwable ex) {
		ControllerVo cVo = context.getControllerVo();
		try {
			ResponseHandler handler = null;
			if (null != cVo) {
				handler = cVo.getResponseHandler();
			}

			if (null != handler) {
				handler.onError(context, ex);
				return;
			}

			if (null != context.getView()) {
				context.getResponse().sendRedirect(WebComponent.getInstance().getErrorRedirectPage());
			}

			defaultResponseHandler.onError(context, ex);

		} catch (Throwable e) {
			log.errorLang(e, "web.servlet.invalid.onError", context.getPath());
		} finally {
			if (context.isInThread()) {
				WebComponent.getInstance().requestContextThreadLocal.remove();
			}
			// 追踪结束
			endTracking(traceContext, context, ex);
		}
	}

	/**
	 * 成功的响应
	 */
	private void doResponseSuccess(Object traceContext, RequestContext context, ControllerVo cVo) {
		HttpServletResponse response = context.getResponse();
		HttpServletRequest  request  = context.getRequest();
		Throwable           ex       = null;
		try {
			CustomResponseHandler crh = context.getCustomResponseHandler();
			if (null != crh) {
				crh.response(context);
				return;
			}
			ResponseHandler handler = null;
			if (null != cVo) {
				handler = cVo.getResponseHandler();
			}
			if (null != handler) {
				handler.onSuccess(context);
				return;
			}
			if (context.isRedirect()) {
				response.sendRedirect(context.getView());
				return;
			}
			if (context.isForward()) {
				RequestDispatcher dispatcher = request.getRequestDispatcher(context.getView());
				dispatcher.forward(request, response);
				return;
			}

			defaultResponseHandler.onSuccess(context);

		} catch (Throwable e) {
			ex = e;
			log.errorLang(e, "web.servlet.invalid.onSuccess", context.getPath());
		} finally {
			if (context.isInThread()) {
				WebComponent.getInstance().requestContextThreadLocal.remove();
			}
			// 追踪结束
			endTracking(traceContext, context, ex);
		}
	}

	private Object startTracking(ControllerVo cVo, RequestContext context, long now) {
		Object traceContext = null;
		RuntimeContext.updateHeader(context.getArg(), TraceManager.TRACE_ORIGIN_WEB);
		TangYuanManager manager = TangYuanManager.getInstance();
		if (null != manager) {
			manager.initTracking(RuntimeContext.get());
			// TODO 控制器的URL需要增加标识。比如C@/a/c
			traceContext = manager.startTracking(cVo.getUrl(), TangYuanServiceType.WEB.getVal(), context.getArg(), TraceManager.EXECUTE_MODE_SYNC, now);
		}
		return traceContext;
	}

	private void endTracking(Object traceContext, RequestContext context, Throwable ex) {
		if (null != traceContext) {
			TangYuanManager manager = TangYuanManager.getInstance();
			manager.endTracking(traceContext, context.getResult(), ex);
		}
		// 清理上下文记录
		RuntimeContext.clean();
	}

	private void checkAll(ControllerVo cVo, String remoteIp) {
		TangYuanManager manager = TangYuanManager.getInstance();
		if (null != manager) {
			manager.checkApp();
			manager.checkAccessControl(cVo.getUrl(), remoteIp);
			manager.checkService(cVo.getUrl());
		}
	}

}
