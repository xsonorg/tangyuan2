package org.xson.tangyuan.timer;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.runtime.RuntimeContext;
import org.xson.tangyuan.runtime.trace.TrackingManager;
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
		RuntimeContext.beginFromArg(null, "TIMER");

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		TimerConfig config = (TimerConfig) dataMap.get("CONFIG");
		getLogger().info(config.getDesc() + ":" + config.getService() + " start");
		CustomJob customJob = config.getCustomJob();

		// 跟踪相关
		Object current = null;
		boolean tracking = RuntimeContext.isTracking();

		XCO result = null;
		Throwable ex = null;

		try {
			if (null == customJob) {
				String service = config.getService();
				Object arg = config.getArg(null);

				if (tracking) {
					current = startTracking(config, arg);
				}

				Object retObj = ServiceActuator.execute(service, arg);
				result = TangYuanUtil.retObjToXco(retObj);
				getLogger().info(config.getDesc() + ":" + service + " end. code[" + result.getCode() + "], message[" + result.getMessage() + "]");

			} else {

				if (tracking) {
					current = startTracking(config, null);
				}

				customJob.execute(config);
			}
		} catch (Throwable e) {
			ex = e;
			getLogger().error("ERROR:" + config.getService(), e);
		}

		if (tracking) {
			RuntimeContext.endTracking(current, result, ex);
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

	private Object startTracking(TimerConfig config, Object arg) {
		String serviceURI = getServiceName(config);
		return RuntimeContext.startTracking(null, serviceURI, arg, TrackingManager.SERVICE_TYPE_NO, TrackingManager.RECORD_TYPE_TIMER,
				TrackingManager.EXECUTE_MODE_SYNC, null);
	}
}
