package org.xson.tangyuan.tools;

import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;

public class ToolsComponent implements TangYuanComponent {

	private static ToolsComponent	instance	= new ToolsComponent();

	private Log						log			= LogFactory.getLog(getClass());

	static {
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "tools"));
	}

	private ToolsComponent() {
	}

	public static ToolsComponent getInstance() {
		return instance;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		log.info("config setting success...");
	}

	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("tools component starting, version: " + Version.getVersion());
		// log.info("Start parsing: " + resource);
		// XmlConfigBuilder xmlConfigBuilder = new XmlConfigBuilder();
		// xmlConfigBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);
		log.info("tools component successfully.");
	}

	@Override
	public void stop(boolean wait) {
		log.info("tools component stopping...");
		// try {
		// if (null != dataSourceManager) {
		// dataSourceManager.close();
		// }
		// } catch (Throwable e) {
		// log.error(null, e);
		// }
		log.info("tools component stop successfully.");
	}

}
