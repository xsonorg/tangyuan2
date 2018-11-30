package org.xson.tangyuan.runtime.trace;

import java.util.Properties;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanAssert;

public class TrackingConfig {

	private String	reporter			= null;

	/** 上报地址 */
	private String	host				= null;
	/** 批量上报 */
	private int		reporterBatch		= 1;
	/** 上报间隔(秒) */
	private long	reporterInterval	= 1L * 1000L;
	/** 最后等待时间(秒) */
	private long	lastWaitTime		= 10L * 1000L;

	private String	clientId			= null;

	private boolean	enableSqlTracking	= false;
	private boolean	enableMongoTracking	= false;
	private boolean	enableHbaseTracking	= false;
	private boolean	enableEsTracking	= false;

	public TrackingConfig() {
	}

	public void init(Properties p) throws Throwable {
		this.host = StringUtils.trim(p.getProperty("host"));
		TangYuanAssert.stringEmpty(this.host, "The 'host' attribute is missing in the trace-config configuration file.");

		this.reporter = StringUtils.trim(p.getProperty("reporter"));
		this.clientId = StringUtils.trim(p.getProperty("clientId"));

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

		// report control
		if (p.containsKey("reporter.batch")) {
			this.reporterBatch = Integer.parseInt(p.getProperty("reporter.batch"));
		}
		if (p.containsKey("reporter.interval")) {
			this.reporterInterval = Long.parseLong(p.getProperty("reporter.interval")) * 1000L;
		}
		if (p.containsKey("lastWaitTime")) {
			this.lastWaitTime = Long.parseLong(p.getProperty("lastWaitTime")) * 1000L;
			if (this.lastWaitTime > TangYuanContainer.getInstance().getMaxWaitTimeForShutDown()) {
				this.lastWaitTime = TangYuanContainer.getInstance().getMaxWaitTimeForShutDown();
			}
		}
	}

	public String getReporter() {
		return reporter;
	}

	public String getHost() {
		return host;
	}

	public Integer getReporterBatch() {
		return reporterBatch;
	}

	public Long getReporterInterval() {
		return reporterInterval;
	}

	public Long getLastWaitTime() {
		return lastWaitTime;
	}

	public String getClientId() {
		return clientId;
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

}
