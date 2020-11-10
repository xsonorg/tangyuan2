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

	private static TimerComponent              instance       = new TimerComponent();

	private Log                                log            = LogFactory.getLog(getClass());
	private JobManager                         jobManager     = null;
	private TimerComponentSingleLiveController tcslController = null;

	private volatile ComponentState            state          = ComponentState.UNINITIALIZED;

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

		//		this.running = true;
		this.state = ComponentState.RUNNING;

		log.info(TangYuanLang.get("component.starting.successfully"), "timer");

	}

	@Override
	public void stop(long waitingTime, boolean asyn) {
		//		this.running = false;
		this.state = ComponentState.CLOSING;

		log.info(TangYuanLang.get("component.stopping"), "timer");
		if (null != this.jobManager) {
			this.jobManager.stop();
			this.jobManager = null;
		}
		if (null != this.tcslController) {
			this.tcslController.stop();
			// log.info(TangYuanLang.get("instance.stop.id"), "singleLiveController", this.tcslController.getClass().getName());
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

	//////////////////////////////////////////////////////////////////////

	//	private volatile boolean                   running        = false;
	//	public boolean isRunning() {
	//		return running;
	//	}
	//	public boolean isAvailable() {
	//		if (null == this.haController) {
	//			return true;
	//		}
	//		return this.haController.isAvailable();
	//	}

	//	@Override
	//	public void start(String resource) throws Throwable {
	//		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	//		log.info("timer client component starting, version: " + Version.getVersion());
	//		log.info("*** Start parsing: " + resource);
	//		parse(resource);// "config.xml"
	//
	//		if (null != haController) {
	//			haController.doStart(haProperties);
	//		}
	//
	//		schedulerfactory = new StdSchedulerFactory();
	//		scheduler = schedulerfactory.getScheduler();
	//		register();
	//		scheduler.start();
	//		running = true;
	//		log.info("timer client component successfully.");
	//	}

	//	private void buildConfigNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//	// <config-property name="A" value="B" />
	//	Map<String, String> configMap = new HashMap<String, String>();
	//	for (XmlNodeWrapper context : contexts) {
	//		String name  = StringUtils.trim(context.getStringAttribute("name"));
	//		String value = StringUtils.trim(context.getStringAttribute("value"));
	//		if (null == name || null == value) {
	//			throw new XmlParseException("<config-property> missing name or value");
	//		}
	//		configMap.put(name.toUpperCase(), value);
	//	}
	//	if (configMap.size() > 0) {
	//		PlaceholderResourceSupport.processMap(configMap);
	//		config(configMap);
	//	}
	//}

	//	@Override
	//	public void stop(boolean wait) {
	//		try {
	//			log.info("timer client component stopping...");
	//			running = false;
	//			if (null != scheduler) {
	//				try {
	//					scheduler.shutdown();
	//				} catch (Throwable e) {
	//					log.error(e);
	//				}
	//				// scheduler.shutdown(wait);
	//			}
	//			if (null != haController) {
	//				haController.doStop();
	//			}
	//			log.info("timer client component stop successfully.");
	//		} catch (Throwable e) {
	//			log.error("timer client component stop error", e);
	//		}
	//	}

	//	private void parse(String resource) throws Throwable {
	//		// InputStream inputStream = Resources.getResourceAsStream(resource);
	//		InputStream    inputStream = ResourceManager.getInputStream(resource, true);
	//		XPathParser    xPathParser = new XPathParser(inputStream);
	//		XmlNodeWrapper root        = xPathParser.evalNode("/timer-component");
	//
	//		buildConfigNodes(root.evalNodes("config-property"));
	//		buildHaNodes(root.evalNodes("ha-config"));
	//		buildTimerNodes(root.evalNodes("timer"));
	//
	//		// timerList = new ArrayList<TimerConfig>();
	//		// for (XmlNodeWrapper node : nodeList) {
	//		// String scheduled =
	//		// StringUtils.trim(node.getStringAttribute("scheduled"));
	//		// String service =
	//		// StringUtils.trim(node.getStringAttribute("service"));
	//		// String desc = StringUtils.trim(node.getStringAttribute("desc"));
	//		// boolean sync = true;
	//		// String _sync = StringUtils.trim(node.getStringAttribute("sync"));
	//		// if (null != _sync) {
	//		// sync = Boolean.parseBoolean(_sync);
	//		// }
	//		// CustomJob customJob = null;
	//		// String custom = StringUtils.trim(node.getStringAttribute("custom"));
	//		// if (null != custom) {
	//		// Class<?> clazz = ClassUtils.forName(custom);
	//		// if (!CustomJob.class.isAssignableFrom(clazz)) {
	//		// throw new TangYuanException("User-defined JOB must implement
	//		// org.xson.timer.client.CustomJob: " + custom);
	//		// }
	//		// customJob = (CustomJob) TangYuanUtil.newInstance(clazz);
	//		// }
	//		//
	//		// if (null == customJob && null == service) {
	//		// throw new TangYuanException("job and service can not be empty");
	//		// }
	//		//
	//		// // 自定义参数
	//		// Map<String, String> propertiesMap = new HashMap<String, String>();
	//		// Map<String, Object> preProperties = null;
	//		// List<XmlNodeWrapper> properties = node.evalNodes("property");
	//		// for (XmlNodeWrapper propertyNode : properties) {
	//		// propertiesMap.put(StringUtils.trim(propertyNode.getStringAttribute("name")),
	//		// StringUtils.trim(propertyNode.getStringAttribute("value")));
	//		// }
	//		// if (propertiesMap.size() > 0) {
	//		// PlaceholderResourceSupport.processMap(propertiesMap);
	//		// preProperties = TimerUtil.parseProperties(propertiesMap);
	//		// } else {
	//		// propertiesMap = null;
	//		// }
	//		//
	//		// TimerConfig config = new TimerConfig(scheduled, service, sync, false,
	//		// desc, customJob, propertiesMap, preProperties);
	//		// timerList.add(config);
	//		// }
	//	}

	//	private void buildHaNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		// 高可用控制器
	//		int size = contexts.size();
	//		if (size > 1) {
	//			throw new XmlParseException("The <ha-config> node can have at most one.");
	//		}
	//		if (size == 0) {
	//			return;
	//		}
	//
	//		XmlNodeWrapper xNode   = contexts.get(0);
	//		String         clazz   = StringUtils.trim(xNode.getStringAttribute("class"));
	//		Class<?>       haClass = ClassUtils.forName(clazz);
	//		this.haController = (TimerHAController) TangYuanUtil.newInstance(haClass);
	//
	//		Map<String, String>  propertiesMap = new HashMap<String, String>();
	//		List<XmlNodeWrapper> properties    = xNode.evalNodes("property");
	//		for (XmlNodeWrapper propertyNode : properties) {
	//			propertiesMap.put(StringUtils.trim(propertyNode.getStringAttribute("name")), StringUtils.trim(propertyNode.getStringAttribute("value")));
	//		}
	//		if (propertiesMap.size() > 0) {
	//			PlaceholderResourceSupport.processMap(propertiesMap);
	//			this.haProperties = TimerUtil.parseProperties(propertiesMap);
	//		}
	//	}

	//	private void buildTimerNodes(List<XmlNodeWrapper> nodeList) throws Throwable {
	//		this.timerList = new ArrayList<TimerConfig>();
	//		for (XmlNodeWrapper node : nodeList) {
	//			String  scheduled = StringUtils.trim(node.getStringAttribute("scheduled"));
	//			String  service   = StringUtils.trim(node.getStringAttribute("service"));
	//			String  desc      = StringUtils.trim(node.getStringAttribute("desc"));
	//			boolean sync      = true;
	//			String  _sync     = StringUtils.trim(node.getStringAttribute("sync"));
	//			if (null != _sync) {
	//				sync = Boolean.parseBoolean(_sync);
	//			}
	//			CustomJob customJob = null;
	//			String    custom    = StringUtils.trim(node.getStringAttribute("custom"));
	//			if (null != custom) {
	//				Class<?> clazz = ClassUtils.forName(custom);
	//				if (!CustomJob.class.isAssignableFrom(clazz)) {
	//					throw new TangYuanException("User-defined JOB must implement org.xson.timer.client.CustomJob: " + custom);
	//				}
	//				customJob = (CustomJob) TangYuanUtil.newInstance(clazz);
	//			}
	//
	//			if (null == customJob && null == service) {
	//				throw new TangYuanException("job and service can not be empty");
	//			}
	//
	//			// 自定义参数
	//			Map<String, String>  propertiesMap = new HashMap<String, String>();
	//			Map<String, Object>  preProperties = null;
	//			List<XmlNodeWrapper> properties    = node.evalNodes("property");
	//			for (XmlNodeWrapper propertyNode : properties) {
	//				propertiesMap.put(StringUtils.trim(propertyNode.getStringAttribute("name")), StringUtils.trim(propertyNode.getStringAttribute("value")));
	//			}
	//			if (propertiesMap.size() > 0) {
	//				PlaceholderResourceSupport.processMap(propertiesMap);
	//				preProperties = TimerUtil.parseProperties(propertiesMap);
	//			} else {
	//				propertiesMap = null;
	//			}
	//
	//			TimerConfig config = new TimerConfig(scheduled, service, sync, false, desc, customJob, propertiesMap, preProperties);
	//			timerList.add(config);
	//		}
	//	}

	//	private void register() throws Throwable {
	//		for (int i = 0; i < timerList.size(); i++) {
	//			TimerConfig          config   = timerList.get(i);
	//			Class<? extends Job> jobClass = config.isSync() ? NonConcurrentJob.class : ConcurrentJob.class;
	//			JobDetail            job      = JobBuilder.newJob(jobClass).withIdentity("JOB" + i, JOB_GROUP_NAME).build();
	//			job.getJobDataMap().put("CONFIG", config);
	//			Trigger trigger = TriggerBuilder.newTrigger().withIdentity("TRIGGER" + i, TRIGGER_GROUP_NAME).withSchedule(CronScheduleBuilder.cronSchedule(config.getScheduled()))
	//					.startNow().build();
	//			scheduler.scheduleJob(job, trigger);
	//			log.info("add timer: " + config.getService());
	//		}
	//	}

	//	private static Log            log                = LogFactory.getLog(TimerComponent.class);
	//	private static String         JOB_GROUP_NAME     = "JOB_GROUP";
	//	private static String         TRIGGER_GROUP_NAME = "TRIGGER_GROUP";
	//	private SchedulerFactory      schedulerfactory = null;
	//	private Scheduler             scheduler        = null;
	//	private List<TimerConfig>     timerList        = null;
	//	private TimerHAController     haController     = null;
	//	private Map<String, Object>   haProperties     = null;
}
