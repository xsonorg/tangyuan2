package org.xson.tangyuan.manager.monitor;

import java.util.Properties;

public class MonitorVo {
	//	# 汇报批次
	//	reporter.batch=10

	private boolean enableJvmReport;
	private boolean enableServiceReport;
	private long    reporterInterval;

	private boolean getBooleanValue(Properties properties, String key, boolean defaultValue) {
		if (properties.containsKey(key)) {
			return Boolean.parseBoolean(properties.getProperty(key));
		}
		return defaultValue;
	}

	private int getIntValue(Properties properties, String key, int defaultValue) {
		if (properties.containsKey(key)) {
			return Integer.parseInt(properties.getProperty(key));
		}
		return defaultValue;
	}

	protected void init(Properties p) throws Throwable {
		this.enableJvmReport = getBooleanValue(p, "enable.jvm.report", true);
		this.enableServiceReport = getBooleanValue(p, "enable.service.report", true);
		this.reporterInterval = getIntValue(p, "reporter.interval", 10);
	}

	public boolean isEnableJvmReport() {
		return enableJvmReport;
	}

	public boolean isEnableServiceReport() {
		return enableServiceReport;
	}

	public long getReporterInterval() {
		return reporterInterval;
	}

	//	public MonitorVo(boolean enableJvmReport, boolean enableServiceReport, long reporterInterval) {
	//		this.enableJvmReport = enableJvmReport;
	//		this.enableServiceReport = enableServiceReport;
	//		this.reporterInterval = reporterInterval;
	//	}

	//	private MonitorVo getMonitorVo(Properties p) {
	//		boolean   enableJvmReport     = getBooleanValue(p, "enable.jvm.report", true);
	//		boolean   enableServiceReport = getBooleanValue(p, "enable.service.report", true);
	//		long      reporterInterval    = getIntValue(p, "reporter.interval", 10);
	//		MonitorVo monitorVo           = new MonitorVo(enableJvmReport, enableServiceReport, reporterInterval * 1000L);
	//		return monitorVo;
	//	}
}
