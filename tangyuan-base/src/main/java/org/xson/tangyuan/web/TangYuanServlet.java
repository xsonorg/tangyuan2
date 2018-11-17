package org.xson.tangyuan.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceURI;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.runtime.RuntimeContext;
import org.xson.tangyuan.util.ServletUtil;
import org.xson.tangyuan.util.TangYuanUtil;

public class TangYuanServlet extends HttpServlet {

	private final static long	serialVersionUID	= 1L;

	private static Log			log					= LogFactory.getLog(TangYuanServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (TangYuanContainer.getInstance().isClosing()) {
			try {
				resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The current system is shutting down");
			} catch (IOException e) {
			}
			return;
		}

		XCO result = null;
		try {
			// log.info("request from [" + req.getRequestURL() + "], client ip[" + ServletUtil.getIpAddress(req) + "]");
			// ServiceURI tyURI = ServiceURI.parseUrlPath(req.getRequestURI());
			// XCO arg = ServletUtil.getXCOArg(req);
			// log.info("request arg: " + arg);

			XCO arg = ServletUtil.getXCOArg(req);
			// 添加上下文记录
			RuntimeContext.beginFromArg(arg);

			log.info("request from [" + req.getRequestURL() + "], client ip[" + ServletUtil.getIpAddress(req) + "]");
			ServiceURI tyURI = ServiceURI.parseUrlPath(req.getRequestURI());
			log.info("request arg: " + arg);

			Object retObj = doXcoRpcRquest(tyURI, arg);
			result = TangYuanUtil.retObjToXco(retObj);
		} catch (Throwable e) {
			log.error("request error: " + req.getRequestURL(), e);
			result = TangYuanUtil.getExceptionResult(e);
		} finally {

			log.info("reponse result: " + result);

			try {
				ServletUtil.reponse(resp, result);
			} catch (Throwable e) {

			}

			// 清理上下文记录
			RuntimeContext.clean();
		}
	}

	private Object doXcoRpcRquest(ServiceURI sURI, final XCO arg) throws Throwable {
		Object result = null;
		if (null == sURI.getMark()) {
			result = ServiceActuator.execute(sURI.getQualifiedServiceName(), arg);
		} else if ("async".equalsIgnoreCase(sURI.getMark())) {
			ServiceActuator.executeAsync(sURI.getQualifiedServiceName(), arg);
		} else if ("timer".equalsIgnoreCase(sURI.getMark())) {
			ServiceActuator.executeAsync(sURI.getQualifiedServiceName(), arg);
		} else {
			throw new TangYuanException("Invalid URL[mark]: " + sURI.getOriginal());
		}
		return result;
	}

}
