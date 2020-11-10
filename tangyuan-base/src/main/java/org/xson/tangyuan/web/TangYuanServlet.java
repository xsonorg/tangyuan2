package org.xson.tangyuan.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.manager.ManagerComponent;
import org.xson.tangyuan.manager.TangYuanManager;
import org.xson.tangyuan.service.Actuator;
import org.xson.tangyuan.service.runtime.RuntimeContext;
import org.xson.tangyuan.util.ServletUtil;
import org.xson.tangyuan.util.TangYuanUtil;

public class TangYuanServlet extends HttpServlet {

	private final static long serialVersionUID = 1L;

	private static Log        log              = LogFactory.getLog(TangYuanServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		XCO result = null;
		try {
			String remoteIp = ServletUtil.getIpAddress(req);
			log.info("request to [" + req.getRequestURL() + "], client ip[" + remoteIp + "]");
			XCO arg = ServletUtil.getXCOArg(req);
			log.info("request arg: " + arg);

			// 添加上下文记录
			RuntimeContext.begin(arg);
			// 解析服务名称
			ServiceURI serviceURI = ServiceURI.parse(req);
			// 访问检查
			checkAll(serviceURI, remoteIp);
			// 初始化跟踪记录
			initTracking();
			// 服务调用
			Object retObj = doXcoRpcRquest(serviceURI, arg);
			result = TangYuanUtil.retObjToXco(retObj);
		} catch (Throwable e) {
			log.error("request error: " + req.getRequestURL(), e);
			result = TangYuanUtil.getExceptionResult(e);
		}
		log.info("reponse result: " + result);
		try {
			ServletUtil.reponse(resp, result);
		} catch (Throwable e) {
			log.error("reponse error: " + req.getRequestURL(), e);
		}
		// 清理上下文记录
		RuntimeContext.clean();
	}

	private Object doXcoRpcRquest(ServiceURI serviceURI, final XCO arg) throws Throwable {
		Object result  = null;
		String service = serviceURI.getService();
		String mode    = serviceURI.getMode();
		if (null == mode) {
			result = Actuator.execute(service, arg);
		} else if ("async".equalsIgnoreCase(mode)) {
			Actuator.executeAsync(service, arg);
		} else {
			result = Actuator.execute(service, arg);
		}
		return result;
	}

	private void initTracking() {
		TangYuanManager manager = TangYuanManager.getInstance();
		if (null != manager) {
			manager.initTracking(RuntimeContext.get());
		}
	}

	private void checkAll(ServiceURI serviceURI, String remoteIp) {
		if (!TangYuanContainer.getInstance().isRunning()) {
			throw new ServiceException(ManagerComponent.getInstance().getAppErrorCode(), ManagerComponent.getInstance().getAppErrorMessage());
		}

		TangYuanManager manager = TangYuanManager.getInstance();
		if (null != manager) {
			manager.checkApp();
			manager.checkAccessControl(serviceURI.getService(), remoteIp);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//		if (!TangYuanContainer.getInstance().isRunning()) {
	//			try {
	//				resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The current system is shutting down");
	//			} catch (IOException e) {
	//			}
	//			return;
	//		}
	//	/**
	//	 * 访问控制检测
	//	 */
	//	private boolean checkAccessControl(ServiceURI serviceURI) {
	//		// 1. 组件的状态
	//		// 2. 访问控制规则的检查
	//		boolean         result  = true;
	//		TangYuanManager manager = TangYuanManager.getInstance();
	//		if (null != manager) {
	//			try {
	//				XCO    header       = RuntimeContext.get().getHeader();
	//				String remoteDomain = null;
	//				String remoteIp     = null;
	//				if (null != header) {
	//					remoteDomain = header.getStringValue("domain");
	//					remoteIp = header.getStringValue("ip");// TODO
	//				}
	//				if (!manager.checkAccessControl(serviceURI.getService(), remoteIp, remoteDomain)) {
	//					//				throw new ServiceException(HttpServletResponse.SC_UNAUTHORIZED, "401");
	//					result = false;
	//				}
	//			} catch (Throwable e) {
	//				log.error(e);
	//				result = false;
	//			}
	//		}
	//
	//		return result;
	//	}

	//	private void startRuntimeContext(XCO arg) {
	//		RuntimeContext.begin(arg);
	//		TangYuanManager manager = TangYuanManager.getInstance();
	//		if (null != manager) {
	//			manager.initTracking(RuntimeContext.get());
	//		}
	//	}

	//	private Object doXcoRpcRquest(String serviceURI, final XCO arg) throws Throwable {
	//		Object result = Actuator.execute(serviceURI, arg);
	//		return result;
	//	}

	//	private String parseServiceURI(HttpServletRequest request) {
	//		String uri = request.getRequestURI();
	//		if (null == uri) {
	//			return null;
	//		}
	//		if (uri.startsWith("/")) {
	//			uri = uri.substring(1);
	//		}
	//		int pos = uri.lastIndexOf(".");
	//		if (pos > -1) {
	//			return uri.substring(0, pos);
	//		}
	//		return uri;
	//	}

	//	private Object doXcoRpcRquest(ServiceURI sURI, final XCO arg) throws Throwable {
	//		Object result = null;
	//		if (null == sURI.getMark()) {
	//			result = ServiceActuator.execute(sURI.getQualifiedServiceName(), arg);
	//		} else if ("async".equalsIgnoreCase(sURI.getMark())) {
	//			ServiceActuator.executeAsync(sURI.getQualifiedServiceName(), arg);
	//		} else if ("timer".equalsIgnoreCase(sURI.getMark())) {
	//			ServiceActuator.executeAsync(sURI.getQualifiedServiceName(), arg);
	//		} else {
	//			throw new TangYuanException("Invalid URL[mark]: " + sURI.getOriginal());
	//		}
	//		return result;
	//	}

	// ServiceURI tyURI  = ServiceURI.parseUrlPath(req.getRequestURI());
	// String serviceURI = parseServiceURI(req);
	// Object     retObj = doXcoRpcRquest(serviceURI, arg);

	//		if (!TangYuanContainer.getInstance().isRunning()) {
	//			try {
	//				resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The current system is shutting down");
	//			} catch (IOException e) {
	//			}
	//			return;
	//		}
	//		boolean accessDenied = false;
	//			if (checkAll()(serviceURI)) {
	//			} else {
	//				accessDenied = true;
	//			}
	//		if (accessDenied) {
	//			try {
	//				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "401");
	//			} catch (IOException e) {
	//			}
	//		} else {
	//
	//		}

}
