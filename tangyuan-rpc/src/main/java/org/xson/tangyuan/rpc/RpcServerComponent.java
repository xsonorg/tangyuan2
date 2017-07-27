package org.xson.tangyuan.rpc;

import java.util.Map;

import org.xson.tangyuan.TangYuanComponent;

/**
 * 后启动, 先关闭
 */
public class RpcServerComponent implements TangYuanComponent {

	private static RpcServerComponent instance = new RpcServerComponent();

	private RpcServerComponent() {
	}

	public static RpcServerComponent getInstance() {
		return instance;
	}

	// private Log log = LogFactory.getLog(getClass());

	@Override
	public void config(Map<String, String> properties) {

	}

	@Override
	public void start(String resource) throws Throwable {
		// log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		// log.info("rpc server component starting, version: " + Version.getVersion());
		// TODO
		// log.info("rpc server component successfully.");
	}

	@Override
	public void stop(boolean wait) {
		// log.info("rpc server component stopping...");
		// TODO
		// log.info("rpc server component stop successfully.");
	}

}
