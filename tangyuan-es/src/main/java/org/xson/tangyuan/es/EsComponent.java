package org.xson.tangyuan.es;

import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.Version;
import org.xson.tangyuan.es.datasource.EsSourceManager;
import org.xson.tangyuan.es.xml.XmlEsComponentBuilder;
import org.xson.tangyuan.es.xml.XmlEsContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.TangYuanState.ComponentState;
import org.xson.tangyuan.service.context.EsServiceContextFactory;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class EsComponent implements TangYuanComponent {

	private static EsComponent      instance        = new EsComponent();

	private Log                     log             = LogFactory.getLog(getClass());
	private volatile ComponentState state           = ComponentState.UNINITIALIZED;
	private EsSourceManager         esSourceManager = null;

	static {
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.ES, new EsServiceContextFactory());
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "es"));
	}

	private EsComponent() {
	}

	public static EsComponent getInstance() {
		return instance;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		//		log.info(TangYuanLang.get("config.property.load"), "elasticsearch-component");
	}

	public EsSourceManager getEsSourceManager() {
		return esSourceManager;
	}

	public void setEsSourceManager(EsSourceManager esSourceManager) {
		this.esSourceManager = esSourceManager;
	}

	public boolean isRunning() {
		return ComponentState.RUNNING == this.state;
	}

	public void start(String resource) throws Throwable {
		log.info(TangYuanLang.get("component.dividing.line"));
		log.info(TangYuanLang.get("component.starting"), "elasticsearch", Version.getVersion());

		this.state = ComponentState.INITIALIZING;

		XmlEsContext componentContext = new XmlEsContext();
		componentContext.setXmlContext(TangYuanContainer.getInstance().getXmlGlobalContext());

		XmlEsComponentBuilder builder = new XmlEsComponentBuilder();
		builder.parse(componentContext, resource);
		componentContext.clean();

		if (null != this.esSourceManager) {
			this.esSourceManager.start();
		}

		this.state = ComponentState.RUNNING;

		log.info(TangYuanLang.get("component.starting.successfully"), "elasticsearch");
	}

	@Override
	public void stop(long waitingTime, boolean asyn) {
		log.info(TangYuanLang.get("component.stopping"), "elasticsearch");

		this.state = ComponentState.CLOSING;

		try {
			if (null != this.esSourceManager) {
				this.esSourceManager.stop();
			}
		} catch (Throwable e) {
			log.error(e);
		}

		this.state = ComponentState.CLOSED;

		log.info(TangYuanLang.get("component.stopping.successfully"), "elasticsearch");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	// log.info("config setting success...");
	// <config-property name="http.client.resource" value="http.client.properties"/>
	// if (properties.containsKey("http.client.resource".toUpperCase())) {
	// this.httpClientResource = StringUtils.trim(properties.get("http.client.resource".toUpperCase()));
	// }
	//	@Override
	//	public void stop(boolean wait) {
	//		EsSourceManager.stop();
	//		log.info("elasticsearch component stop successfully.");
	//	}
	// public String getHttpClientResource() {
	// return httpClientResource;
	// }
	//	public void start(String resource) throws Throwable {
	//	log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	//	log.info("elasticsearch component starting, version: " + Version.getVersion());
	//	XmlConfigBuilder xmlBuilder = new XmlConfigBuilder();
	//	xmlBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);
	//	EsSourceManager.start();
	//	log.info("elasticsearch component successfully.");
	//}
	//		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	//		log.info("elasticsearch component starting, version: " + Version.getVersion());
	//		XmlConfigBuilder xmlBuilder = new XmlConfigBuilder();
	//		xmlBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);
	//		EsSourceManager.start();
	//		log.info("elasticsearch component successfully.");

}
