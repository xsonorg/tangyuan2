package org.xson.tangyuan.service.pool;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.util.PropertyUtils;

public class ThreadPool {

	private Log					log			= LogFactory.getLog(ThreadPool.class);
	private volatile boolean	running		= false;
	private ExecutorService		pool		= null;
	private long				maxWaitTime	= 10L;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void start(Properties p, long maxWaitTimeForShutDown) throws Throwable {
		if (!running) {

			// running = true;

			// type:custom/SingleThread/FixedThreadPool/CachedThreadPool
			String type = p.getProperty("type");
			if (null == type) {
				type = "SingleThread";
			}
			if ("SingleThread".equalsIgnoreCase(type)) {
				pool = Executors.newSingleThreadExecutor();
			} else if ("FixedThreadPool".equalsIgnoreCase(type)) {
				int nThreads = PropertyUtils.getIntValue((Map) p, "corePoolSize", 100);
				pool = Executors.newFixedThreadPool(nThreads);
			} else if ("CachedThreadPool".equalsIgnoreCase(type)) {
				pool = Executors.newCachedThreadPool();
			} else if ("custom".equalsIgnoreCase(type)) {
				int corePoolSize = PropertyUtils.getIntValue((Map) p, "corePoolSize", 100);
				int maximumPoolSize = PropertyUtils.getIntValue((Map) p, "maximumPoolSize", 2 * corePoolSize);
				long keepAliveTime = PropertyUtils.getLongValue((Map) p, "keepAliveTime", 60L);
				TimeUnit unit = getTimeUnit((Map) p, "unit", TimeUnit.SECONDS);
				BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
				ThreadFactory threadFactory = Executors.defaultThreadFactory();
				RejectedExecutionHandler handler = new AbortPolicy();
				pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
			}

			this.maxWaitTime = PropertyUtils.getLongValue((Map) p, "maxWaitTimeForShutDown", maxWaitTimeForShutDown);

			running = true;

			log.info("thread pool start successfully.");
		}
	}

	private TimeUnit getTimeUnit(Map<String, String> properties, String key, TimeUnit defaultValue) {
		String unit = PropertyUtils.getStringValue(properties, "unit", "SECONDS");
		if ("SECONDS".equalsIgnoreCase(unit)) {
			return TimeUnit.SECONDS;
		} else if ("MINUTES".equalsIgnoreCase(unit)) {
			return TimeUnit.MINUTES;
		} else if ("HOURS".equalsIgnoreCase(unit)) {
			return TimeUnit.HOURS;
		} else if ("DAYS".equalsIgnoreCase(unit)) {
			return TimeUnit.DAYS;
		}
		throw new TangYuanException("Unsupported time units:" + unit);
	}

	public void stop() {
		if (running) {
			running = false;
			if (null != pool) {
				pool.shutdown(); // Disable new tasks from being submitted
				try {
					// Wait a while for existing tasks to terminate
					if (!pool.awaitTermination(maxWaitTime, TimeUnit.SECONDS)) {
						// Cancel currently executing tasks
						pool.shutdownNow();
						if (!pool.awaitTermination(maxWaitTime, TimeUnit.SECONDS)) {
							log.error("Pool did not terminate");
						}
					}
				} catch (InterruptedException ie) {
					pool.shutdownNow();
					Thread.currentThread().interrupt();
				}
			}
			log.info("thread pool stop successfully.");
		}
	}

	public void execute(Runnable runnable) {
		if (null != pool && running) {
			pool.execute(runnable);
		}
	}
}
