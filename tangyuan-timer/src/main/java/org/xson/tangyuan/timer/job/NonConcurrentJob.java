package org.xson.tangyuan.timer.job;

import org.quartz.DisallowConcurrentExecution;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;

@DisallowConcurrentExecution
public class NonConcurrentJob extends TimerJob {

	private static Log log = LogFactory.getLog(NonConcurrentJob.class);

	@Override
	public Log getLogger() {
		return log;
	}

}
