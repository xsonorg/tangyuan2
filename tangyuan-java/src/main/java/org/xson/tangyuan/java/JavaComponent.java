package org.xson.tangyuan.java;

import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.Version;
import org.xson.tangyuan.java.xml.XmlJavaComponentBuilder;
import org.xson.tangyuan.java.xml.XmlJavaContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.TangYuanState.ComponentState;
import org.xson.tangyuan.service.context.JavaServiceContextFactory;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class JavaComponent implements TangYuanComponent {

	private static JavaComponent    instance = new JavaComponent();

	private Log                     log      = LogFactory.getLog(getClass());

	private volatile ComponentState state    = ComponentState.UNINITIALIZED;

	static {
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.JAVA, new JavaServiceContextFactory());
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "java"));
	}

	private JavaComponent() {
	}

	public static JavaComponent getInstance() {
		return instance;
	}

	public boolean isRunning() {
		return ComponentState.RUNNING == this.state;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		//		log.info(TangYuanLang.get("config.property.load"), "java-component");
	}

	public void start(String resource) throws Throwable {
		log.info(TangYuanLang.get("component.dividing.line"));
		log.info(TangYuanLang.get("component.starting"), "java", Version.getVersion());

		this.state = ComponentState.INITIALIZING;

		TangYuanLang.getInstance().load("tangyuan-lang-java");

		XmlJavaContext componentContext = new XmlJavaContext();
		componentContext.setXmlContext(TangYuanContainer.getInstance().getXmlGlobalContext());

		XmlJavaComponentBuilder xmlBuilder = new XmlJavaComponentBuilder();
		xmlBuilder.parse(componentContext, resource);
		componentContext.clean();

		this.state = ComponentState.RUNNING;

		log.info(TangYuanLang.get("component.starting.successfully"), "java");
	}

	@Override
	public void stop(long waitingTime, boolean asyn) {
		this.state = ComponentState.CLOSED;
		log.info(TangYuanLang.get("component.stopping.successfully"), "java");
	}

}
