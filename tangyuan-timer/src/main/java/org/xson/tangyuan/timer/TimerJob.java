package org.xson.tangyuan.timer;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.util.TangYuanUtil;

public abstract class TimerJob implements Job {

	abstract Log getLogger();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		TimerConfig config = (TimerConfig) dataMap.get("CONFIG");
		getLogger().info(config.getDesc() + ":" + config.getService() + " start");
		CustomJob customJob = config.getCustomJob();
		try {
			if (null == customJob) {
				final String service = config.getService();
				// Object retObj = ServiceActuator.execute(config.getRealService(), new XCO());
				Object retObj = ServiceActuator.execute(service, new XCO());
				XCO result = TangYuanUtil.retObjToXco(retObj);
				getLogger().info(config.getDesc() + ":" + service + " end. code[" + result.getCode() + "], message[" + result.getMessage() + "]");
			} else {
				customJob.execute(config);
			}
		} catch (Throwable e) {
			getLogger().error("ERROR:" + config.getService(), e);
		}
	}

}
