package org.xson.tangyuan.timer.job;

import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.timer.xml.vo.TimerVo;
import org.xson.tangyuan.util.CollectionUtils;

public class JobManager {

	private Log              log              = LogFactory.getLog(getClass());
	private SchedulerFactory schedulerfactory = null;
	private Scheduler        scheduler        = null;

	public void start(List<TimerVo> timerList) throws Throwable {

		if (CollectionUtils.isEmpty(timerList)) {
			return;
		}

		String JOB_GROUP_NAME     = "JOB_GROUP";
		String TRIGGER_GROUP_NAME = "TRIGGER_GROUP";

		this.schedulerfactory = new StdSchedulerFactory();
		this.scheduler = schedulerfactory.getScheduler();

		for (int i = 0; i < timerList.size(); i++) {
			TimerVo              vo       = timerList.get(i);
			Class<? extends Job> jobClass = vo.isSync() ? NonConcurrentJob.class : ConcurrentJob.class;
			JobDetail            job      = JobBuilder.newJob(jobClass).withIdentity("JOB" + i, JOB_GROUP_NAME).build();
			job.getJobDataMap().put("CONFIG", vo);
			Trigger trigger = TriggerBuilder.newTrigger().withIdentity("TRIGGER" + i, TRIGGER_GROUP_NAME).withSchedule(CronScheduleBuilder.cronSchedule(vo.getScheduled()))
					.startNow().build();
			scheduler.scheduleJob(job, trigger);

			//			log.info("add timer: " + vo.getService());
			log.info(TangYuanLang.get("add.tag.service", "timer", vo.getService() + "-" + vo.getDesc()));
		}

		this.scheduler.start();
	}

	public void stop() {
		if (null != scheduler) {
			try {
				scheduler.shutdown();
				//	scheduler.shutdown(wait);
			} catch (Throwable e) {
				log.error(e);
			}
		}
	}

}
