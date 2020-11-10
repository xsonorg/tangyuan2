package org.xson.tangyuan.manager.app;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.LocalStorage;
import org.xson.tangyuan.manager.ManagerLauncherContext;
import org.xson.tangyuan.manager.conf.DefaultResourceReloader;

public class DefaultAppManager extends DefaultResourceReloader implements AppManager {

	private Log          log          = LogFactory.getLog(getClass());
	private LocalStorage localStorage = null;
	private AppManagerVo amVo         = null;

	public DefaultAppManager(ManagerLauncherContext mlc) {
		this.localStorage = mlc.getLocalStorage();
	}

	private void update(AppManagerVo amVo) {
		this.amVo = amVo;
	}

	private AppManagerVo reload0() {
		AppManagerVo vo = null;
		try {
			String xml = this.localStorage.getAppInfo();
			if (null != xml) {
				XCO data = XCO.fromXML(xml);
				vo = new AppManagerVo();
				vo.init(data);
			}
		} catch (Throwable e) {
			log.error(e);
		}
		return vo;
	}

	@Override
	public void reload(String resource) throws Throwable {
		AppManagerVo vo = reload0();
		if (null != vo) {
			update(vo);
			log.info(TangYuanLang.get("resource.reload"), resource);
		}
	}

	@Override
	public void init(String resource) {
		AppManagerVo vo = reload0();
		if (null != vo) {
			update(vo);
		}
	}

	@Override
	public boolean isAccessed(String service) {
		if (null == this.amVo) {
			return true;
		}
		return this.amVo.isAccessed(service);
	}

	////////////////////////////////////////////////////////////////////////////////

	//	private XCO          app          = null;
	//	// 可以不考虑线程安全
	//	private String       state        = null;

	//	public void init(String resource) {
	//		XCO localData = this.localStorage.getLocalData();
	//		if (null == localData) {
	//			return;
	//		}
	//		this.app = localData.getXCOValue("app");
	//		if (null == this.app) {
	//			return;
	//		}
	//		this.state = this.app.getStringValue("state");
	//		if ("RUNNING".equalsIgnoreCase(this.state)) {
	//			this.state = null;
	//		}
	//	}
	//
	//	public void update(XCO data) {
	//		this.app = data;
	//		if (null != data) {
	//			String state = data.getStringValue("state");
	//			if ("RUNNING".equalsIgnoreCase(state)) {
	//				state = null;
	//			}
	//			this.state = state;
	//		}
	//	}
	//
	//	public boolean isAccessed(String service) {
	//		return null == this.state;
	//	}

	//		if (null != this.localStorage) {
	//			this.localStorage.flushApp(data);
	//		}

	//	private ManagerLauncherContext mlc          = null;
	//	public DefaultAppManager(ManagerLauncherContext mlc) {
	//		this.mlc = mlc;
	//	}
	//	public void init(String resource) {
	//		if (null == this.mlc) {
	//			return;
	//		}
	//		this.localStorage = this.mlc.getLocalStorage();
	//		if (null == localStorage) {
	//			return;
	//		}
	//		XCO localData = this.localStorage.getLocalData();
	//		if (null == localData) {
	//			return;
	//		}
	//		this.app = localData.getXCOValue("app");
	//		if (null == this.app) {
	//			return;
	//		}
	//		this.state = this.app.getStringValue("state");
	//		if ("RUNNING".equalsIgnoreCase(this.state)) {
	//			this.state = null;
	//		}
	//	}
}
