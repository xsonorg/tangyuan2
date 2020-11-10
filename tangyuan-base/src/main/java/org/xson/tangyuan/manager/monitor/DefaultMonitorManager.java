package org.xson.tangyuan.manager.monitor;

import java.util.Properties;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.ManagerLauncherContext;
import org.xson.tangyuan.manager.conf.DefaultResourceReloader;

/**
 * 监控管理器
 */
public class DefaultMonitorManager extends DefaultResourceReloader implements MonitorManager {

	private Log                    log = LogFactory.getLog(getClass());

	private MonitorReporterThread  t   = null;

	private ManagerLauncherContext mlc = null;

	public DefaultMonitorManager(ManagerLauncherContext mlc) {
		this.mlc = mlc;
	}

	//	@Override
	//	public void init(String resource) throws Throwable {
	//		Properties p         = MixedResourceManager.getProperties(resource, true);
	//		MonitorVo  monitorVo = new MonitorVo();
	//		monitorVo.init(p);
	//		this.t = new MonitorReporterThread(null, mlc, monitorVo);
	//	}
	//
	//	@Override
	//	public void reload(String resource, String context) throws Throwable {
	//		if (null != t) {
	//			Properties p         = getPropertiesForReload(resource, context, true, false);
	//			MonitorVo  monitorVo = new MonitorVo();
	//			monitorVo.init(p);
	//			t.update(monitorVo);
	//		}
	//	}

	@Override
	public void init(String resource) throws Throwable {
		this.t = new MonitorReporterThread(null, mlc);
		reload0(resource);
	}

	@Override
	public void reload(String resource) throws Throwable {
		if (null != t) {
			reload0(resource);
			log.info(TangYuanLang.get("resource.reload"), resource);
		}
	}

	private void reload0(String resource) throws Throwable {
		Properties p         = getPropertiesForReload(resource, null, true, true);
		MonitorVo  monitorVo = new MonitorVo();
		monitorVo.init(p);
		t.update(monitorVo);
	}

	@Override
	public void start() {
		if (null != t) {
			t.start();
		}
	}

	@Override
	public void stop() {
		if (null != t) {
			t.stop();
		}
	}

	@Override
	public void addTask(Object task) {
		if (null != t) {
			t.addTask(task);
		}
	}

}
