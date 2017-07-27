package org.xson.tangyuan.timer;

import org.quartz.DisallowConcurrentExecution;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;

@DisallowConcurrentExecution
public class NonConcurrentJob extends TimerJob {

	private static Log log = LogFactory.getLog(NonConcurrentJob.class);

	@Override
	public Log getLogger() {
		return log;
	}

}
