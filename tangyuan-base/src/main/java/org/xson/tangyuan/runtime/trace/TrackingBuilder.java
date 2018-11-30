package org.xson.tangyuan.runtime.trace;

import java.util.Properties;

import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.ResourceManager;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;

public class TrackingBuilder {

	public TrackingManager parse(String resource) throws Throwable {

		Properties p = ResourceManager.getProperties(resource, true);
		// 检查是否开启
		String enable = StringUtils.trim(p.getProperty("enable"));
		if (!"true".equalsIgnoreCase(enable)) {
			return null;
		}

		TrackingConfig config = new TrackingConfig();
		config.init(p);

		TrackingReporter reporter = null;
		String reporterName = config.getReporter();
		if (null == reporterName) {
			reporter = new HttpTrackingReporter();
		} else {
			Class<?> reporterClass = ClassUtils.forName(reporterName);
			reporter = (TrackingReporter) TangYuanUtil.newInstance(reporterClass);
		}

		return new DefaultTrackingManager(config, reporter);
	}

}
