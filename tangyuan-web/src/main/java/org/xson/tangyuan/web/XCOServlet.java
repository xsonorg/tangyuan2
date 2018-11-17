package org.xson.tangyuan.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.runtime.RuntimeContext;
import org.xson.tangyuan.trace.TrackingContext;
import org.xson.tangyuan.trace.TrackingManager;
import org.xson.tangyuan.validate.XCOValidate;
import org.xson.tangyuan.validate.XCOValidateException;
import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;
import org.xson.tangyuan.web.RequestContext.ReturnDataType;
import org.xson.tangyuan.web.handler.DefaultJSONResponseHandler;
import org.xson.tangyuan.web.handler.DefaultXCOResponseHandler;
import org.xson.tangyuan.web.util.ServletUtils;
import org.xson.tangyuan.web.xml.vo.ControllerVo;

public class XCOServlet extends HttpServlet {

	private static final long	serialVersionUID	= 1L;

	private static Log			log					= LogFactory.getLog(XCOServlet.class);

	private ResponseHandler		xcoResponseHandler	= new DefaultXCOResponseHandler();
	private ResponseHandler		jsonResponseHandler	= new DefaultJSONResponseHandler();

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handler(req, resp, RequestTypeEnum.PUT);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handler(req, resp, RequestTypeEnum.DELETE);
	}

	// @Override
	// protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	// handler(req, resp, RequestTypeEnum.HEAD);
	// }
	// @Override
	// protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	// handler(req, resp, RequestTypeEnum.OPTIONS);
	// }

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handler(req, resp, RequestTypeEnum.GET);
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handler(req, resp, RequestTypeEnum.POST);
	}

	private void handler(HttpServletRequest req, HttpServletResponse resp, RequestTypeEnum requestType) {
		WebComponent container = WebComponent.getInstance();
		if (container.isclosing()) {
			try {
				resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The current system is shutting down.");
			} catch (IOException e) {
			}
			return;
		}

		if (!container.isRestMode()) {
			// 比对请求类型是否一致, 只有在非REST模式下才需要
			if (RequestTypeEnum.POST != requestType && RequestTypeEnum.GET != requestType) {
				throw new XCOWebException("Unsupported request type: " + requestType);
			}
		}

		RequestContext context = pretreatmentContext(req, resp, requestType);

		// 记录追踪信息
		startTracking(context);

		// find controller
		ControllerVo cVo = container.getControllerVo(requestType, context.getPath());
		if (null == cVo) {
			context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
			doResponseError(context, null, null, "No matching controller.");
			return;
		}

		// data convert
		try {
			cVo.dataConvert(context);

			// 添加上下文记录, 之前日志中可能会丢丢失, 但不影响
			RuntimeContext.beginFromArg(context.getArg(), "WEB");

			if (log.isInfoEnabled()) {
				log.info(requestType + " " + context.getPath() + ", arg: " + context.getArg());
			}

			setTracking(context);
		} catch (Throwable e) {
			context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
			doResponseError(context, cVo, e, "data convert error.");
			return;
		}

		// assembly
		try {
			cVo.assembly(context);
		} catch (Throwable e) {
			context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
			doResponseError(context, cVo, e, "assembly error.");
			return;
		}

		// validate
		String validateId = cVo.getValidate();
		if (null != validateId) {
			try {
				boolean checkResult = XCOValidate.validate(validateId, (XCO) context.getArg());
				if (!checkResult) {
					context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
					doResponseError(context, cVo, null, "validate error.");
					return;
				}
			} catch (Throwable e) {
				if (e instanceof XCOValidateException) {
					XCOValidateException xcoEx = (XCOValidateException) e;
					context.setErrorInfo(xcoEx.getErrorCode(), xcoEx.getErrorMessage());
				} else {
					context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
				}
				doResponseError(context, cVo, e, "validate error.");
				return;
			}
		}
		exec(context, cVo);
	}

	private void exec(RequestContext context, ControllerVo cVo) {
		// Throwable ex = null;
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
			doResponseSuccess(context, cVo);
		} catch (Throwable ex) {
			// ex = e;
			if (ex instanceof ServiceException) {
				ServiceException se = (ServiceException) ex;
				context.setErrorInfo(se.getErrorCode(), se.getErrorMessage());
			} else {
				context.setErrorInfo(WebComponent.getInstance().getErrorCode(), WebComponent.getInstance().getErrorMessage());
			}
			doResponseError(context, cVo, ex, "exec error.");
		}
		// finally {
		// if (null == ex) {
		// doResponseSuccess(context, cVo);
		// } else {
		// // context.setErrorInfo(WebComponent.getInstance().getErrorCode(), WebComponent.getInstance().getErrorMessage());
		// // fix bug
		// if (ex instanceof ServiceException) {
		// ServiceException se = (ServiceException) ex;
		// context.setErrorInfo(se.getErrorCode(), se.getErrorMessage());
		// } else {
		// context.setErrorInfo(WebComponent.getInstance().getErrorCode(), WebComponent.getInstance().getErrorMessage());
		// }
		// doResponseError(context, cVo, ex, "exec error.");
		// }
		// }
	}

	/**
	 * 对上下文进行预处理
	 */
	private RequestContext pretreatmentContext(HttpServletRequest req, HttpServletResponse resp, RequestTypeEnum requestType) {
		RequestContext context = WebComponent.getInstance().requestContextThreadLocal.get();
		if (null == context) { // fix bug
			String path = ServletUtils.parseRequestURI((HttpServletRequest) req);
			context = new RequestContext(req, resp, false, path);
			context.setRequestType(requestType);
		}
		context.init();
		return context;
	}

	private void doResponseError(RequestContext context, ControllerVo cVo, Throwable ex, String errorMessage) {
		log.error(context.getPath() + ": " + errorMessage, ex);
		try {
			ResponseHandler handler = null;
			if (null != cVo) {
				handler = cVo.getResponseHandler();
			}
			if (null != handler) {
				handler.onError(context);
				return;
			}

			if (context.isViewRequest()) {
				context.getResponse().sendRedirect(WebComponent.getInstance().getErrorRedirectPage());
			} else {
				if (ReturnDataType.XCO == context.getReturnDataType()) {
					xcoResponseHandler.onError(context);
				} else if (ReturnDataType.JSON == context.getReturnDataType()) {
					jsonResponseHandler.onError(context);
				} else {
					log.error("Unknown return type[" + context.getContextType() + "], uri: " + context.getPath());
				}
			}
		} catch (IOException e) {
			log.error("doResponseError Error", e);
		} finally {
			if (context.isInThread()) {
				WebComponent.getInstance().requestContextThreadLocal.remove();
			}
			endTracking(context, ex);

			// 清理上下文记录
			RuntimeContext.clean();
		}
	}

	private void doResponseSuccess(RequestContext context, ControllerVo cVo) {
		HttpServletResponse response = context.getResponse();
		HttpServletRequest request = context.getRequest();
		try {

			ResponseHandler handler = null;
			if (null != cVo) {
				handler = cVo.getResponseHandler();
			}
			if (null != handler) {
				handler.onSuccess(context);
				return;
			}

			if (context.isViewRequest()) {
				String view = context.getView();
				if (null != view) {
					if (context.isRedirect()) {
						response.sendRedirect(view);
					} else {
						RequestDispatcher dispatcher = request.getRequestDispatcher(view);
						dispatcher.forward(request, response);
					}
				}
			} else {
				if (ReturnDataType.XCO == context.getReturnDataType()) {
					xcoResponseHandler.onSuccess(context);
				} else if (ReturnDataType.JSON == context.getReturnDataType()) {
					jsonResponseHandler.onSuccess(context);
				} else {
					log.error("Unknown return type[" + context.getContextType() + "], uri: " + context.getPath());
				}
			}
		} catch (Throwable e) {
			log.error("doResponseSuccess Error", e);
		} finally {
			if (context.isInThread()) {
				WebComponent.getInstance().requestContextThreadLocal.remove();
			}
			endTracking(context, null);

			// 清理上下文记录
			RuntimeContext.clean();
		}
	}

	private void startTracking(RequestContext context) {
		TrackingManager trackingManager = TangYuanContainer.getInstance().getTrackingManager();
		if (null != trackingManager) {
			Object parent = trackingManager.initTracking(null);
			String headers = ServletUtils.getHttpHeaderContext(context.getRequest());
			Object current = trackingManager.startTracking(parent, context.getPath(), null, headers, TrackingManager.SERVICE_TYPE_NO,
					TrackingManager.RECORD_TYPE_CONTROLLER, TrackingManager.EXECUTE_MODE_SYNC);
			TrackingContext.setThreadLocalParent(current);
		}
	}

	private void setTracking(RequestContext context) {
		TrackingManager trackingManager = TangYuanContainer.getInstance().getTrackingManager();
		if (null != trackingManager) {
			Object current = context.getAttach().get(TrackingManager.XCO_TRACE_KEY);
			if (null == current) {
				return;
			}
			Object arg = context.getArg();
			if (null != arg) {
				trackingManager.setTracking(current, (XCO) arg);
			}
		}
	}

	private void endTracking(RequestContext context, Throwable ex) {
		TrackingManager trackingManager = TangYuanContainer.getInstance().getTrackingManager();
		if (null != trackingManager) {
			Object current = context.getAttach().get(TrackingManager.XCO_TRACE_KEY);
			if (null != current) {
				Object result = context.getResult();
				if (null == result) {
					Integer code = context.getCode();
					if (null != code) {
						XCO xco = new XCO();
						xco.setIntegerValue(TangYuanContainer.XCO_CODE_KEY, code);
						xco.setStringValue(TangYuanContainer.XCO_MESSAGE_KEY, context.getMessage());
						result = xco;
					}
				}
				trackingManager.endTracking(current, result, ex);
			}
			TrackingContext.cleanThreadLocalParent();
		}
	}
}
