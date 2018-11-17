package org.xson.tangyuan.timer;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;

public class ConcurrentJob extends TimerJob {

	private static Log log = LogFactory.getLog(ConcurrentJob.class);

	@Override
	public Log getLogger() {
		return log;
	}
}
