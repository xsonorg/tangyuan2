package org.xson.tangyuan.manager;

import java.util.Map;

import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.xml.XmlManagerBuilder;

public class ManagerComponent implements TangYuanComponent {

	private static ManagerComponent instance            = new ManagerComponent();

	private Log                     log                 = LogFactory.getLog(getClass());

	private int                     appErrorCode        = -1;
	private String                  appErrorMessage     = "当前应用暂时不能访问";
	private int                     serviceErrorCode    = -1;
	private String                  serviceErrorMessage = "当前服务暂时不能访问";
	private int                     accessErrorCode     = -1;
	private String                  accessErrorMessage  = "访问受限";

	static {
		// TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.SQL, new SqlServiceContextFactory());
		// TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "sql"));
	}

	private ManagerComponent() {
	}

	public static ManagerComponent getInstance() {
		return instance;
	}

	public int getAppErrorCode() {
		return appErrorCode;
	}

	public String getAppErrorMessage() {
		return appErrorMessage;
	}

	public int getServiceErrorCode() {
		return serviceErrorCode;
	}

	public String getServiceErrorMessage() {
		return serviceErrorMessage;
	}

	public int getAccessErrorCode() {
		return accessErrorCode;
	}

	public String getAccessErrorMessage() {
		return accessErrorMessage;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		if (properties.containsKey("appErrorCode".toUpperCase())) {
			this.appErrorCode = Integer.parseInt(properties.get("appErrorCode".toUpperCase()));
		}
		if (properties.containsKey("appErrorMessage".toUpperCase())) {
			this.appErrorMessage = properties.get("appErrorMessage".toUpperCase());
		}

		if (properties.containsKey("serviceErrorCode".toUpperCase())) {
			this.serviceErrorCode = Integer.parseInt(properties.get("serviceErrorCode".toUpperCase()));
		}
		if (properties.containsKey("serviceErrorMessage".toUpperCase())) {
			this.serviceErrorMessage = properties.get("serviceErrorMessage".toUpperCase());
		}

		if (properties.containsKey("accessErrorCode".toUpperCase())) {
			this.accessErrorCode = Integer.parseInt(properties.get("accessErrorCode".toUpperCase()));
		}
		if (properties.containsKey("accessErrorMessage".toUpperCase())) {
			this.accessErrorMessage = properties.get("accessErrorMessage".toUpperCase());
		}

		log.info(TangYuanLang.get("config.property.load"), "tangyuan-manager");
	}

	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info(TangYuanLang.get("component.starting"), "manager");
		XmlManagerBuilder managerBuilder = new XmlManagerBuilder();
		//		XmlSqlContext componentContext = new XmlSqlContext();
		//		componentContext.setXmlContext(TangYuanContainer.getInstance().getXmlGlobalContext());
		managerBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);
		log.info(TangYuanLang.get("component.starting.successfully"), "manager");
	}

	//	@Override
	//	public void stop(boolean wait) {
	//		log.info(TangYuanLang.get("component.stopping"), "manager");
	//		//		try {
	//		//			if (null != dataSourceManager) {
	//		//				dataSourceManager.close();
	//		//			}
	//		//		} catch (Throwable e) {
	//		//			log.error(null, e);
	//		//		}
	//		log.info(TangYuanLang.get("component.stopping.successfully"), "manager");
	//	}

	@Override
	public void stop(long waitingTime, boolean asyn) {
		log.info(TangYuanLang.get("component.stopping"), "manager");
		//		try {
		//			if (null != dataSourceManager) {
		//				dataSourceManager.close();
		//			}
		//		} catch (Throwable e) {
		//			log.error(null, e);
		//		}
		log.info(TangYuanLang.get("component.stopping.successfully"), "manager");
	}
}
