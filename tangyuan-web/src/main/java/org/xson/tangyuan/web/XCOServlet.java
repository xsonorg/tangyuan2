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
import org.xson.tangyuan.web.RequestContext.DataFormatEnum;
import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;
import org.xson.tangyuan.web.handler.DefaultJSONResponseHandler;
import org.xson.tangyuan.web.handler.DefaultXCOResponseHandler;
import org.xson.tangyuan.web.util.ServletUtils;
import org.xson.tangyuan.web.xml.ControllerVo;

public class XCOServlet extends HttpServlet {

	private static final long	serialVersionUID	= 1L;
	private static Log			log					= LogFactory.getLog(XCOServlet.class);

	private ResponseHandler		xcoResponseHandler	= new DefaultXCOResponseHandler();
	private ResponseHandler		jsonResponseHandler	= new DefaultJSONResponseHandler();

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
		
		RequestContext context = pretreatmentContext(req, resp, requestType);

		ControllerVo cVo = container.getControllerVo(context.getUrl());
		if (null == cVo) {
			log.error("It does not match the URL: " + context.getUrl());
			context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
			doResponseError(context, null, null);
			return;
		}

		// data convert
		try {
			if (RequestTypeEnum.POST == requestType) {
				context.setArg(ServletUtils.parseArgFromPostRequest(cVo, req, context));
			} else if (RequestTypeEnum.GET == requestType) {// GET
				context.setArg(ServletUtils.parseArgFromGetRequest(cVo, req));
			} else {
				throw new XcoWebException("Unsupported request type: " + requestType);
			}
			log.info(context.getUrl() + " :" + context.getArg());
		} catch (Throwable e) {
			context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
			doResponseError(context, e, "data convert error");
			return;
		}

		// assembly
		try {
			cVo.assembly(context);
		} catch (Throwable e) {
			context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
			doResponseError(context, e, "assembly error");
			return;
		}

		// validate
		String validateId = cVo.getValidate();
		if (null != validateId) {
			try {
				boolean checkResult = XCOValidate.validate(validateId, (XCO) context.getArg());
				if (!checkResult) {
					context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
					doResponseError(context, null, "validate error");
					return;
				}
			} catch (Exception e) {
				if (e instanceof XCOValidateException) {
					XCOValidateException xcoEx = (XCOValidateException) e;
					context.setErrorInfo(xcoEx.getErrorCode(), xcoEx.getErrorMessage());
				} else {
					context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
				}
				doResponseError(context, e, "validate error");
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
				doResponseError(context, ex, "exec error");
			}
		}
	}

	/**
	 * 对上下文进行预处理
	 */
	private RequestContext pretreatmentContext(HttpServletRequest req, HttpServletResponse resp, RequestTypeEnum requestType) {
		RequestContext context = WebComponent.getInstance().requestContextThreadLocal.get();
		if (null == context) { // fix bug
			context = new RequestContext(req, resp, false);
			context.setRequestType(requestType);
		}
		return context;
	}

	private void doResponseError(RequestContext context, Throwable ex, String errorMessage) {
		if (null != ex) {
			log.error(errorMessage + ": " + context.getUrl(), ex);
		}
		try {
			if (context.isAjax()) {
				if (DataFormatEnum.JSON == context.getDataFormat()) {
					jsonResponseHandler.onError(context);
				} else {
					xcoResponseHandler.onError(context);
				}
			} else {
				context.getResponse().sendRedirect(WebComponent.getInstance().getErrorRedirectPage());
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
			if (context.isAjax()) {
				if (DataFormatEnum.JSON == context.getDataFormat()) {
					jsonResponseHandler.onSuccess(context);
				} else {
					xcoResponseHandler.onSuccess(context);
				}
			} else {
				String view = context.getView();
				if (null != view) {
					if (context.isForward()) {
						RequestDispatcher dispatcher = request.getRequestDispatcher(view);
						dispatcher.forward(request, response);
					} else {
						response.sendRedirect(context.getView());
					}
				}
			}
		} catch (Exception e) {
			log.error("doResponseSuccess Error", e);
		} finally {
			if (context.isInThread()) {
				WebComponent.getInstance().requestContextThreadLocal.remove();
			}
		}
	}
}
