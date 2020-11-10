package org.xson.tangyuan.timer.job;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;

public class ConcurrentJob extends TimerJob {

	private static Log log = LogFactory.getLog(ConcurrentJob.class);

	@Override
	public Log getLogger() {
		return log;
	}
}
