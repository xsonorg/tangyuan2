package org.xson.tangyuan.es;

import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.es.datasource.EsSourceManager;
import org.xson.tangyuan.es.executor.EsServiceContextFactory;
import org.xson.tangyuan.es.xml.XmlConfigBuilder;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class EsComponent implements TangYuanComponent {

	private static EsComponent	instance	= new EsComponent();

	private Log					log			= LogFactory.getLog(getClass());

	static {
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.ES, new EsServiceContextFactory());
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "es", 40, 40));
	}

	private EsComponent() {
	}

	public static EsComponent getInstance() {
		return instance;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		// log.info("config setting success...");
	}

	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("elasticsearch component starting, version: " + Version.getVersion());
		XmlConfigBuilder xmlBuilder = new XmlConfigBuilder();
		xmlBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);
		EsSourceManager.start();
		log.info("elasticsearch component successfully.");
	}

	@Override
	public void stop(boolean wait) {
		EsSourceManager.stop();
		log.info("elasticsearch component stop successfully.");
	}

}
