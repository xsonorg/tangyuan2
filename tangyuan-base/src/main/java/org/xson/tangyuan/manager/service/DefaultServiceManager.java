package org.xson.tangyuan.manager.service;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.LocalStorage;
import org.xson.tangyuan.manager.ManagerLauncherContext;
import org.xson.tangyuan.manager.conf.DefaultResourceReloader;

/**
 * 　服务控制器(状态): 关于是否开启，空置，在外部判断
 * 
 * 状态是否可以下移到服务，性能会更好
 */
public class DefaultServiceManager extends DefaultResourceReloader implements ServiceManager {

	private Log              log          = LogFactory.getLog(getClass());

	private LocalStorage     localStorage = null;

	private ServiceManagerVo smVo         = null;

	public DefaultServiceManager(ManagerLauncherContext mlc) {
		this.localStorage = mlc.getLocalStorage();
	}

	private void update(ServiceManagerVo smVo) {
		this.smVo = smVo;
	}

	private ServiceManagerVo reload0() {
		ServiceManagerVo smVo = null;
		try {
			String xml = this.localStorage.getServiceInfo();
			if (null != xml) {
				XCO data = XCO.fromXML(xml);
				smVo = new ServiceManagerVo();
				smVo.init(data);
			}
		} catch (Throwable e) {
			log.error(e);
		}
		return smVo;
	}

	@Override
	public void reload(String resource) throws Throwable {
		ServiceManagerVo smVo = reload0();
		if (null != smVo) {
			update(smVo);
			log.info(TangYuanLang.get("resource.reload"), resource);
		}
	}

	@Override
	public void init(String resource) {
		ServiceManagerVo smVo = reload0();
		if (null != smVo) {
			update(smVo);
		}
	}

	@Override
	public boolean isAccessed(String service) {
		if (null == smVo) {
			return true;
		}
		return smVo.isAccessed(service);
	}

	//	private Map<String, Integer> suspendedServices = null;
	//	@Override
	//	public void init(String resource) {
	//		XCO localData = this.localStorage.getLocalData();
	//		if (null == localData) {
	//			return;
	//		}
	//		XCO data = localData.getXCOValue("service");
	//		if (null != data) {
	//			List<String> list = data.getStringListValue("suspendedServices");
	//			if (null != list && list.size() > 0) {
	//				Map<String, Integer> map = new HashMap<String, Integer>();
	//				for (String s : list) {
	//					map.put(s, 1);
	//				}
	//				if (map.size() > 0) {
	//					this.suspendedServices = map;
	//				}
	//			}
	//		}
	//	}

	//	@Override
	//	public void update(XCO data) {
	//		Map<String, Integer> map  = null;
	//		List<String>         list = data.getStringListValue("suspendedServices");
	//		if (null != list && list.size() > 0) {
	//			map = new HashMap<String, Integer>();
	//			for (String s : list) {
	//				map.put(s, 1);
	//			}
	//		}
	//		this.suspendedServices = map;
	//	}

	//	@Override
	//	public boolean isAccessed(String service) {
	//		final Map<String, Integer> suspendedServices = this.suspendedServices;
	//		if (null == suspendedServices) {
	//			return true;
	//		}
	//		return suspendedServices.containsKey(service);
	//	}

	//	@Override
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
	//		XCO data = localData.getXCOValue("service");
	//		if (null != data) {
	//			List<String> list = data.getStringListValue("suspendedServices");
	//			if (null != list && list.size() > 0) {
	//				Map<String, Integer> map = new HashMap<String, Integer>();
	//				for (String s : list) {
	//					map.put(s, 1);
	//				}
	//				if (map.size() > 0) {
	//					this.suspendedServices = map;
	//				}
	//			}
	//		}
	//	}
	//	public DefaultServiceManager(ManagerLauncherContext mlc) {
	//	this.mlc = mlc;
	//}
	//	private ManagerLauncherContext mlc               = null;
	//	@Override
	//	public void update(XCO data) {
	//		List<String> list = data.getStringListValue("suspendedServices");
	//		if (null != list && list.size() > 0) {
	//			Map<String, Integer> map = new HashMap<String, Integer>();
	//			for (String s : list) {
	//				map.put(s, 1);
	//			}
	//			if (map.size() > 0) { // 防止指令重排
	//				this.suspendedServices = map;
	//			} else {
	//				this.suspendedServices = null;
	//			}
	//		}
	//		if (null != this.localStorage) {
	//			this.localStorage.flushService(data);
	//		}
	//	}
	//	if (null != this.localStorage) {
	//			this.localStorage.flushService(data);
	//		}

}
