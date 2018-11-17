package org.xson.tangyuan.timer;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.runtime.RuntimeContext;
import org.xson.tangyuan.trace.TrackingContext;
import org.xson.tangyuan.trace.TrackingManager;
import org.xson.tangyuan.util.TangYuanUtil;

public abstract class TimerJob implements Job {

	abstract Log getLogger();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		boolean available = TimerComponent.getInstance().isAvailable();
		if (!available) {
			getLogger().info("The current project is in a high availability alternative state.");
			return;
		}

		// 添加上下文记录
		RuntimeContext.beginFromArg(null);

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		TimerConfig config = (TimerConfig) dataMap.get("CONFIG");
		getLogger().info(config.getDesc() + ":" + config.getService() + " start");
		CustomJob customJob = config.getCustomJob();

		TrackingManager trackingManager = TangYuanContainer.getInstance().getTrackingManager();
		Object current = null;
		XCO result = null;
		Throwable ex = null;
		try {
			if (null == customJob) {
				String service = config.getService();
				Object arg = config.getArg(null);

				if (null != trackingManager) {
					Object parent = trackingManager.initTracking(null);
					current = trackingManager.startTracking(parent, getServiceName(config), null, TrackingManager.SERVICE_TYPE_NO,
							TrackingManager.RECORD_TYPE_TIMER, TrackingManager.EXECUTE_MODE_SYNC);
					TrackingContext.setThreadLocalParent(current);
				}

				Object retObj = ServiceActuator.execute(service, arg);
				result = TangYuanUtil.retObjToXco(retObj);
				getLogger().info(config.getDesc() + ":" + service + " end. code[" + result.getCode() + "], message[" + result.getMessage() + "]");
			} else {

				if (null != trackingManager) {
					Object parent = trackingManager.initTracking(null);
					current = trackingManager.startTracking(parent, getServiceName(config), null, TrackingManager.SERVICE_TYPE_NO,
							TrackingManager.RECORD_TYPE_TIMER, TrackingManager.EXECUTE_MODE_SYNC);
					TrackingContext.setThreadLocalParent(current);
				}

				customJob.execute(config);
			}
		} catch (Throwable e) {
			ex = e;
			getLogger().error("ERROR:" + config.getService(), e);
		}

		if (null != trackingManager) {
			trackingManager.endTracking(current, result, ex);
			TrackingContext.cleanThreadLocalParent();
		}

		// 清理上下文记录
		RuntimeContext.clean();
	}

	private String getServiceName(TimerConfig config) {
		String serviceName = config.getDesc();
		if (null == serviceName || 0 == serviceName.length()) {
			serviceName = "TIMER:" + config.getService();
		}
		if (null == serviceName || 0 == serviceName.length()) {
			CustomJob job = config.getCustomJob();
			if (null != job) {
				serviceName = job.getClass().getName();
			}
		}
		return serviceName;
	}
}
