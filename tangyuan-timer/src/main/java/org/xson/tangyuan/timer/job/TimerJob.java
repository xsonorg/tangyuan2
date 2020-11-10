package org.xson.tangyuan.timer.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.xson.common.object.XCO;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.manager.TangYuanManager;
import org.xson.tangyuan.manager.trace.TraceManager;
import org.xson.tangyuan.service.Actuator;
import org.xson.tangyuan.service.runtime.RuntimeContext;
import org.xson.tangyuan.timer.TimerComponent;
import org.xson.tangyuan.timer.xml.vo.TimerVo;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public abstract class TimerJob implements Job {

	abstract Log getLogger();

	private static TangYuanManager tangYuanManager = null;

	static {
		tangYuanManager = TangYuanManager.getInstance();
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		// 检查系统是否关闭中
		if (!TimerComponent.getInstance().isRunning()) {
			getLogger().info("The current system processing is not running.");
			return;
		}

		// 是否单活
		if (!TimerComponent.getInstance().isSingleLive()) {
			getLogger().info("The current project is in a non-single-live state.");
			return;
		}

		// 添加上下文记录
		RuntimeContext.begin(null, TraceManager.TRACE_ORIGIN_TIMER);

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		TimerVo    config  = (TimerVo) dataMap.get("CONFIG");
		getLogger().info(config.getDesc() + ":" + config.getService() + " start");
		CustomJob customJob    = config.getCustomJob();

		// 跟踪相关
		long      now          = System.currentTimeMillis();
		Object    traceContext = null;
		XCO       result       = null;
		Throwable ex           = null;

		try {
			if (null == customJob) {
				String service = config.getService();
				Object arg     = config.getArg(null);

				// 添加跟踪
				traceContext = startTracking(config, now);

				Object retObj = Actuator.execute(service, arg);
				result = TangYuanUtil.retObjToXco(retObj);
				getLogger().info(config.getDesc() + ":" + service + " end. code[" + result.getCode() + "], message[" + result.getMessage() + "]");

			} else {

				// 添加跟踪
				traceContext = startTracking(config, now);

				customJob.execute(config);
				String service = config.getService();
				getLogger().info(config.getDesc() + ":" + service + " end");
			}
		} catch (Throwable e) {
			ex = e;
			getLogger().error("ERROR:" + config.getService(), e);
		}

		// 记录
		endTracking(traceContext, result, ex);

		// 清理上下文记录
		RuntimeContext.clean();
	}

	private String getServiceName(TimerVo config) {
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

	/**
	 * 开始服务监控跟踪
	 */
	private Object startTracking(TimerVo config, long now) {
		if (null != tangYuanManager) {
			String  serviceKey  = getServiceName(config);
			Integer serviceType = TangYuanServiceType.NO.getVal();
			Integer executeMode = TraceManager.EXECUTE_MODE_SYNC;
			Object  arg         = null;
			return tangYuanManager.startTracking(serviceKey, serviceType, arg, executeMode, now);
		}
		return null;
	}

	/**
	 * 结束服务监控跟踪
	 */
	private void endTracking(Object traceContext, Object result, Throwable ex) {
		if (null != tangYuanManager) {
			tangYuanManager.endTracking(traceContext, result, ex);
		}
	}

	//	private Object startTracking(TimerVo config, Object arg) {
	//		String serviceURI = getServiceName(config);
	//		return RuntimeContext.startTracking(null, serviceURI, arg, TrackingManager.SERVICE_TYPE_NO, TrackingManager.RECORD_TYPE_TIMER, TrackingManager.EXECUTE_MODE_SYNC, null);
	//	}
}
