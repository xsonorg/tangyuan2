package org.xson.tangyuan.rpc;

import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.rpc.xml.XMLConfigBuilder;

/**
 * 先启动, 后关闭
 */
public class RpcClientComponent implements TangYuanComponent {

	private static RpcClientComponent	instance	= new RpcClientComponent();

	private Log							log			= LogFactory.getLog(getClass());

	private RpcClientComponent() {
	}

	public static RpcClientComponent getInstance() {
		return instance;
	}

	@Override
	public void config(Map<String, String> properties) {
		// RpcContainer.getInstance().config(properties);
	}

	@Override
	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("rpc client component starting, version: " + Version.getVersion());
		XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder();
		xmlConfigBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);
		// TODO
		log.info("rpc client component successfully.");
	}

	@Override
	public void stop(boolean wait) {
		log.info("rpc client component stopping...");
		log.info("rpc client component stop successfully.");
	}

}
