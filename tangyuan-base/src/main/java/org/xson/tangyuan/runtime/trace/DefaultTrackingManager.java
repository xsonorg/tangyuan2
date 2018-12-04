package org.xson.tangyuan.runtime.trace;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.app.AppProperty;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.runtime.RuntimeContextUtils;
import org.xson.tangyuan.util.TangYuanUtil;

import com.alibaba.fastjson.JSONObject;

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

	/*
	 * 此处传递的参数是完整的Header, 参数中Header信息统一清除
	 */
	@Override
	public XCO initTracking(String traceId, XCO header) {
		return initTracking(traceId, header, null);
	}

	@Override
	public XCO initTracking(String traceId, XCO header, Long now) {
		XCO parent = null;

		XCO xco = (XCO) header;

		if (null == now) {
			now = System.currentTimeMillis();
		}

		if (null != xco) {
			parent = xco.getXCOValue(XCO_TRACE_KEY);
		}

		if (null != parent) {
			// 头部中存在跟踪信息
			int base_order = parent.getIntegerValue("base_order");
			parent.remove("base_order");

			parent.setStringValue("trace_id", traceId);

			parent.setAttachObject(new AtomicInteger(base_order));
			parent.setLongValue("base_time", now);
		} else {
			// 一个初始化的跟踪信息
			parent = new XCO();
			// parent.setStringValue("trace_id", RuntimeContextUtils.createSpanId());
			parent.setStringValue("trace_id", traceId);
			parent.setStringValue("span_id", "");
			parent.setIntegerValue("depth", 0);
			parent.setLongValue("cumulative_time", 0L); // 之前累计的偏移时间
			parent.setLongValue("base_time", now);
			parent.setAttachObject(new AtomicInteger(0)); // base_order
		}

		return parent;
	}

	@Override
	public Object startTracking(XCO parent, String serviceURI, Object arg, Integer serviceType, Integer recordType, Integer executeMode, Long now) {
		/* 上层保证parent!=null */

		XCO trackingVo = new XCO();

		String spanId = RuntimeContextUtils.createSpanId();
		// 设置当前跟踪信息
		trackingVo.setStringValue("trace_id", parent.getStringValue("trace_id"));
		trackingVo.setStringValue("span_id", spanId);
		trackingVo.setStringValue("parent_id", parent.getStringValue("span_id"));
		trackingVo.setIntegerValue("depth", parent.getIntegerValue("depth") + 1);// 调用层深
		AtomicInteger base_order = (AtomicInteger) parent.getAttachObject();
		trackingVo.setIntegerValue("order", base_order.getAndIncrement());// 当前层的调用顺序

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

		long currentTime = 0L;
		if (null == now) {
			currentTime = System.currentTimeMillis();
		} else {
			currentTime = now;
		}

		trackingVo.setLongValue("start_time0", currentTime);
		long offset_time = calcOffsetTime(parent, currentTime);
		trackingVo.setLongValue("offset_time", offset_time);

		trackingVo.setStringValue("host_name", (String) AppProperty.get(AppProperty.HOST_NAME));
		trackingVo.setStringValue("host_ip", (String) AppProperty.get(AppProperty.HOST_IP));
		trackingVo.setStringValue("node_name", (String) AppProperty.get(AppProperty.NODE_NAME));
		trackingVo.setStringValue("app_name", (String) AppProperty.get(AppProperty.APP_NAME));

		// 当前跟踪信息作为父调用信息
		trackingVo.setLongValue("base_time", currentTime);
		trackingVo.setLongValue("cumulative_time", 0L);// 后面如果还有RPC调用,需要修正
		trackingVo.setAttachObject(new AtomicInteger(0)); // base_order

		return trackingVo;
	}

	/**
	 * 计算偏移时间(当前时间-基础时间+累计偏移时间)
	 */
	private long calcOffsetTime(XCO parentTrackingVo, long currentTime) {
		long base_time = parentTrackingVo.getLongValue("base_time");
		long cumulative_time = parentTrackingVo.getLongValue("cumulative_time");
		return currentTime - base_time + cumulative_time;
	}

	private String getExceptionInfo(Throwable ex) {
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw, true));
		return sw.getBuffer().toString();
	}

	@Override
	public void endTracking(XCO current, Object result, Throwable ex) {

		XCO trackingVo = current;

		int service_code = 0;
		String service_result = "";
		String service_message = "";
		String exception_info = "";

		if (null != result) {
			if (result instanceof XCO) {
				XCO xco = (XCO) result;
				Integer _code = xco.getCode();
				service_code = (null == _code) ? 0 : _code.intValue();
				service_message = xco.getMessage();
				service_result = xco.toXMLString();
			} else if (result instanceof JSONObject) {
				JSONObject obj = (JSONObject) result;
				String _code = obj.getString("code");
				if (null == _code) {
					_code = obj.getString(TangYuanContainer.XCO_CODE_KEY);
				}
				service_code = (null == _code) ? 0 : Integer.parseInt(_code);
				String _message = obj.getString("message");
				if (null == _message) {
					_message = obj.getString(TangYuanContainer.XCO_MESSAGE_KEY);
				}
				service_message = (null == _message) ? "" : _message;
				service_result = obj.toJSONString();
			} else {
				service_result = result.toString();
			}
		}

		if (null == result && null != ex) {
			ServiceException se = TangYuanUtil.getServiceException(ex);
			service_code = se.getErrorCode();
			service_message = se.getErrorMessage();
		}

		if (null != ex) {
			exception_info = getExceptionInfo(ex);
		}

		trackingVo.setIntegerValue("service_code", service_code);
		trackingVo.setStringValue("service_message", service_message);
		trackingVo.setStringValue("exception_info", exception_info);
		if (!this.config.isEnableResultTracking()) {
			service_result = "";
		}
		trackingVo.setStringValue("service_result", service_result);

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

	@Override
	public void setTrackingHeader(XCO header, XCO parent) {

		XCO traceHeader = new XCO();
		traceHeader.setStringValue("trace_id", parent.getStringValue("trace_id"));
		traceHeader.setStringValue("span_id", parent.getStringValue("span_id"));
		traceHeader.setIntegerValue("depth", parent.getIntegerValue("depth"));

		AtomicInteger base_order = (AtomicInteger) parent.getAttachObject();
		traceHeader.setIntegerValue("base_order", base_order.getAndIncrement());

		// 修正之前累计的偏移时间
		long last_base_time = parent.getLongValue("cumulative_time");
		long base_time = parent.getLongValue("base_time");
		long now = System.currentTimeMillis();
		long cumulative_time = now - base_time + last_base_time;
		traceHeader.setLongValue("cumulative_time", cumulative_time);

		header.setXCOValue(XCO_TRACE_KEY, traceHeader);
	}

	@Override
	public void appendTracking(int type, XCO current, Object arg) {
		// 之前已经过滤
		if (null == arg) {
			return;
		}
		List<String> commands = current.getStringListValue("commands");
		if (null == commands) {
			commands = new ArrayList<String>();
			current.setStringListValue("commands", commands);
		}
		commands.add(arg.toString());
	}

	@Override
	public boolean isTraceCommand(int type) {
		if (SERVICE_TYPE_SQL == type) {
			return config.isEnableSqlTracking();
		}
		if (SERVICE_TYPE_MONGO == type) {
			return config.isEnableMongoTracking();
		}
		if (SERVICE_TYPE_ES == type) {
			return config.isEnableEsTracking();
		}
		if (SERVICE_TYPE_HBASE == type) {
			return config.isEnableHbaseTracking();
		}
		return false;
	}

}
