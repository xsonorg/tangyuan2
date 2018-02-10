package org.xson.tangyuan.aop;

import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.Version;
import org.xson.tangyuan.aop.xml.XmlAopBuilder;

public class AopComponent implements TangYuanComponent {

	private static AopComponent	instance	= new AopComponent();

	private Log					log			= LogFactory.getLog(getClass());

	static {
		// sql 40 40
		// TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "aop", 55, 40));
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "aop"));
	}

	private AopComponent() {
	}

	public static AopComponent getInstance() {
		return instance;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		log.info("config setting success...");
	}

	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("aop component starting, version: " + Version.getVersion());
		// log.info("Start parsing: " + resource);
		XmlAopBuilder xmlBuilder = new XmlAopBuilder();
		xmlBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);
		log.info("aop component successfully.");
	}

	@Override
	public void stop(boolean wait) {
	}

}
