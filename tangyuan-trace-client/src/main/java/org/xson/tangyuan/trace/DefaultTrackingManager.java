package org.xson.tangyuan.trace;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.xson.common.object.XCO;
import org.xson.tangyuan.app.AppProperty;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.util.TangYuanUtil;

public class DefaultTrackingManager implements TrackingManager {

	private TrackingConfig		config		= null;

	private TrackingReporter	reporter	= null;

	public DefaultTrackingManager(TrackingConfig config, TrackingReporter reporter) {
		this.config = config;
		this.reporter = reporter;
	}

	@Override
	public void start() throws Throwable {
		this.reporter.start(this.config);
	}

	@Override
	public void stop() {
		this.reporter.stop();
	}

	private String createSpanId() {
		return UUID.randomUUID().toString();
	}

	private String getExceptionInfo(Throwable ex) {
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw, true));
		return sw.getBuffer().toString();
	}

	public Object initTracking(Object arg) {
		return initTracking(arg, null);
	}

	@Override
	public Object initTracking(Object arg, Long now) {
		XCO parent = (XCO) TrackingContext.getThreadLocalParent();
		if (null != parent) {
			return parent;
		}

		XCO xco = (XCO) arg;
		//		XCO parent = new XCO();
		if (null == now) {
			now = System.currentTimeMillis();
		}
		if (null != xco && xco.exists(XCO_TRACE_KEY)) {	// 头部中存在跟踪信息
			parent = xco.getXCOValue(XCO_TRACE_KEY);
			int base_order = parent.getIntegerValue("base_order");
			parent.setAttachObject(new AtomicInteger(base_order));
			parent.setLongValue("base_time", now);

			parent.remove("base_order");
			// 清理参数中的更总信息, 將其保存在上下文中
			xco.remove(XCO_TRACE_KEY);
		} else {							// 一个初始化的跟踪信息
			parent = new XCO();
			parent.setStringValue("trace_id", createSpanId());
			parent.setStringValue("span_id", "");
			parent.setIntegerValue("depth", 0);
			parent.setAttachObject(new AtomicInteger(0));
			// 之前累计的偏移时间
			parent.setLongValue("cumulative_time", 0L);
			parent.setLongValue("base_time", now);
		}
		return parent;
	}

	private long calcOffsetTime(XCO parentTrackingVo, long currentTime) {
		long base_time = parentTrackingVo.getLongValue("base_time");
		long cumulative_time = parentTrackingVo.getLongValue("cumulative_time");
		return currentTime - base_time + cumulative_time;
	}

	public Object startTracking(Object parent, String serviceURI, Object arg, Integer serviceType, Integer recordType, Integer executeMode) {
		return startTracking(parent, serviceURI, arg, null, serviceType, recordType, executeMode);
	}

	@Override
	public Object startTracking(Object parent, String serviceURI, Object arg, String headers, Integer serviceType, Integer recordType,
			Integer executeMode) {
		return startTracking(parent, serviceURI, arg, headers, serviceType, recordType, executeMode, null);
	}

	@Override
	public Object startTracking(Object parent, String serviceURI, Object arg, String headers, Integer serviceType, Integer recordType,
			Integer executeMode, Long now) {
		XCO parentTrackingVo = (XCO) parent;
		XCO trackingVo = new XCO();

		String spanId = createSpanId();

		// 当前跟踪信息
		trackingVo.setStringValue("trace_id", parentTrackingVo.getStringValue("trace_id"));
		trackingVo.setStringValue("span_id", spanId);
		trackingVo.setStringValue("parent_id", parentTrackingVo.getStringValue("span_id"));
		trackingVo.setIntegerValue("depth", parentTrackingVo.getIntegerValue("depth") + 1);//调用层深
		AtomicInteger base_order = (AtomicInteger) parentTrackingVo.getAttachObject();
		trackingVo.setIntegerValue("order", base_order.getAndIncrement());//当前层的调用顺序

		if (null != serviceType) {
			trackingVo.setIntegerValue("service_type", serviceType);
		}
		if (null != recordType) {
			trackingVo.setIntegerValue("record_type", recordType);
		}
		if (null != executeMode) {
			trackingVo.setIntegerValue("execute_mode", executeMode);
		}
		trackingVo.setStringValue("service_name", serviceURI);
		if (null != arg) {
			trackingVo.setStringValue("service_arg", arg.toString());
		}
		if (null != headers) {
			trackingVo.setStringValue("header_info", headers);
		}

		//		long currentTime = System.currentTimeMillis();

		long currentTime = 0L;
		if (null == now) {
			currentTime = System.currentTimeMillis();
		} else {
			currentTime = now;
		}

		trackingVo.setLongValue("start_time0", currentTime);
		long offset_time = calcOffsetTime(parentTrackingVo, currentTime);
		trackingVo.setLongValue("offset_time", offset_time);

		trackingVo.setStringValue("host_name", (String) AppProperty.get(AppProperty.HOST_NAME));
		trackingVo.setStringValue("host_ip", (String) AppProperty.get(AppProperty.HOST_IP));
		trackingVo.setStringValue("node_name", (String) AppProperty.get(AppProperty.NODE_NAME));
		trackingVo.setStringValue("app_name", (String) AppProperty.get(AppProperty.APP_NAME));

		// 当前跟踪信息作为父调用信息
		trackingVo.setLongValue("base_time", currentTime);
		trackingVo.setLongValue("cumulative_time", 0L);// 后面如果还有RPC调用,需要修正
		trackingVo.setAttachObject(new AtomicInteger(0));

		return trackingVo;
	}

	@Override
	public void setTracking(Object current, XCO arg) {
		if (null != arg) {
			XCO trackingVo = (XCO) current;
			trackingVo.setStringValue("service_arg", arg.toString());
		}
	}

	public void endTracking(Object current, Object result, Throwable ex) {

		XCO trackingVo = (XCO) current;

		int service_code = 0;
		String service_result = "";
		String service_message = "";
		String exception_info = "";

		XCO xco = (XCO) result;
		if (null != xco) {
			service_code = xco.getCode();
			service_message = xco.getMessage();
			service_result = xco.toXMLString();
		}

		if (null != ex) {
			exception_info = getExceptionInfo(ex);
		}

		if (null == xco && null != ex) {
			ServiceException se = TangYuanUtil.getServiceException(ex);
			service_code = se.getErrorCode();
			service_message = se.getErrorMessage();
		}

		trackingVo.setIntegerValue("service_code", service_code);
		trackingVo.setStringValue("service_result", service_result);
		trackingVo.setStringValue("service_message", service_message);
		trackingVo.setStringValue("exception_info", exception_info);

		long start_time = trackingVo.getLongValue("start_time0");
		long end_time = System.currentTimeMillis();
		long exec_time = end_time - start_time;
		if (0 == exec_time) {
			exec_time = 1L;
		}

		trackingVo.setStringValue("start_time", start_time + "");
		trackingVo.setStringValue("end_time", end_time + "");
		trackingVo.setLongValue("exec_time", exec_time);

		trackingVo.remove("start_time0");

		this.reporter.report(trackingVo);
	}

	public void addTrackingHeader(Object parent, Object arg) {
		if (!(arg instanceof XCO)) {
			return;
		}

		XCO trackingVo = (XCO) parent;
		XCO xco = (XCO) arg;
		XCO header = new XCO();

		header.setStringValue("trace_id", trackingVo.getStringValue("trace_id"));
		header.setStringValue("span_id", trackingVo.getStringValue("span_id"));
		header.setIntegerValue("depth", trackingVo.getIntegerValue("depth"));

		AtomicInteger base_order = (AtomicInteger) trackingVo.getAttachObject();
		header.setIntegerValue("base_order", base_order.getAndIncrement());

		long last_base_time = trackingVo.getLongValue("cumulative_time");
		long base_time = trackingVo.getLongValue("base_time");
		long now = System.currentTimeMillis();
		// 修正之前累计的偏移时间
		long cumulative_time = now - base_time + last_base_time;
		header.setLongValue("cumulative_time", cumulative_time);

		xco.setXCOValue(XCO_TRACE_KEY, header);
	}

	public void cleanTrackingHeader(Object arg) {
		if (!(arg instanceof XCO)) {
			return;
		}
		XCO xco = (XCO) arg;
		xco.remove(XCO_TRACE_KEY);
	}

	//	@Override
	//	public void setTracking(Object current, AbstractServiceNode service) {
	//		XCO trackingVo = (XCO) current;
	//		trackingVo.setIntegerValue("service_type", getServiceType(service));
	//	}

	//	public Object initTracking(Object arg) {
	//		XCO xco = (XCO) arg;
	//		XCO parent = new XCO();
	//		if (xco.exists(XCO_TRACE_KEY)) {	// 头部中存在跟踪信息
	//			parent = xco.getXCOValue(XCO_TRACE_KEY);
	//			int base_order = parent.getIntegerValue("base_order");
	//			parent.setAttachObject(new AtomicInteger(base_order));
	//			parent.setLongValue("base_time", System.currentTimeMillis());
	//
	//			parent.remove("base_order");
	//			// 清理参数中的更总信息, 將其保存在上下文中
	//			xco.remove(XCO_TRACE_KEY);
	//		} else {							// 一个初始化的跟踪信息
	//			parent.setStringValue("trace_id", createSpanId());
	//			parent.setStringValue("span_id", "");
	//			parent.setIntegerValue("depth", 0);
	//			parent.setAttachObject(new AtomicInteger(0));
	//			// 之前累计的偏移时间
	//			parent.setLongValue("cumulative_time", 0L);
	//			parent.setLongValue("base_time", System.currentTimeMillis());
	//		}
	//		return parent;
	//	}

}
