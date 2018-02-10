package org.xson.tangyuan.timer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlNodeWrapper;

public class TimerComponent implements TangYuanComponent {

	private static Log				log					= LogFactory.getLog(TimerComponent.class);
	private static String			JOB_GROUP_NAME		= "JOB_GROUP";
	private static String			TRIGGER_GROUP_NAME	= "TRIGGER_GROUP";
	private static TimerComponent	instance			= new TimerComponent();

	static {
		// timer 60 20
		// TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "timer", 60, 20));
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "timer"));
	}

	private TimerComponent() {
	}

	public static TimerComponent getInstance() {
		return instance;
	}

	private SchedulerFactory	schedulerfactory	= null;
	private Scheduler			scheduler			= null;
	private List<TimerConfig>	timerList			= null;

	private void parse(String resource) throws Throwable {
		InputStream inputStream = Resources.getResourceAsStream(resource);
		XPathParser xPathParser = new XPathParser(inputStream);
		XmlNodeWrapper root = xPathParser.evalNode("/timer-component");
		List<XmlNodeWrapper> nodeList = root.evalNodes("timer");
		timerList = new ArrayList<TimerConfig>();
		for (XmlNodeWrapper node : nodeList) {
			String scheduled = StringUtils.trim(node.getStringAttribute("scheduled"));
			String service = StringUtils.trim(node.getStringAttribute("service"));
			String desc = StringUtils.trim(node.getStringAttribute("desc"));
			boolean sync = true;
			String _sync = StringUtils.trim(node.getStringAttribute("sync"));
			if (null != _sync) {
				sync = Boolean.parseBoolean(_sync);
			}
			CustomJob customJob = null;
			String custom = StringUtils.trim(node.getStringAttribute("custom"));
			if (null != custom) {
				Class<?> clazz = ClassUtils.forName(custom);
				if (!CustomJob.class.isAssignableFrom(clazz)) {
					throw new TangYuanException("User-defined JOB must implement org.xson.timer.client.CustomJob: " + custom);
				}
				customJob = (CustomJob) TangYuanUtil.newInstance(clazz);
			}

			if (null == customJob && null == service) {
				throw new TangYuanException("job and service can not be empty");
			}
			TimerConfig config = new TimerConfig(scheduled, service, sync, false, desc, customJob);
			timerList.add(config);
		}
	}

	private void register() throws Throwable {
		for (int i = 0; i < timerList.size(); i++) {
			TimerConfig config = timerList.get(i);
			Class<? extends Job> jobClass = config.isSync() ? NonConcurrentJob.class : ConcurrentJob.class;
			JobDetail job = JobBuilder.newJob(jobClass).withIdentity("JOB" + i, JOB_GROUP_NAME).build();
			job.getJobDataMap().put("CONFIG", config);
			Trigger trigger = TriggerBuilder.newTrigger().withIdentity("TRIGGER" + i, TRIGGER_GROUP_NAME)
					.withSchedule(CronScheduleBuilder.cronSchedule(config.getScheduled())).startNow().build();
			scheduler.scheduleJob(job, trigger);
			log.info("add timer: " + config.getService());
		}
	}

	@Override
	public void config(Map<String, String> properties) {
	}

	@Override
	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("timer client component starting, version: " + Version.getVersion());
		log.info("*** Start parsing: " + resource);
		parse(resource);// "config.xml"
		schedulerfactory = new StdSchedulerFactory();
		scheduler = schedulerfactory.getScheduler();
		register();
		scheduler.start();
		log.info("timer client component successfully.");
	}

	@Override
	public void stop(boolean wait) {
		try {
			log.info("timer client component stopping...");
			if (null != scheduler) {
				scheduler.shutdown();
				// scheduler.shutdown(wait);
			}
			log.info("timer client component stop successfully.");
		} catch (Throwable e) {
			log.error("timer client component stop error", e);
		}
	}

}
