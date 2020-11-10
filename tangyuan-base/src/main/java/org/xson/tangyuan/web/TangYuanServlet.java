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

	private final static long	serialVersionUID	= 1L;

	private static Log			log					= LogFactory.getLog(TangYuanServlet.class);

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
		Object result = null;
		String service = serviceURI.getService();
		String mode = serviceURI.getMode();
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

}
