package org.xson.tangyuan.java;

import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.java.executor.JavaServiceContextFactory;
import org.xson.tangyuan.java.xml.XmlConfigBuilder;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class JavaComponent implements TangYuanComponent {

	private static JavaComponent	instance	= new JavaComponent();

	private Log						log			= LogFactory.getLog(getClass());

	static {
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.JAVA, new JavaServiceContextFactory());
		// TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "java", 40, 40));
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "java"));
	}

	private JavaComponent() {
	}

	public static JavaComponent getInstance() {
		return instance;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		log.info("config setting success...");
	}

	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("java component starting, version: " + Version.getVersion());
		XmlConfigBuilder xmlBuilder = new XmlConfigBuilder();
		xmlBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);
		log.info("java component successfully.");
	}

	@Override
	public void stop(boolean wait) {
		log.info("java component stop successfully.");
	}

}
