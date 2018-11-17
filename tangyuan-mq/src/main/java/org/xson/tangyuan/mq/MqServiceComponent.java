package org.xson.tangyuan.mq;

import java.util.Map;

import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mq.xml.XmlMqBuilder;
import org.xson.tangyuan.mq.xml.node.XMLMqStartBuilder;

public class MqServiceComponent implements TangYuanComponent {

	private static MqServiceComponent	instance	= new MqServiceComponent();

	private Log							log			= LogFactory.getLog(getClass());

	private MqServiceComponent() {
	}

	public static MqServiceComponent getInstance() {
		return instance;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		log.info("config setting success...");
	}

	@Override
	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("mq service component starting, version: " + Version.getVersion());

		XmlMqBuilder xmlConfigBuilder = new XmlMqBuilder();
		xmlConfigBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);

		new XMLMqStartBuilder().parseService();
		log.info("mq service component successfully.");
	}

	@Override
	public void stop(boolean wait) {
		log.info("mq service component stopping...");
		MqContainer.getInstance().getMqSourceManager().close();
		log.info("mq service component stop successfully.");
	}

}
