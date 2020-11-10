package org.xson.tangyuan.manager.trace;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.ManagerLauncherContext;
import org.xson.tangyuan.manager.conf.DefaultResourceReloader;
import org.xson.tangyuan.service.runtime.RuntimeContext;
import org.xson.tangyuan.util.TangYuanUtil;

import com.alibaba.fastjson.JSONObject;

/**
 * 服务跟踪管理
 */
public class DefaultTraceManager extends DefaultResourceReloader implements TraceManager {

	private Log                    log = LogFactory.getLog(getClass());

	private TraceReporterThread    t   = null;

	private ManagerLauncherContext mlc = null;

	public DefaultTraceManager(ManagerLauncherContext mlc) {
		this.mlc = mlc;
	}

	//	@Override
	//	public void init(String resource) throws Throwable {
	//		Properties p       = MixedResourceManager.getProperties(resource, true);
	//		TraceVo    traceVo = new TraceVo();
	//		traceVo.init(p);
	//		this.t = new TraceReporterThread(null, mlc, traceVo);
	//	}
	//	@Override
	//	public void reload(String resource, String context) throws Throwable {
	//		if (null != t) {
	//			Properties p       = getPropertiesForReload(resource, context, true, false);
	//			TraceVo    traceVo = new TraceVo();
	//			traceVo.init(p);
	//			t.update(traceVo);
	//		}
	//	}

	@Override
	public void init(String resource) throws Throwable {
		//		Properties p       = MixedResourceManager.getProperties(resource, true);
		//		TraceVo    traceVo = new TraceVo();
		//		traceVo.init(p);
		this.t = new TraceReporterThread(null, mlc);
		reload0(resource);
	}

	@Override
	public void reload(String resource) throws Throwable {
		if (null != t) {
			reload0(resource);
			log.info(TangYuanLang.get("resource.reload"), resource);
		}
	}

	private void reload0(String resource) throws Throwable {
		Properties p       = getPropertiesForReload(resource, null, true, true);
		TraceVo    traceVo = new TraceVo();
		traceVo.init(p);
		t.update(traceVo);
	}

	@Override
	public void start() {
		if (null != t) {
			t.start();
		}
	}

	@Override
	public void stop() {
		if (null != t) {
			t.stop();
		}
	}

	@Override
	public void addTask(Object task) {
		if (null != t) {
			t.addTask(task);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public XCO initTracking(RuntimeContext rc) {
		long   now     = System.currentTimeMillis();
		XCO    data    = rc.getData();
		XCO    header  = data.getXCOValue(TangYuanContainer.XCO_HEADER_KEY);

		XCO    parent  = null;
		String traceId = null;

		if (null != header) {
			parent = header.getXCOValue(XCO_TRACE_KEY);
			traceId = header.getStringValue(RuntimeContext.HEADER_KEY_TRACE_ID);
		} else {
			traceId = data.getStringValue(RuntimeContext.HEADER_KEY_TRACE_ID);
		}

		if (null != parent) {
			// 头部中存在跟踪信息
			int base_order = parent.getIntegerValue("base_order");
			parent.remove("base_order");
			parent.setStringValue("trace_id", traceId);
			// 当前层的调用顺序
			parent.setAttachObject(new AtomicInteger(base_order));
			parent.setLongValue("base_time", now);
		} else {
			// 一个初始化的跟踪信息
			parent = new XCO();
			parent.setStringValue("trace_id", traceId);
			parent.setStringValue("span_id", "");
			parent.setIntegerValue("depth", 0);
			// 之前累计的偏移时间
			parent.setLongValue("cumulative_time", 0L);
			parent.setLongValue("base_time", now);// base_time==start_time
			// base_order
			parent.setAttachObject(new AtomicInteger(0));
		}
		return parent;
	}

	/**
	 * 用途: 服务过程中的RPC调用(仅用在RPC)
	 */
	@Override
	public void appendHeader(XCO parent, XCO header) {

		XCO traceHeader = new XCO();
		traceHeader.setStringValue("trace_id", parent.getStringValue("trace_id"));
		traceHeader.setStringValue("span_id", parent.getStringValue("span_id"));
		traceHeader.setIntegerValue("depth", parent.getIntegerValue("depth"));

		AtomicInteger base_order = (AtomicInteger) parent.getAttachObject();
		traceHeader.setIntegerValue("base_order", base_order.getAndIncrement());

		//		long last_base_time  = parent.getLongValue("cumulative_time");
		//		long cumulative_time = now - base_time + last_base_time;

		// 记录S1开始到新的调用之间的偏移，并将其携带(用作于X1部分)
		long base_time       = parent.getLongValue("base_time");
		long now             = System.currentTimeMillis();
		long cumulative_time = now - base_time;
		traceHeader.setLongValue("cumulative_time", cumulative_time);

		header.setXCOValue(XCO_TRACE_KEY, traceHeader);
	}

	@Override
	public XCO startTracking(XCO parent, String serviceKey, Integer serviceType, Object arg, Integer executeMode, long now) {

		XCO trackingVo = new XCO();

		//ManagerLauncherContext mlc = XmlGlobalContext.getMlc();
		if (null != mlc) {
			trackingVo.setStringValue("host_ip", mlc.getNodeIp());
			trackingVo.setStringValue("node_name", mlc.getNodeName());
			trackingVo.setStringValue("node_port", mlc.getNodePort());
			trackingVo.setStringValue("app_name", mlc.getAppName());
		}

		String spanId = TraceUtil.createSpanId();

		// 设置当前跟踪信息
		trackingVo.setStringValue("trace_id", parent.getStringValue("trace_id"));
		trackingVo.setStringValue("span_id", spanId);
		trackingVo.setStringValue("parent_id", parent.getStringValue("span_id"));
		// 调用层深
		trackingVo.setIntegerValue("depth", parent.getIntegerValue("depth") + 1);
		// 当前层的调用顺序
		AtomicInteger base_order = (AtomicInteger) parent.getAttachObject();
		trackingVo.setIntegerValue("order", base_order.getAndIncrement());

		trackingVo.setStringValue("service_name", serviceKey);
		trackingVo.setIntegerValue("record_type", RECORD_TYPE_SERVICE);

		if (null != serviceType) {
			trackingVo.setIntegerValue("service_type", serviceType);
		}
		if (null != executeMode) {
			trackingVo.setIntegerValue("execute_mode", executeMode);
		}
		if (null != arg) {
			trackingVo.setStringValue("service_arg", arg.toString());
		}

		// 时间相关
		long currentTime = now;
		// trackingVo.setLongValue("start_time0", currentTime);
		trackingVo.setLongValue("start_time", currentTime);
		// currentTime - parent.base_time + parent.cumulative_time;
		long offset_time = calcOffsetTime(parent, currentTime);
		// 偏移时间: 相对于父节点
		trackingVo.setLongValue("offset_time", offset_time);

		// 当前跟踪信息作为父调用信息

		// base_order
		trackingVo.setAttachObject(new AtomicInteger(0));
		// 当前跟踪信息作为父调用信息
		trackingVo.setLongValue("base_time", currentTime);
		// 后面如果还有RPC调用,需要修正
		trackingVo.setLongValue("cumulative_time", 0L);

		return trackingVo;
	}

	@Override
	public void appendTrackingArg(XCO current, Object arg) {
		if (null != arg) {
			current.setStringValue("service_arg", arg.toString());
		}
	}

	@Override
	public void appendTrackingCommand(XCO current, Object command) {
		// 之前已经过滤
		if (null == command) {
			return;
		}
		List<String> commands = current.getStringListValue("commands");
		if (null == commands) {
			commands = new ArrayList<String>();
			current.setStringListValue("commands", commands);
		}
		commands.add(command.toString());
	}

	@Override
	public void endTracking(XCO current, Object result, Throwable ex) {

		XCO     trackingVo      = current;

		Integer service_code    = null;
		String  service_result  = null;
		String  service_message = null;
		String  exception_info  = null;

		if (null != ex) {
			exception_info = getExceptionInfo(ex);
			ServiceException se = TangYuanUtil.getServiceException(ex);
			service_code = se.getErrorCode();
			service_message = se.getErrorMessage();
		} else {
			if (result instanceof JSONObject) {
				JSONObject res = (JSONObject) result;
				service_result = res.toJSONString();
				if (res.containsKey("code")) {
					service_code = res.getIntValue("code");
				}
				if (res.containsKey("message")) {
					service_message = res.getString("message");
				}
			} else {
				XCO res = TangYuanUtil.retObjToXco(result);
				service_result = res.toXMLString();
				service_code = res.getCode();
				service_message = res.getMessage();
			}
		}

		if (null != service_code) {
			trackingVo.setIntegerValue("service_code", service_code);
		}
		if (null != service_message) {
			trackingVo.setStringValue("service_message", service_message);
		}
		if (null != exception_info) {
			trackingVo.setStringValue("exception_info", exception_info);
		}
		if (null != service_result) {
			trackingVo.setStringValue("service_result", service_result);
		}

		long start_time = trackingVo.getLongValue("start_time");
		long end_time   = System.currentTimeMillis();
		long exec_time  = end_time - start_time;
		if (0L == exec_time) {
			exec_time = 1L;
		}
		trackingVo.setLongValue("end_time", end_time);
		trackingVo.setLongValue("exec_time", exec_time);

		//		long start_time = trackingVo.getLongValue("start_time0");
		//		trackingVo.setStringValue("start_time", start_time + "");
		//		trackingVo.setStringValue("end_time", end_time + "");
		//		trackingVo.remove("start_time0");

		addTask(trackingVo);
	}

	/**
	 * 计算偏移时间(当前时间-基础时间+累计偏移时间)
	 */
	private long calcOffsetTime(XCO parentTrackingVo, long currentTime) {
		long base_time       = parentTrackingVo.getLongValue("base_time");
		long cumulative_time = parentTrackingVo.getLongValue("cumulative_time");
		return currentTime - base_time + cumulative_time;
	}

	private String getExceptionInfo(Throwable ex) {
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw, true));
		return sw.getBuffer().toString();
	}

	@Override
	public boolean isTrackingCommand(int type) {
		//		if (SERVICE_TYPE_SQL == type) {
		//			return config.isEnableSqlTracking();
		//		}
		//		if (SERVICE_TYPE_MONGO == type) {
		//			return config.isEnableMongoTracking();
		//		}
		//		if (SERVICE_TYPE_ES == type) {
		//			return config.isEnableEsTracking();
		//		}
		//		if (SERVICE_TYPE_HBASE == type) {
		//			return config.isEnableHbaseTracking();
		//		}
		// TODO
		return true;
	}

	//	@Override
	//	public XCO startTracking(XCO parent, AbstractServiceNode service, Object arg, Integer executeMode, long now) {
	//		//		RuntimeContext rc         = RuntimeContext.get();
	//		//		XCO    parent     		  = rc.getTrackingParent();
	//		XCO    trackingVo = new XCO();
	//		String spanId     = RuntimeContext.createSpanId();
	//
	//		// 设置当前跟踪信息
	//		trackingVo.setStringValue("trace_id", parent.getStringValue("trace_id"));
	//		trackingVo.setStringValue("span_id", spanId);
	//		trackingVo.setStringValue("parent_id", parent.getStringValue("span_id"));
	//		// 调用层深
	//		trackingVo.setIntegerValue("depth", parent.getIntegerValue("depth") + 1);
	//		AtomicInteger base_order = (AtomicInteger) parent.getAttachObject();
	//		// 当前层的调用顺序
	//		trackingVo.setIntegerValue("order", base_order.getAndIncrement());
	//
	//		int serviceType = getServiceType(service);
	//		trackingVo.setIntegerValue("service_type", serviceType);
	//		trackingVo.setIntegerValue("record_type", RECORD_TYPE_SERVICE);
	//		if (null != executeMode) {
	//			trackingVo.setIntegerValue("execute_mode", executeMode);
	//		}
	//		trackingVo.setStringValue("service_name", service.getServiceKey());
	//		if (null != arg) {
	//			trackingVo.setStringValue("service_arg", arg.toString());
	//		}
	//
	//		//		long currentTime = 0L;
	//		//		if (null == now) {
	//		//			currentTime = System.currentTimeMillis();
	//		//		} else {
	//		//			currentTime = now;
	//		//		}
	//
	//		long currentTime = now;
	//
	//		trackingVo.setLongValue("start_time0", currentTime);
	//		long offset_time = calcOffsetTime(parent, currentTime);
	//		trackingVo.setLongValue("offset_time", offset_time);
	//
	//		//ManagerLauncherContext mlc = XmlGlobalContext.getMlc();
	//		if (null != mlc) {
	//			//			trackingVo.setStringValue("host_name", (String) AppProperty.get(AppProperty.HOST_NAME));
	//			//			trackingVo.setStringValue("host_ip", (String) AppProperty.get(AppProperty.HOST_IP));
	//			//			trackingVo.setStringValue("node_name", (String) AppProperty.get(AppProperty.NODE_NAME));
	//			//			trackingVo.setStringValue("app_name", (String) AppProperty.get(AppProperty.APP_NAME));
	//			trackingVo.setStringValue("host_ip", mlc.getNodeIp());
	//			trackingVo.setStringValue("node_name", mlc.getNodeName());
	//			trackingVo.setStringValue("node_port", mlc.getNodePort());
	//			trackingVo.setStringValue("app_name", mlc.getAppName());
	//		}
	//
	//		// 当前跟踪信息作为父调用信息
	//		trackingVo.setLongValue("base_time", currentTime);
	//		// 后面如果还有RPC调用,需要修正
	//		trackingVo.setLongValue("cumulative_time", 0L);
	//		// base_order
	//		trackingVo.setAttachObject(new AtomicInteger(0));
	//
	//		return trackingVo;
	//	}

	//	/**
	//	 * 用途: 服务过程中的RPC调用
	//	 */
	//	@Override
	//	public void appendHeader(XCO parent, XCO header) {
	//
	//		XCO traceHeader = new XCO();
	//		traceHeader.setStringValue("trace_id", parent.getStringValue("trace_id"));
	//		traceHeader.setStringValue("span_id", parent.getStringValue("span_id"));
	//		traceHeader.setIntegerValue("depth", parent.getIntegerValue("depth"));
	//
	//		//  可能有问题
	//		AtomicInteger base_order = (AtomicInteger) parent.getAttachObject();
	//		traceHeader.setIntegerValue("base_order", base_order.getAndIncrement());
	//
	//		// 修正之前累计的偏移时间
	//		long last_base_time  = parent.getLongValue("cumulative_time");
	//		long base_time       = parent.getLongValue("base_time");
	//		long now             = System.currentTimeMillis();
	//		long cumulative_time = now - base_time + last_base_time;
	//		traceHeader.setLongValue("cumulative_time", cumulative_time);
	//
	//		//		XCO header = arg.getXCOValue(TangYuanContainer.XCO_HEADER_KEY);
	//		header.setXCOValue(XCO_TRACE_KEY, traceHeader);
	//	}

	//	@Override
	//	public XCO initTracking(RuntimeContext rc) {
	//		long   now     = System.currentTimeMillis();
	//		XCO    data    = rc.getData();
	//		XCO    header  = data.getXCOValue(TangYuanContainer.XCO_HEADER_KEY);
	//		XCO    parent  = header.getXCOValue(XCO_TRACE_KEY);
	//		String traceId = header.getStringValue(RuntimeContext.HEADER_KEY_TRACE_ID);
	//		if (null != parent) {
	//			// 头部中存在跟踪信息
	//			int base_order = parent.getIntegerValue("base_order");
	//			parent.remove("base_order");
	//			parent.setStringValue("trace_id", traceId);
	//			parent.setAttachObject(new AtomicInteger(base_order));
	//			parent.setLongValue("base_time", now);
	//		} else {
	//			// 一个初始化的跟踪信息
	//			parent = new XCO();
	//			parent.setStringValue("trace_id", traceId);
	//			parent.setStringValue("span_id", "");
	//			parent.setIntegerValue("depth", 0);
	//			// 之前累计的偏移时间
	//			parent.setLongValue("cumulative_time", 0L);
	//			parent.setLongValue("base_time", now);
	//			// base_order
	//			parent.setAttachObject(new AtomicInteger(0));
	//		}
	//		return parent;
	//	}

	//	@Override
	//	public void endTracking(XCO current, Object result, Throwable ex) {
	//
	//		XCO    trackingVo      = current;
	//
	//		int    service_code    = 0;
	//		String service_result  = "";
	//		String service_message = "";
	//		String exception_info  = "";
	//
	//		//  result有可能是任何类型
	//
	//		if (null != result) {
	//			if (result instanceof XCO) {
	//				XCO     xco   = (XCO) result;
	//				Integer _code = xco.getCode();
	//				service_code = (null == _code) ? 0 : _code.intValue();
	//				service_message = xco.getMessage();
	//				service_result = xco.toXMLString();
	//			}
	//			// 
	//		}
	//
	//		if (null == result && null != ex) {
	//			ServiceException se = TangYuanUtil.getServiceException(ex);
	//			service_code = se.getErrorCode();
	//			service_message = se.getErrorMessage();
	//		}
	//		if (null != ex) {
	//			exception_info = getExceptionInfo(ex);
	//		}
	//
	//		trackingVo.setIntegerValue("service_code", service_code);
	//		trackingVo.setStringValue("service_message", service_message);
	//		trackingVo.setStringValue("exception_info", exception_info);
	//		trackingVo.setStringValue("service_result", service_result);
	//		//		if (!this.config.isEnableResultTracking()) {
	//		//			service_result = "";
	//		//		}
	//
	//		long start_time = trackingVo.getLongValue("start_time0");
	//		long end_time   = System.currentTimeMillis();
	//		long exec_time  = end_time - start_time;
	//		if (0 == exec_time) {
	//			exec_time = 1L;
	//		}
	//		trackingVo.setStringValue("start_time", start_time + "");
	//		trackingVo.setStringValue("end_time", end_time + "");
	//		trackingVo.setLongValue("exec_time", exec_time);
	//		trackingVo.remove("start_time0");
	//		//  考虑类型过滤
	//		addTask(trackingVo);
	//	}

	//	private int getServiceType(AbstractServiceNode service) {
	//		if (null == service) {
	//			return SERVICE_TYPE_NO;
	//		}
	//		TangYuanServiceType type = service.getServiceType();
	//		if (TangYuanServiceType.SQL == type) {
	//			return SERVICE_TYPE_SQL;
	//		} else if (TangYuanServiceType.MONGO == type) {
	//			return SERVICE_TYPE_MONGO;
	//		} else if (TangYuanServiceType.JAVA == type) {
	//			return SERVICE_TYPE_JAVA;
	//		} else if (TangYuanServiceType.HIVE == type) {
	//			return SERVICE_TYPE_HIVE;
	//		} else if (TangYuanServiceType.HBASE == type) {
	//			return SERVICE_TYPE_HBASE;
	//		} else if (TangYuanServiceType.MQ == type) {
	//			return SERVICE_TYPE_MQ;
	//		} else if (TangYuanServiceType.ES == type) {
	//			return SERVICE_TYPE_ES;
	//		}
	//		return SERVICE_TYPE_NO;
	//	}
}
