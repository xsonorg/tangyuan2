package org.xson.tangyuan.aop;

import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.Version;
import org.xson.tangyuan.aop.xml.XmlAopBuilder;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.TangYuanState.ComponentState;

public class AopComponent implements TangYuanComponent {

	private static AopComponent     instance = new AopComponent();

	private Log                     log      = LogFactory.getLog(getClass());
	private volatile ComponentState state    = ComponentState.UNINITIALIZED;

	static {
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "aop"));
	}

	private AopComponent() {
	}

	public static AopComponent getInstance() {
		return instance;
	}

	public boolean isRunning() {
		return ComponentState.RUNNING == this.state;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
	}

	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info(TangYuanLang.get("component.starting"), "aop", Version.getVersion());
		this.state = ComponentState.INITIALIZING;

		XmlAopBuilder xmlBuilder = new XmlAopBuilder();
		xmlBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);

		this.state = ComponentState.RUNNING;
		log.info(TangYuanLang.get("component.starting.successfully"), "aop");
	}

	@Override
	public void stop(long waitingTime, boolean asyn) {
		this.state = ComponentState.CLOSED;
		log.info(TangYuanLang.get("component.stopping.successfully"), "aop");
	}

}
