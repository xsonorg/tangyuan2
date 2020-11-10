package org.xson.tangyuan.manager;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.manager.access.AccessControlManager;
import org.xson.tangyuan.manager.app.AppManager;
import org.xson.tangyuan.manager.conf.ConfigManager;
import org.xson.tangyuan.manager.monitor.MonitorManager;
import org.xson.tangyuan.manager.service.ServiceManager;
import org.xson.tangyuan.manager.trace.TraceContext;
import org.xson.tangyuan.manager.trace.TraceManager;
import org.xson.tangyuan.service.runtime.RuntimeContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

/**
 * 管理模块总入口
 */
public class TangYuanManager {

	private static TangYuanManager     instance        = null;

	private Log                        log             = LogFactory.getLog(TangYuanManager.class);

	private final AppManager           appManager;
	private final ServiceManager       serviceManager;
	private final AccessControlManager accessControlManager;
	private final MonitorManager       monitorManager;
	private final TraceManager         traceManager;

	private ManagerClient              managerClient   = null;
	private ManagerListener            managerListener = null;

	private TangYuanManager(ManagerLauncherContext mlc, AppManager appManager, ServiceManager serviceManager, AccessControlManager accessControlManager,
			MonitorManager monitorManager, TraceManager traceManager, ConfigManager configManager) {

		this.appManager = appManager;
		this.serviceManager = serviceManager;
		this.accessControlManager = accessControlManager;
		this.monitorManager = monitorManager;
		this.traceManager = traceManager;

		this.managerClient = new ManagerClient(null, mlc, appManager, serviceManager, configManager);
		this.managerListener = new ManagerListener(null, this.managerClient);
	}

	public static void init(ManagerLauncherContext mlc, AppManager appManager, ServiceManager serviceManager, AccessControlManager accessControlManager,
			MonitorManager monitorManager, TraceManager traceManager, ConfigManager configManager) {
		if (null != instance) {
			instance = new TangYuanManager(mlc, appManager, serviceManager, accessControlManager, monitorManager, traceManager, configManager);
		}
	}

	public static TangYuanManager getInstance() {
		return instance;
	}

	public void start() {
		// 还需要重设数据
		if (null != this.monitorManager) {
			this.monitorManager.start();
		}
		if (null != this.traceManager) {
			this.traceManager.start();
		}
		this.managerClient.start();
		this.managerListener.start();
	}

	public void stop() {
		this.managerListener.stop();
		this.managerClient.stop();
		if (null != this.monitorManager) {
			this.monitorManager.stop();
		}
		if (null != this.traceManager) {
			this.traceManager.stop();
		}
	}

	// ====================================accessControl====================================

	public void checkAccessControl(String service, String remoteIp) {
		if (null == this.accessControlManager) {
			return;
		}

		boolean result = false;
		try {
			XCO    header       = RuntimeContext.get().getHeader();
			String remoteDomain = null;
			if (null != header) {
				// TODO
				remoteDomain = header.getStringValue("domain");
			}
			result = this.accessControlManager.check(service, remoteIp, remoteDomain);
		} catch (Throwable e) {
			log.error(e);
		}
		if (result) {
			throw new ServiceException(ManagerComponent.getInstance().getAccessErrorCode(), ManagerComponent.getInstance().getAccessErrorMessage());
		}

	}

	public void checkService(String service) {
		if (null == this.serviceManager) {
			return;
		}
		if (!this.serviceManager.isAccessed(service)) {
			throw new ServiceException(ManagerComponent.getInstance().getServiceErrorCode(), ManagerComponent.getInstance().getServiceErrorMessage());
		}
	}

	public void checkApp() {
		if (null == this.appManager) {
			return;
		}
		if (!this.appManager.isAccessed(null)) {
			throw new ServiceException(ManagerComponent.getInstance().getAppErrorCode(), ManagerComponent.getInstance().getAppErrorMessage());
		}
	}

	// ====================================trace====================================

	public void initTracking(RuntimeContext rc) {
		if (null != this.traceManager) {
			XCO parent = this.traceManager.initTracking(rc);
			rc.setTrackingParent(parent);
		}
	}

	public TraceContext startTracking(String serviceKey, Integer serviceType, Object arg, Integer executeMode, long now) {
		if (null != this.traceManager) {
			try {
				// 需要忽略RPC, MQ
				if (TangYuanServiceType.PRCPROXY.getVal() == serviceType || TangYuanServiceType.MQ.getVal() == serviceType) {
					return null;
				}
				RuntimeContext rc = RuntimeContext.get();
				if (null == rc) {
					// 不能重新 initTracking
					return null;
				}
				TraceContext context = null;
				XCO          parent  = rc.getTrackingParent();
				if (null == parent) {
					return null;
				}
				//			XCO          current = this.traceManager.startTracking(parent, service, arg, executeMode, now);
				XCO current = this.traceManager.startTracking(parent, serviceKey, serviceType, arg, executeMode, now);
				rc.setTrackingParent(current);
				context = new TraceContext(parent, current);
				return context;
			} catch (Throwable e) {
				log.error(e);
			}
		}
		return null;
	}

	public void appendTrackingCommand(Object command) {
		if (null != this.traceManager) {
			try {
				RuntimeContext rc = RuntimeContext.get();
				if (null == rc) {
					// 不能重新 initTracking
					return;
				}
				XCO current = rc.getTrackingParent();
				if (null == current) {
					return;
				}
				this.traceManager.appendTrackingCommand(current, command);
			} catch (Throwable e) {
				log.error(e);
			}
		}
	}

	public void endTracking(Object context, Object result, Throwable ex) {
		if (null != this.traceManager) {
			try {
				if (null == context) {
					return;
				}
				TraceContext traceContext = (TraceContext) context;
				XCO          current      = traceContext.getCurrent();
				this.traceManager.endTracking(current, result, ex);
				RuntimeContext rc = RuntimeContext.get();
				if (null == rc) {
					return;
				}
				rc.setTrackingParent(traceContext.getParent());
			} catch (Throwable e) {
				log.error(e);
			}
		}
	}

	/**
	 * 在参数中追加Header
	 * @param arg
	 * @return
	 */
	public boolean appendTrackingHeader(Object arg) {
		boolean result = false;
		try {
			if (!(arg instanceof XCO)) {
				return result;
			}
			RuntimeContext rc = RuntimeContext.get();
			if (null == rc) {
				return result;
			}
			XCO data = rc.getData();
			if (null == data) {
				return result;
			}
			// 1. 生成header
			XCO header = new XCO();
			// set header: runtime
			header.setStringValue(RuntimeContext.HEADER_KEY_TRACE_ID, data.getStringValue(RuntimeContext.HEADER_KEY_TRACE_ID));
			header.setStringValue(RuntimeContext.HEADER_KEY_ORIGIN, data.getStringValue(RuntimeContext.HEADER_KEY_ORIGIN));
			// 2. 追加trace
			if (null != this.traceManager) {
				XCO parent = rc.getTrackingParent();
				this.traceManager.appendHeader(parent, header);
			}
			// 3. 追加header
			//			argWrapper = ((XCO) arg).clone();
			//			argWrapper.setXCOValue(TangYuanContainer.XCO_HEADER_KEY, header);
			((XCO) arg).setXCOValue(TangYuanContainer.XCO_HEADER_KEY, header);
			result = true;
		} catch (Throwable e) {
			log.error(e);
		}

		//		return argWrapper;
		return result;
	}

	public void cleanAppendTrackingHeader(Object arg) {
		((XCO) arg).remove(TangYuanContainer.XCO_HEADER_KEY);
	}

	//	public Object appendTrackingHeader(Object arg) {
	//		XCO argWrapper = null;
	//		try {
	//			if (!(arg instanceof XCO)) {
	//				return arg;
	//			}
	//			RuntimeContext rc = RuntimeContext.get();
	//			if (null == rc) {
	//				return null;
	//			}
	//			XCO data = rc.getData();
	//			if (null == data) {
	//				return null;
	//			}
	//			// 1. 生成header
	//			XCO header = new XCO();
	//			// set header: runtime
	//			header.setStringValue(RuntimeContext.HEADER_KEY_TRACE_ID, data.getStringValue(RuntimeContext.HEADER_KEY_TRACE_ID));
	//			header.setStringValue(RuntimeContext.HEADER_KEY_ORIGIN, data.getStringValue(RuntimeContext.HEADER_KEY_ORIGIN));
	//			// 2. 追加trace
	//			if (null != this.traceManager) {
	//				XCO parent = rc.getTrackingParent();
	//				this.traceManager.appendHeader(parent, header);
	//			}
	//			// 3. 追加header
	//			argWrapper = ((XCO) arg).clone();
	//			argWrapper.setXCOValue(TangYuanContainer.XCO_HEADER_KEY, header);
	//		} catch (Throwable e) {
	//			log.error(e);
	//		}
	//
	//		return argWrapper;
	//	}

	public boolean isTrackingCommand(int type) {
		if (null != this.traceManager) {
			return this.traceManager.isTrackingCommand(type);
		}
		return false;
	}

	// ====================================Monitor====================================

	public void reportInitialized() {
		if (null != this.monitorManager) {
			// TODO
			// 汇报启动完成
			// 汇报服务
		}
	}

	public void reportClosing() {
		if (null != this.monitorManager) {
			// TODO
		}
	}

	public void reportClosed() {
		if (null != this.monitorManager) {
			// TODO
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////

	//	public static void main(String[] args) {
	//		System.out.println("CLOSING CLOSED".toLowerCase());
	//	}

	//	public void appendTrackingArg(Integer serviceType, Object arg) {
	//		if (null != this.traceManager) {
	//			RuntimeContext rc = RuntimeContext.get();
	//			if (null == rc) {
	//				return;
	//			}
	//			XCO current = rc.getTrackingParent();
	//			if (null == current) {
	//				return;
	//			}
	//			this.traceManager.appendTrackingArg(current, arg);
	//		}
	//	}

	//	private final ConfigManager        configManager;
	//	private ManagerLauncherContext     mlc             = null;
	//	this.mlc = mlc;
	//	this.configManager = configManager;

	//	public boolean checkAccessControl(String service, String remoteIp, String remoteDomain) {
	//		if (null == this.accessControlManager) {
	//			return true;
	//		}
	//		return this.accessControlManager.check(service, remoteIp, remoteDomain);
	//	}

	//	public void appendTrackingHeader(XCO parent, XCO header) {
	//		if (null != this.traceManager) {
	//			this.traceManager.appendHeader(parent, header);
	//		}
	//	}
	//	/**
	//	 * 添加追踪信息到头部
	//	 */
	//	public void setTrackingHeader(XCO header, XCO parent);

	// ====================================trace====================================
	//	public TraceContext startTracking(AbstractServiceNode service, Object arg, Integer executeMode, long now) {
	//		if (null != this.traceManager) {
	//			// 防止空服务
	//			if (null == service) {
	//				service = EmptyServiceNode.instance;
	//			}
	//			// 需要忽略RPC, MQ
	//			if (TangYuanServiceType.PRCPROXY == service.getServiceType() || TangYuanServiceType.MQ == service.getServiceType()) {
	//				return null;
	//			}
	//			RuntimeContext rc = RuntimeContext.get();
	//			if (null == rc) {
	//				//  可以重新 initTracking
	//			}
	//			TraceContext context = null;
	//			XCO          parent  = rc.getTrackingParent();
	//			XCO          current = this.traceManager.startTracking(parent, service, arg, executeMode, now);
	//			rc.setTrackingParent(current);
	//			context = new TraceContext(parent, current);
	//			return context;
	//		}
	//		return null;
	//	}
	//	public void appendTrackingCommand(int type, Object command) {
	//		if (null != this.traceManager) {
	//			if (this.traceManager.isTrackingCommand(type)) {
	//				RuntimeContext rc = RuntimeContext.get();
	//				if (null == rc) {
	//					// 不能重新 initTracking
	//					return;
	//				}
	//				XCO current = rc.getTrackingParent();
	//				if (null == current) {
	//					return;
	//				}
	//				this.traceManager.appendTrackingCommand(current, command);
	//			}
	//		}
	//	}
}
