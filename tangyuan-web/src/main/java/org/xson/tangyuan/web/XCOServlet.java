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

		String remoteIp = ServletUtil.getIpAddress(req);//TODO 待优化　多个IP

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

	///////////////////////////////////////////////////////////////////////////////////////

	//	private void doResponseError(Object traceContext, RequestContext context, Throwable ex, String errorMessage) {
	//		// log.error(TangYuanLang.get(errorMessage, context.getPath()), ex);
	//		// log.errorLang(ex, errorMessage, context.getPath());
	//		try {
	//			ResponseHandler handler = null;
	//			if (null != cVo) {
	//				handler = cVo.getResponseHandler();
	//			}
	//
	//			if (null != handler) {
	//				handler.onError(context, ex);
	//				return;
	//			}
	//
	//			if (null != context.getView()) {
	//				context.getResponse().sendRedirect(WebComponent.getInstance().getErrorRedirectPage());
	//			}
	//
	//			defaultResponseHandler.onError(context, ex);
	//
	//		} catch (IOException e) {
	//			//			log.error("doResponseError Error", e);
	//			log.error(TangYuanLang.get("web.servlet.invalid.onError", context.getPath()), e);
	//		} finally {
	//			if (context.isInThread()) {
	//				WebComponent.getInstance().requestContextThreadLocal.remove();
	//			}
	//			// 追踪结束
	//			endTracking(traceContext, context, ex);
	//		}
	//	}

	//	private void doResponseError(Object traceContext, RequestContext context, ControllerVo cVo, Throwable ex, String errorMessage) {
	//		//		log.error(context.getPath() + ": " + errorMessage, ex);
	//		log.error(TangYuanLang.get(errorMessage, context.getPath()), ex);
	//		try {
	//			ResponseHandler handler = null;
	//			if (null != cVo) {
	//				handler = cVo.getResponseHandler();
	//			}
	//
	//			if (null != handler) {
	//				handler.onError(context, ex);
	//				return;
	//			}
	//
	//			if (null != context.getView()) {
	//				context.getResponse().sendRedirect(WebComponent.getInstance().getErrorRedirectPage());
	//			}
	//
	//			defaultResponseHandler.onError(context, ex);
	//
	//		} catch (IOException e) {
	//			//			log.error("doResponseError Error", e);
	//			log.error(TangYuanLang.get("web.servlet.invalid.onError", context.getPath()), e);
	//		} finally {
	//			if (context.isInThread()) {
	//				WebComponent.getInstance().requestContextThreadLocal.remove();
	//			}
	//			// 追踪结束
	//			endTracking(traceContext, context, ex);
	//		}
	//	}

	//	private void doResponseError(HttpServletRequest request, HttpServletResponse response, ServiceException se, RequestContext context, ControllerVo cVo) {
	//
	//		try {
	//			ResponseHandler handler = null;
	//			if (null != cVo) {
	//				handler = cVo.getResponseHandler();
	//			}
	//			if (null != handler) {
	//				handler.onError(context, se);
	//				return;
	//			}
	//
	//			ServletResponseUtil.responseError(request, response, se);
	//
	//		} catch (Throwable e) {
	//			//			log.error("doResponseError Error", e);
	//			log.error(TangYuanLang.get("web.servlet.invalid.onError", request.getRequestURI()), e);
	//		} finally {
	//			if (null != context && context.isInThread()) {
	//				WebComponent.getInstance().requestContextThreadLocal.remove();
	//			}
	//		}
	//	}

	//	/**
	//	 * 对上下文进行预处理
	//	 */
	//	private RequestContext pretreatmentContext(HttpServletRequest req, HttpServletResponse resp, RequestTypeEnum requestType) {
	//		RequestContext context = WebComponent.getInstance().requestContextThreadLocal.get();
	//		if (null == context) {
	//			//			String path = ServletUtils.parseRequestURI((HttpServletRequest) req);// fix bug
	//			String path = ServletUtil.parseRequestURI((HttpServletRequest) req);
	//			context = new RequestContext(req, resp, false, path);
	//			context.setRequestType(requestType);
	//		}
	//		context.init();
	//		return context;
	//	}
	//		if (!container.isRunning()) {
	//			try {
	//				resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The current system is shutting down.");
	//			} catch (IOException e) {
	//			}
	//			return;
	//		}
	//		// 比对请求类型是否一致, 只有在非REST模式下才需要
	//		if (!container.isRestMode()) {
	//			if (RequestTypeEnum.POST != requestType && RequestTypeEnum.GET != requestType) {
	//				throw new XCOWebException("Unsupported request type: " + requestType);
	//				//  request.getRequestURI() log.error return
	//			}
	//		}
	//		if (null == cVo) {
	//			Throwable ex = new ServiceException(container.getErrorCode(), container.getErrorMessage());
	//			doResponseError(null, context, null, ex, "No matching controller.");
	//			return;
	//		}

	//	private void endTracking(RequestContext context, Throwable ex) {
	//		if (!RuntimeContext.isTracking()) {
	//			return;
	//		}
	//		Object current = context.getAttach().get(TrackingManager.XCO_TRACE_KEY);
	//		if (null == current) {
	//			return;
	//		}
	//
	//		Object result = context.getResult();
	//		if (null == result) {
	//			Integer code = context.getCode();
	//			if (null != code) {
	//				XCO xco = new XCO();
	//				xco.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, code);
	//				xco.setStringValue(TangYuanContainer.XCO_MESSAGE_KEY, context.getMessage());
	//				result = xco;
	//			}
	//		}
	//
	//		RuntimeContext.endTracking(current, result, ex);
	//	}
	//	private void startTracking(RequestContext context) {
	//		//		Object current = RuntimeContext.startTracking(null, context.getPath(), context.getArg(), TrackingManager.SERVICE_TYPE_NO, TrackingManager.RECORD_TYPE_CONTROLLER,
	//		//				TrackingManager.EXECUTE_MODE_SYNC, null);
	//		//		context.getAttach().put(TrackingManager.XCO_TRACE_KEY, current);
	//	}

	// @Override
	// protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	// handler(req, resp, RequestTypeEnum.HEAD);
	// }
	// @Override
	// protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	// handler(req, resp, RequestTypeEnum.OPTIONS);
	// }

	//	private void doResponseError(RequestContext context, ControllerVo cVo, Throwable ex, String errorMessage) {
	//	log.error(context.getPath() + ": " + errorMessage, ex);
	//	try {
	//		ResponseHandler handler = null;
	//		if (null != cVo) {
	//			handler = cVo.getResponseHandler();
	//		}
	//		if (null != handler) {
	//			handler.onError(context, ex);
	//			return;
	//		}
	//
	//		if (context.isViewRequest()) {
	//			context.getResponse().sendRedirect(WebComponent.getInstance().getErrorRedirectPage());
	//		} else {
	//			if (ReturnDataType.XCO == context.getReturnDataType()) {
	//				xcoResponseHandler.onError(context, ex);
	//			} else if (ReturnDataType.JSON == context.getReturnDataType()) {
	//				jsonResponseHandler.onError(context, ex);
	//			} else {
	//				log.error("Unknown return type[" + context.getContextType() + "], uri: " + context.getPath());
	//			}
	//		}
	//	} catch (IOException e) {
	//		log.error("doResponseError Error", e);
	//	} finally {
	//		if (context.isInThread()) {
	//			WebComponent.getInstance().requestContextThreadLocal.remove();
	//		}
	//		// 追踪结束
	//		endTracking(context, ex);
	//		// 清理上下文记录
	//		RuntimeContext.clean();
	//	}
	//}
	//	private ResponseHandler   xcoResponseHandler  = new DefaultXCOResponseHandler();
	//	private ResponseHandler   jsonResponseHandler = new DefaultJSONResponseHandler();

	//	/**
	//	 * 成功的响应
	//	 */
	//	private void doResponseSuccess(RequestContext context, ControllerVo cVo) {
	//		HttpServletResponse response = context.getResponse();
	//		HttpServletRequest  request  = context.getRequest();
	//		try {
	//
	//			ResponseHandler handler = null;
	//			if (null != cVo) {
	//				handler = cVo.getResponseHandler();
	//			}
	//			if (null != handler) {
	//				handler.onSuccess(context);
	//				return;
	//			}
	//
	//			if (context.isViewRequest()) {
	//				String view = context.getView();
	//				if (null != view) {
	//					if (context.isRedirect()) {
	//						response.sendRedirect(view);
	//					} else {
	//						RequestDispatcher dispatcher = request.getRequestDispatcher(view);
	//						dispatcher.forward(request, response);
	//					}
	//				}
	//			} else {
	//				if (ReturnDataType.XCO == context.getReturnDataType()) {
	//					xcoResponseHandler.onSuccess(context);
	//				} else if (ReturnDataType.JSON == context.getReturnDataType()) {
	//					jsonResponseHandler.onSuccess(context);
	//				} else {
	//					log.error("Unknown return type[" + context.getContextType() + "], uri: " + context.getPath());
	//				}
	//			}
	//		} catch (Throwable e) {
	//			log.error("doResponseSuccess Error", e);
	//		} finally {
	//			if (context.isInThread()) {
	//				WebComponent.getInstance().requestContextThreadLocal.remove();
	//			}
	//
	//			// 追踪结束
	//			endTracking(context, null);
	//
	//			// 清理上下文记录
	//			RuntimeContext.clean();
	//		}
	//	}

	//} catch (Throwable ex) {
	//	// ex = e;
	//	//			if (ex instanceof ServiceException) {
	//	//				ServiceException se = (ServiceException) ex;
	//	//				context.setErrorInfo(se.getErrorCode(), se.getErrorMessage());
	//	//			} else {
	//	//				context.setErrorInfo(WebComponent.getInstance().getErrorCode(), WebComponent.getInstance().getErrorMessage());
	//	//			}
	//	doResponseError(traceContext, context, cVo, ex, "exec error.");
	//}

	//		if (container.isclosing()) {
	//			try {
	//				resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The current system is shutting down.");
	//			} catch (IOException e) {
	//			}
	//			return;
	//		}

	//	try {
	//				boolean checkResult = XCOValidate.validate(validateId, (XCO) context.getArg());
	//				if (!checkResult) {
	//					context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
	//					doResponseError(context, cVo, null, "validate error.");
	//					return;
	//				}
	//			} catch (Throwable e) {
	//				if (e instanceof XCOValidateException) {
	//					XCOValidateException xcoEx = (XCOValidateException) e;
	//					context.setErrorInfo(xcoEx.getErrorCode(), xcoEx.getErrorMessage());
	//				} else {
	//					context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
	//				}
	//				doResponseError(context, cVo, e, "validate error.");
	//				return;
	//			}

	//	try {
	//
	//				} catch (Throwable e) {
	//					// 
	//				}

	//			if (ReturnDataType.XCO == context.getReturnDataType()) {
	//				xcoResponseHandler.onSuccess(context);
	//			} else if (ReturnDataType.JSON == context.getReturnDataType()) {
	//				jsonResponseHandler.onSuccess(context);
	//			}

	//	log.error("Unknown return type[" + context.getContextType() + "], uri: " + context.getPath());

	//			if (context.isViewRequest()) {
	//				String view = context.getView();
	//				if (null != view) {
	//					if (context.isRedirect()) {
	//						response.sendRedirect(view);
	//					} else {
	//						RequestDispatcher dispatcher = request.getRequestDispatcher(view);
	//						dispatcher.forward(request, response);
	//					}
	//				}
	//			} else {
	//				if (ReturnDataType.XCO == context.getReturnDataType()) {
	//					xcoResponseHandler.onSuccess(context);
	//				} else if (ReturnDataType.JSON == context.getReturnDataType()) {
	//					jsonResponseHandler.onSuccess(context);
	//				} else {
	//					log.error("Unknown return type[" + context.getContextType() + "], uri: " + context.getPath());
	//				}
	//			}
}
