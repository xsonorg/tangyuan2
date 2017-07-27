package org.xson.tangyuan.monitor;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.util.DateUtils;

/**
 * 服务监控线程
 */
public class ServiceDeadlockMonitor {

	private Log									log				= LogFactory.getLog(ServiceDeadlockMonitor.class);
	private Map<Integer, ServiceDeadlockInfo>	contextInfoMap	= null;
	private volatile boolean					running			= false;
	private InnerMonitorThread					thread			= null;
	private DeadlockMonitorWriter				writer			= null;

	public ServiceDeadlockMonitor(DeadlockMonitorWriter writer) {
		if (null != writer) {
			this.writer = writer;
		} else {
			this.writer = new DeadlockMonitorWriter() {
				@Override
				public void writer(StringBuilder builder) {
					log.info(builder.toString());
				}
			};
		}
	}

	private class InnerMonitorThread extends Thread {

		protected InnerMonitorThread() {
			super("InnerMonitorThread");
			setDaemon(true);
		}

		@Override
		public void run() {
			while (running) {
				StringBuilder builder = new StringBuilder();
				builder.append("Monitor at: " + DateUtils.getDateTimeString(new Date()));
				builder.append("----------------------------\n");
				Set<Map.Entry<Integer, ServiceDeadlockInfo>> set = contextInfoMap.entrySet();
				for (Map.Entry<Integer, ServiceDeadlockInfo> entry : set) {
					ServiceDeadlockInfo contextInfo = entry.getValue();
					String info = contextInfo.toString();
					if (null != info) {
						builder.append(info);
						builder.append("\n");
					}
					if (!contextInfo.isRunning()) {
						contextInfoMap.remove(entry.getKey());
					}
				}
				builder.append("\n");
				// log
				flush(builder);
				try {
					sleep(TangYuanContainer.getInstance().getDeadlockMonitorSleepTime());
				} catch (InterruptedException e) {
					log.error(null, e);
					return;
				}
			}
		}
	}

	private void flush(StringBuilder builder) {
		this.writer.writer(builder);
	}

	public void add(ServiceDeadlockInfo contextInfo) {
		contextInfoMap.put(contextInfo.hashCode(), contextInfo);
	}

	public void start() {
		if (!running) {
			running = true;
			contextInfoMap = new ConcurrentHashMap<Integer, ServiceDeadlockInfo>();
			thread = new InnerMonitorThread();
			thread.start();
			log.info("InnerMonitorThread start...");
		}
	}

	public void stop() {
		if (running) {
			running = false;
			log.info("InnerMonitorThread stop...");
		}
	}

}
