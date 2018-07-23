package org.xson.tangyuan.trace;

import java.util.Properties;

import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.ResourceManager;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;

public class DefaultTrackingBuilder implements TrackingBuilder {

	public TrackingManager parse(String resource) throws Throwable {
		Properties p = ResourceManager.getProperties(resource, true);
		// 检查是否开启
		String enable = StringUtils.trim(p.getProperty("enable"));
		if (!"true".equalsIgnoreCase(enable)) {
			return null;
		}

		TrackingConfig config = new TrackingConfig(p);
		config.init();

		String reporterName = StringUtils.trim(p.getProperty("reporter"));
		if (null == reporterName) {
			reporterName = "org.xson.tangyuan.trace.HttpTrackingReporter";
		}
		Class<?> reporterClass = ClassUtils.forName(reporterName);
		TrackingReporter reporter = (TrackingReporter) TangYuanUtil.newInstance(reporterClass);

		//		TrackingReporter reporter = new HttpTrackingReporter();

		return new DefaultTrackingManager(config, reporter);
	}

}
