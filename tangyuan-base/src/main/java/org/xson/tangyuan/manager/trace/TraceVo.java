package org.xson.tangyuan.manager.trace;

import java.util.Properties;

import org.xson.tangyuan.TangYuanException;

public class TraceVo {

	/** 批量上报 */
	private int     reporterBatch        = 1;
	/** 上报间隔(秒) */
	private long    reporterInterval     = 1L * 1000L;
	/** 最后等待时间(秒) */
	private long    lastWaitTime         = 10L * 1000L;

	private boolean enableSqlTracking    = false;
	private boolean enableMongoTracking  = false;
	private boolean enableHbaseTracking  = false;
	private boolean enableEsTracking     = false;
	private boolean enableResultTracking = true;

	//	public TraceVo() {
	//	}
	//
	//	public void init(XCO data) throws Throwable {
	//		// TODO
	//		// 1. data-->context
	//		// 2. context-->Properties
	//		String     context = "";
	//		Properties p       = new Properties();
	//		if (null != context) {
	//			p.load(new ByteArrayInputStream(context.getBytes(StandardCharsets.UTF_8)));
	//		}
	//		init(p);
	//	}

	public void init(Properties p) throws Throwable {

		if (null == p) {
			throw new TangYuanException("");
			// TODO
		}

		// enable command
		if (p.containsKey("enable.sql.tracking")) {
			this.enableSqlTracking = Boolean.parseBoolean(p.getProperty("enable.sql.tracking"));
		}
		if (p.containsKey("enable.mongo.tracking")) {
			this.enableMongoTracking = Boolean.parseBoolean(p.getProperty("enable.mongo.tracking"));
		}
		if (p.containsKey("enable.hbase.tracking")) {
			this.enableHbaseTracking = Boolean.parseBoolean(p.getProperty("enable.hbase.tracking"));
		}
		if (p.containsKey("enable.es.tracking")) {
			this.enableEsTracking = Boolean.parseBoolean(p.getProperty("enable.es.tracking"));
		}
		if (p.containsKey("enable.result.tracking")) {
			this.enableResultTracking = Boolean.parseBoolean(p.getProperty("enable.result.tracking"));
		}

		// report control
		if (p.containsKey("reporter.batch")) {
			this.reporterBatch = Integer.parseInt(p.getProperty("reporter.batch"));
		}
		if (p.containsKey("reporter.interval")) {
			this.reporterInterval = Long.parseLong(p.getProperty("reporter.interval")) * 1000L;
		}
		if (p.containsKey("lastWaitTime")) {
			this.lastWaitTime = Long.parseLong(p.getProperty("lastWaitTime")) * 1000L;
		}
	}

	public int getReporterBatch() {
		return reporterBatch;
	}

	public long getReporterInterval() {
		return reporterInterval;
	}

	public long getLastWaitTime() {
		return lastWaitTime;
	}

	public boolean isEnableSqlTracking() {
		return enableSqlTracking;
	}

	public boolean isEnableMongoTracking() {
		return enableMongoTracking;
	}

	public boolean isEnableHbaseTracking() {
		return enableHbaseTracking;
	}

	public boolean isEnableEsTracking() {
		return enableEsTracking;
	}

	public boolean isEnableResultTracking() {
		return enableResultTracking;
	}

}
