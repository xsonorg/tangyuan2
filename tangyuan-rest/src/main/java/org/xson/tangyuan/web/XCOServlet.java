package org.xson.tangyuan.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.executor.ServiceException;
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
				resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The current system is shutting down");
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

		ControllerVo cVo = container.getControllerVo(requestType, context.getPath());
		if (null == cVo) {
			// log.error("It does not match the URL: " + context.getPath());
			// context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
			// doResponseError(context, null, null);
			// return;

			// log.error("It does not match the URL: " + context.getPath());
			context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
			doResponseError(context, null, "No matching controller.");
			return;
		}

		// data convert
		try {
			cVo.dataConvert(context);
			if (log.isInfoEnabled()) {
				log.info(requestType + " " + context.getPath() + ", arg: " + context.getArg());
				// log.info(context.getPath() + " :" + context.getArg());
			}
		} catch (Throwable e) {
			context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
			doResponseError(context, e, "data convert error.");
			return;
		}

		// assembly
		try {
			cVo.assembly(context);
		} catch (Throwable e) {
			context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
			doResponseError(context, e, "assembly error.");
			return;
		}

		// validate
		String validateId = cVo.getValidate();
		if (null != validateId) {
			try {
				boolean checkResult = XCOValidate.validate(validateId, (XCO) context.getArg());
				if (!checkResult) {
					context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
					doResponseError(context, null, "validate error.");
					return;
				}
			} catch (Exception e) {
				if (e instanceof XCOValidateException) {
					XCOValidateException xcoEx = (XCOValidateException) e;
					context.setErrorInfo(xcoEx.getErrorCode(), xcoEx.getErrorMessage());
				} else {
					context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
				}
				doResponseError(context, e, "validate error.");
				return;
			}
		}
		exec(context, cVo);
	}

	private void exec(RequestContext context, ControllerVo cVo) {
		Throwable ex = null;
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

		} catch (Throwable e) {
			ex = e;
		} finally {
			if (null == ex) {
				doResponseSuccess(context);
			} else {
				// context.setErrorInfo(WebComponent.getInstance().getErrorCode(), WebComponent.getInstance().getErrorMessage());
				// fix bug
				if (ex instanceof ServiceException) {
					ServiceException se = (ServiceException) ex;
					context.setErrorInfo(se.getErrorCode(), se.getErrorMessage());
				} else {
					context.setErrorInfo(WebComponent.getInstance().getErrorCode(), WebComponent.getInstance().getErrorMessage());
				}
				doResponseError(context, ex, "exec error.");
			}
		}
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

	private void doResponseError(RequestContext context, Throwable ex, String errorMessage) {
		// if (null != ex) {
		// log.error(errorMessage + ": " + context.getPath(), ex);
		// }
		log.error(context.getPath() + ": " + errorMessage, ex);
		try {
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
		}
	}

	private void doResponseSuccess(RequestContext context) {
		HttpServletResponse response = context.getResponse();
		HttpServletRequest request = context.getRequest();
		try {
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
		}
	}
}
