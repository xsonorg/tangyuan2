package org.xson.tangyuan.manager;

import java.util.LinkedList;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.app.AppManager;
import org.xson.tangyuan.manager.conf.ConfigManager;
import org.xson.tangyuan.manager.conf.ResourceReloader;
import org.xson.tangyuan.manager.service.ServiceManager;

/**
 * 管理器客户端
 */
public class ManagerClient extends ManagerThreadBase {

	private Log                log            = LogFactory.getLog(getClass());

	private LinkedList<Object> taskQueue      = new LinkedList<Object>();

	private AppManager         appManager     = null;
	private ServiceManager     serviceManager = null;
	private ConfigManager      configManager  = null;

	public ManagerClient(String name, ManagerLauncherContext mlc, AppManager appManager, ServiceManager serviceManager, ConfigManager configManager) {
		if (null == name) {
			name = "-ManagerClient-";
		}
		this.threadName = name;

		this.mlc = mlc;
		this.appManager = appManager;
		this.serviceManager = serviceManager;
		this.configManager = configManager;
	}

	@Override
	protected void run() {
		while (running) {
			try {
				if (taskQueue.size() > 0) {
					Thread.sleep(1L);
				} else {
					Thread.sleep(interval);
				}
			} catch (InterruptedException e) {
				//				log.error(e);
				break;
			}
			process();
		}
		log.info(TangYuanLang.get("thread.x.ended"), this.threadName);
	}

	public void addTask(Object task) {
		if (running) {
			synchronized (taskQueue) {
				taskQueue.add(task);
			}
		}
	}

	private void process() {
		Object task = null;
		synchronized (taskQueue) {
			task = taskQueue.poll();
		}
		if (null == task) {
			return;
		}
		try {
			XCO    xco      = (XCO) task;
			String taskType = xco.getStringValue("type");
			if ("app".equalsIgnoreCase(taskType)) {
				updateApp(xco);
			} else if ("service".equalsIgnoreCase(taskType)) {
				updateService(xco);
			} else if ("conf".equalsIgnoreCase(taskType)) {
				updateConf(xco);
			} else if ("all".equalsIgnoreCase(taskType)) {
				updateAll(xco);
			} else {
				log.error("未知的任务: " + xco);
			}
		} catch (Throwable e) {
			log.error(e);
		}
	}

	private void updateApp(XCO xco) throws Throwable {
		String url = getURL("sync_app_url");
		if (null == url) {
			return;
		}
		XCO param = new XCO();
		addCommonParam(param);
		XCO data = (XCO) post(url, param.toXMLString());
		if (null == data) {
			return;
		}
		String       version      = data.getStringValue("version");
		XCO          app          = data.getXCOValue("app");
		LocalStorage localStorage = mlc.getLocalStorage();
		if (null != localStorage) {
			localStorage.flushVersion(version);
			localStorage.flushApp(app);
		}
		if (null != this.appManager) {
			//			this.appManager.update(app);
			((ResourceReloader) this.appManager).reload(null);
		}
	}

	private void updateService(XCO xco) throws Throwable {
		String url = getURL("sync_service_url");
		if (null == url) {
			return;
		}
		XCO param = new XCO();
		addCommonParam(param);
		XCO data = (XCO) post(url, param.toXMLString());
		if (null == data) {
			return;
		}
		String       version      = data.getStringValue("version");
		XCO          service      = data.getXCOValue("service");
		LocalStorage localStorage = mlc.getLocalStorage();
		if (null != localStorage) {
			localStorage.flushVersion(version);
			localStorage.flushService(service);
		}
		if (null != this.serviceManager) {
			//			this.serviceManager.update(service);
			((ResourceReloader) this.serviceManager).reload(null);
		}
	}

	private void updateConf(XCO xco) throws Throwable {
		String url = getURL("sync_conf_url");
		if (null == url) {
			return;
		}
		String resource = xco.getStringValue("resource");
		XCO    param    = new XCO();
		addCommonParam(param);
		param.setStringValue("resource", resource);
		XCO data = (XCO) post(url, param.toXMLString());
		if (null == data) {
			return;
		}
		String       version      = data.getStringValue("version");
		String       context      = data.getStringValue("context");
		LocalStorage localStorage = mlc.getLocalStorage();
		if (null != localStorage) {
			// TODO 两个flush是否可以考虑合并到一个方法中
			localStorage.flushVersion(version);
			localStorage.flushConf(resource, context);
		}
		// 重新加载配置文件
		if (null != this.configManager) {
			//			this.configManager.reload(resource, context);
			this.configManager.reload(resource, null);
		}
	}

	private void updateAll(XCO xco) {
		// TODO
	}
}
