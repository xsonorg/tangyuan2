package org.xson.tangyuan.timer;

import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.Version;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.TangYuanState.ComponentState;
import org.xson.tangyuan.timer.job.JobManager;
import org.xson.tangyuan.timer.xml.XmlTimerComponentBuilder;
import org.xson.tangyuan.timer.xml.XmlTimerContext;
import org.xson.tangyuan.timer.xml.vo.SingleLiveControllerVo;

public class TimerComponent implements TangYuanComponent {

	private static TimerComponent				instance		= new TimerComponent();

	private Log									log				= LogFactory.getLog(getClass());
	private JobManager							jobManager		= null;
	private TimerComponentSingleLiveController	tcslController	= null;

	private volatile ComponentState				state			= ComponentState.UNINITIALIZED;

	static {
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "timer"));
	}

	private TimerComponent() {
	}

	public static TimerComponent getInstance() {
		return instance;
	}

	@Override
	public void config(Map<String, String> properties) {
		log.info(TangYuanLang.get("config.property.load"), "timer-component");
	}

	private void startJob(XmlTimerContext componentContext) throws Throwable {
		this.jobManager = new JobManager();
		this.jobManager.start(componentContext.getTimerList());
	}

	private void startHa(XmlTimerContext componentContext) throws Throwable {
		SingleLiveControllerVo slcVo = componentContext.getSlcVo();
		if (null != slcVo) {
			this.tcslController = slcVo.start();
			// log.info(TangYuanLang.get("instance.start.id"), "singleLiveController", this.tcslController.getClass().getName());
			log.info(TangYuanLang.get("instance.start"), "singleLiveController");
		}
	}

	@Override
	public void start(String resource) throws Throwable {

		log.info(TangYuanLang.get("component.dividing.line"));
		log.info(TangYuanLang.get("component.starting"), "timer", Version.getVersion());

		this.state = ComponentState.INITIALIZING;

		XmlTimerContext componentContext = new XmlTimerContext();
		componentContext.setXmlContext(TangYuanContainer.getInstance().getXmlGlobalContext());

		XmlTimerComponentBuilder xmlBuilder = new XmlTimerComponentBuilder();
		xmlBuilder.parse(componentContext, resource);
		startHa(componentContext);
		startJob(componentContext);
		componentContext.clean();

		// this.running = true;
		this.state = ComponentState.RUNNING;

		log.info(TangYuanLang.get("component.starting.successfully"), "timer");

	}

	@Override
	public void stop(long waitingTime, boolean asyn) {
		// this.running = false;
		this.state = ComponentState.CLOSING;

		log.info(TangYuanLang.get("component.stopping"), "timer");
		if (null != this.jobManager) {
			this.jobManager.stop();
			this.jobManager = null;
		}
		if (null != this.tcslController) {
			this.tcslController.stop();
			this.tcslController = null;
			log.info(TangYuanLang.get("instance.stop"), "singleLiveController");
		}

		this.state = ComponentState.CLOSED;

		log.info(TangYuanLang.get("component.stopping.successfully"), "timer");
	}

	public boolean isRunning() {
		return ComponentState.RUNNING == this.state;
	}

	public boolean isSingleLive() {
		if (null == this.tcslController) {
			return true;
		}
		return this.tcslController.isSingleLive();
	}

}
