package org.xson.tangyuan.trace;

public interface TrackingReporter {

	void start(TrackingConfig config) throws Throwable;

	void stop();

	void report(Object data);
}
