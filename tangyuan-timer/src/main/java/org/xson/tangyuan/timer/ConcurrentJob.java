package org.xson.tangyuan.timer;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;

public class ConcurrentJob extends TimerJob {

	private static Log log = LogFactory.getLog(ConcurrentJob.class);

	@Override
	public Log getLogger() {
		return log;
	}
}
