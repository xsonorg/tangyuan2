package org.xson.tangyuan.service.runtime;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.manager.trace.TraceManager;
import org.xson.tangyuan.manager.trace.TraceUtil;
import org.xson.tangyuan.util.FastJsonUtil;

import com.alibaba.fastjson.JSONObject;

/**
 * 运行时上下文。每个线程对应一个运行时的上下文
 * 
 * 用途如下：
 * 
 * 1. 最初的起源
 * 2. 单机进行的组件
 * 3. 系统信息
 * 4. 请求的Header信息
 */
public class RuntimeContext {

	public static final String                 HEADER_KEY_TRACE_ID = "trace_id";
	public static final String                 HEADER_KEY_ORIGIN   = "origin";

	//	// log type
	//	public static final String                 CONTEXT_ORIGIN_SYS     = "SYS";
	//	public static final String                 CONTEXT_ORIGIN_TEST    = "TEST";
	//	public static final String                 CONTEXT_ORIGIN_UNKNOWN = "UNKNOWN";
	//	public static final String                 CONTEXT_ORIGIN_TIMER   = "TIMER";
	//	public static final String                 CONTEXT_ORIGIN_MQ      = "MQ";
	//	public static final String                 CONTEXT_ORIGIN_WEB     = "WEB";

	private static Log                         log                 = LogFactory.getLog(RuntimeContext.class);

	private static ThreadLocal<RuntimeContext> contextThreadLocal  = new ThreadLocal<RuntimeContext>();

	/**
	 * 包含全部的所需内容. e.g: origin, traceId, component, header
	 */
	private XCO                                data                = null;
	/**
	 * 跟踪信息
	 */
	private XCO                                trackingParent      = null;

	private RuntimeContext(XCO data) {
		this.data = data;
	}

	private RuntimeContext(RuntimeContext parent) {
		if (null != parent.data) {
			this.data = parent.data.clone();
		}
		if (null != parent.trackingParent) {
			this.trackingParent = parent.trackingParent.clone();
		}
	}

	public XCO getData() {
		return data;
	}

	private void updateData(XCO data) {
		this.data = data;
	}

	public XCO getTrackingParent() {
		return trackingParent;
	}

	public void setTrackingParent(XCO trackingParent) {
		this.trackingParent = trackingParent;
	}

	public String getTraceId() {
		return (null == data) ? null : data.getStringValue(HEADER_KEY_TRACE_ID);
	}

	public String getOrigin() {
		return (null == data) ? null : data.getStringValue(HEADER_KEY_ORIGIN);
	}

	public XCO getHeader() {
		if (null != this.data) {
			return this.data.getXCOValue(TangYuanContainer.XCO_HEADER_KEY);
		}
		return null;
	}

	/**
	 * 获取一个上下文
	 */
	public static RuntimeContext get() {
		RuntimeContext rc = contextThreadLocal.get();
		if (null == rc) {
			clean();
		}
		return rc;
	}

	/**
	 * 清理当前线程的上下文
	 */
	public static void clean() {
		RuntimeContext rc = contextThreadLocal.get();
		if (null != rc) {
			rc.clean0();
			rc = null;
		}
		contextThreadLocal.remove();
	}

	private void clean0() {
		this.data = null;
		this.trackingParent = null;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 开启一个运行时上下文: 从一个已存在的上下文，一般用于异步线程调用的时候
	 */
	public static void begin(RuntimeContext parent) {
		if (null != parent) {
			RuntimeContext newRc = new RuntimeContext(parent);
			contextThreadLocal.set(newRc);
		}
	}

	/**
	 * 开启一个运行时上下文: 从调用开始
	 */
	public static void begin(Object arg) {
		begin(arg, null);
	}

	/**
	 * 开启一个运行时上下文: 从调用开始，并指定来源
	 */
	public static void begin(Object arg, String origin) {
		RuntimeContext rc = contextThreadLocal.get();
		if (null != rc) {
			// 之前存在未清理的上下文
			log.error("previously left uncleaned context: " + arg);
			return;
		}
		XCO header = parseHeader(arg);
		XCO data   = createRCData(header, origin);
		rc = new RuntimeContext(data);
		contextThreadLocal.set(rc);
	}

	private static XCO createRCData(XCO header, String origin) {
		String trace_id = null;
		if (null != header) {
			trace_id = header.getStringValue(HEADER_KEY_TRACE_ID);
		}
		if (null == trace_id) {
			trace_id = TraceUtil.createTraceId();
		}

		String origin_info = origin;
		if (null != header) {
			origin_info = header.getStringValue(HEADER_KEY_ORIGIN);
		}
		if (null == origin_info) {
			origin_info = (null != origin) ? origin : TraceManager.TRACE_ORIGIN_UNKNOWN;
		}

		XCO data = new XCO();
		data.setStringValue(HEADER_KEY_TRACE_ID, trace_id);
		data.setStringValue(HEADER_KEY_ORIGIN, origin_info);
		data.setXCOValue(TangYuanContainer.XCO_HEADER_KEY, header);
		return data;
	}

	public static void beginFromHeader(XCO header, String origin) {
		RuntimeContext rc = contextThreadLocal.get();
		if (null != rc) {
			// 之前存在未清理的上下文
			log.error("previously left uncleaned context: " + header);
			return;
		}
		XCO data = null;
		if (null != header) {
			data = createRCData(header, origin);
		}
		rc = new RuntimeContext(data);
		contextThreadLocal.set(rc);
	}

	public static void updateHeader(Object arg, String origin) {
		RuntimeContext rc = contextThreadLocal.get();
		if (null == rc.getData()) {
			XCO header = parseHeader(arg);
			XCO data   = createRCData(header, origin);
			rc.updateData(data);
		}
	}

	/**
	 * 解析从参数传递过来的header
	 * @param arg
	 * @return
	 */
	private static XCO parseHeader(Object arg) {
		if (null == arg) {
			return null;
		}
		XCO header = null;
		try {
			// 通过参数传递的
			if (arg instanceof XCO) {
				XCO obj = (XCO) arg;
				header = obj.getXCOValue(TangYuanContainer.XCO_HEADER_KEY);
				if (null != header) {
					// 清理头部Header
					obj.remove(TangYuanContainer.XCO_HEADER_KEY);
				}
			} else if (arg instanceof JSONObject) {
				JSONObject obj        = (JSONObject) arg;
				JSONObject jsonHeader = obj.getJSONObject(TangYuanContainer.XCO_HEADER_KEY);
				if (null != jsonHeader) {
					// 清理头部Header
					((JSONObject) arg).remove(TangYuanContainer.XCO_HEADER_KEY);
					// 需要把JSON格式的Header转换成XCO格式
					header = FastJsonUtil.toXCO(jsonHeader);
				}
			} else {
				log.warn("暂不支持的header载体类型:" + arg.getClass().getName());// TODO
			}
		} catch (Throwable e) {
			log.error(e);
		}
		return header;
	}

	/////////////////////////////////////////////////////////////////////////////////////

	//	public String getComponent() {
	//		return (null == data) ? null : data.getStringValue(HEADER_KEY_COMPONENT);
	//	}
	//	/**
	//	 * 解析从参数传递过来的header
	//	 * @param arg
	//	 * @return
	//	 */
	//	private static XCO parseHeader(Object arg) {
	//		if (null == arg) {
	//			return new XCO();
	//		}
	//		XCO header = null;
	//		try {
	//			// 通过参数传递的
	//			if (arg instanceof XCO) {
	//				XCO obj = (XCO) arg;
	//				header = obj.getXCOValue(TangYuanContainer.XCO_HEADER_KEY);
	//				if (null != header) {
	//					// 清理头部Header
	//					obj.remove(TangYuanContainer.XCO_HEADER_KEY);
	//				}
	//			} else if (arg instanceof JSONObject) {
	//				JSONObject obj        = (JSONObject) arg;
	//				JSONObject jsonHeader = obj.getJSONObject(TangYuanContainer.XCO_HEADER_KEY);
	//				if (null != jsonHeader) {
	//					// 清理头部Header
	//					((JSONObject) arg).remove(TangYuanContainer.XCO_HEADER_KEY);
	//					// 需要把JSON格式的Header转换成XCO格式
	//					header = FastJsonUtil.toXCO(jsonHeader);
	//				}
	//			} else {
	//				log.warn("暂不支持的header载体类型:" + arg.getClass().getName());// 
	//			}
	//		} catch (Throwable e) {
	//			log.error(e);
	//		}
	//		if (null == header) {
	//			header = new XCO();
	//		}
	//		return header;
	//	}

	//	public static Object appendHeader(Object arg) {
	//		if (!(arg instanceof XCO)) {
	//			return arg;
	//		}
	//
	//		RuntimeContext rc = get();
	//		if (null == rc) {
	//			return null;
	//		}
	//		XCO data   = rc.getData();
	//		XCO xcoArg = ((XCO) arg).clone();
	//
	//		// 1. 追加header		
	//		XCO header = data.getXCOValue(TangYuanContainer.XCO_HEADER_KEY);
	//		if (null == header) {
	//			header = new XCO();
	//			xcoArg.setXCOValue(TangYuanContainer.XCO_HEADER_KEY, header);
	//		}
	//		// set header: runtime
	//		header.setStringValue(RuntimeContext.HEADER_KEY_TRACE_ID, data.getStringValue(RuntimeContext.HEADER_KEY_TRACE_ID));
	//		header.setStringValue(RuntimeContext.HEADER_KEY_ORIGIN, data.getStringValue(RuntimeContext.HEADER_KEY_ORIGIN));
	//
	//		// 2. 追加trace
	//		if (null != tangYuanManager) {
	//			XCO parent = rc.getTrackingParent();
	//			tangYuanManager.appendTrackingHeader(parent, header);
	//		}
	//
	//		return xcoArg;
	//	}

	//	public static String createTraceId() {
	//		// return "T_" + System.currentTimeMillis();
	//		return UUID.randomUUID().toString().replaceAll("-", "");
	//	}
	//	public static String createSpanId() {
	//		return createTraceId();
	//	}

	// ============================================================================
	// ============================================================================
	// ============================================================================

	/**
	 * 启动一些上下文相关的服务
	 */
	//	public static void start(int type) throws Throwable {
	//		if (null != trackingManager) {
	//			trackingManager.start();
	//		}
	//	}

	/**
	 * 关闭一些上下文相关的服务
	 */
	//	public static void shutdown() {
	//		try {
	//			if (null != trackingManager) {
	//				trackingManager.stop();
	//			}
	//		} catch (Throwable e) {
	//			log.error(e);
	//		}
	//	}
	//	public static void setTrackingManager(TrackingManager _trackingManager) {
	//		trackingManager = _trackingManager;
	//	}
	//
	//	public static boolean isTracking() {
	//		return (null == trackingManager) ? false : true;
	//	}
	//
	//	public static boolean isTraceCommand(int type) {
	//		boolean tc = isTracking();
	//		if (tc) {
	//			return trackingManager.isTraceCommand(type);
	//		}
	//		return tc;
	//	}
	//
	//	public static Object startTracking(XCO parent, String serviceURI, Object arg, Integer serviceType, Integer recordType, Integer executeMode,
	//			Long now) {
	//		if (null != trackingManager) {
	//			try {
	//				if (null == parent) {
	//					parent = get().getTrackingParent();
	//				}
	//				Object tracking = trackingManager.startTracking(parent, serviceURI, arg, serviceType, recordType, executeMode, now);
	//				return tracking;
	//			} catch (Throwable e) {
	//				log.error(e);
	//			}
	//		}
	//		return null;
	//	}
	//
	//	public static void endTracking(Object current, Object result, Throwable ex) {
	//		if (null != trackingManager) {
	//			try {
	//				trackingManager.endTracking((XCO) current, result, ex);
	//			} catch (Throwable e) {
	//				log.error(e);
	//			}
	//		}
	//	}
	//
	//	public static void appendTracking(int type, Object arg) {
	//		if (isTraceCommand(type)) {
	//			try {
	//				XCO current = get().getTrackingParent();
	//				trackingManager.appendTracking(type, current, arg);
	//			} catch (Throwable e) {
	//				log.error(e);
	//			}
	//		}
	//	}
	//
	//	private static int getTrackingServiceType(AbstractServiceNode service) {
	//		if (null == service) {
	//			return TrackingManager.SERVICE_TYPE_NO;
	//		}
	//		TangYuanServiceType type = service.getServiceType();
	//		if (TangYuanServiceType.SQL == type) {
	//			return TrackingManager.SERVICE_TYPE_SQL;
	//		} else if (TangYuanServiceType.MONGO == type) {
	//			return TrackingManager.SERVICE_TYPE_MONGO;
	//		} else if (TangYuanServiceType.JAVA == type) {
	//			return TrackingManager.SERVICE_TYPE_JAVA;
	//		} else if (TangYuanServiceType.HIVE == type) {
	//			return TrackingManager.SERVICE_TYPE_HIVE;
	//		} else if (TangYuanServiceType.HBASE == type) {
	//			return TrackingManager.SERVICE_TYPE_HBASE;
	//		} else if (TangYuanServiceType.MQ == type) {
	//			return TrackingManager.SERVICE_TYPE_MQ;
	//		} else if (TangYuanServiceType.ES == type) {
	//			return TrackingManager.SERVICE_TYPE_ES;
	//		}
	//		return TrackingManager.SERVICE_TYPE_NO;
	//	}

	//	/**
	//	 * 开启一个上下文, 容器入口调用
	//	 */
	//	public static void beginFromArg(Object arg) {
	//		beginFromArg(arg, null);
	//	}
	//
	//	public static void beginFromArg(Object arg, String _origin) {
	//		RuntimeContext2 rc = get();
	//		if (null != rc) {
	//			throw new TangYuanException("previously left uncleaned context: " + arg);
	//		}
	//		XCO    header  = null;
	//		String traceId = null;
	//		String origin  = null;
	//
	//		// 通过参数传递的
	//		if (null != arg && arg instanceof XCO) {
	//			header = ((XCO) arg).getXCOValue(TangYuanContainer.XCO_HEADER_KEY);
	//			if (null != header) {
	//				// 清理头部Header
	//				((XCO) arg).remove(TangYuanContainer.XCO_HEADER_KEY);
	//			}
	//		} else if (null != arg && arg instanceof JSONObject) {
	//			JSONObject _header = ((JSONObject) arg).getJSONObject(TangYuanContainer.XCO_HEADER_KEY);
	//			if (null != _header) {
	//				// 清理头部Header
	//				((JSONObject) arg).remove(TangYuanContainer.XCO_HEADER_KEY);
	//				// 需要把JSON格式的Header转换成XCO格式
	//				header = FastJsonUtil.toXCO(_header);
	//			}
	//		}
	//
	//		if (null == header) {
	//			header = new XCO();
	//			traceId = RuntimeContextUtils.createTraceId();
	//			origin = _origin;
	//		} else {
	//			traceId = header.getStringValue(HEADER_KEY_TRACE_ID);
	//			origin = header.getStringValue(HEADER_KEY_ORIGIN);
	//		}
	//		traceId = (null == traceId) ? RuntimeContextUtils.createTraceId() : traceId;
	//		origin = (null == origin) ? CONTEXT_ORIGIN_UNKNOWN : origin;
	//		rc = new RuntimeContext2(traceId, origin);
	//		// 将Header保存到上下文中
	//		rc.header = header;
	//
	//		// 开启服务追踪
	//		//		if (null != trackingManager) {
	//		//			rc.trackingParent = trackingManager.initTracking(traceId, header);
	//		//		}
	//		contextThreadLocal.set(rc);
	//	}

	//	/**
	//	 * 开启一个上下文(线程开始时调用)
	//	 */
	//	public static void beginFromContext(RuntimeContext2 rc) {
	//		if (null != rc) {
	//			RuntimeContext2 newRc = new RuntimeContext2(rc);
	//			contextThreadLocal.set(newRc);
	//		}
	//	}

	//	private RuntimeContext2(String traceId, String origin) {
	//		this.traceId = traceId;
	//		this.origin = origin;
	//	}
	//
	//	private RuntimeContext2(RuntimeContext2 rc) {
	//		this.traceId = rc.getTraceId();
	//		this.origin = rc.getOrigin();
	//		//		this.trackingParent = rc.getTrackingParent();
	//		this.header = rc.getHeader();
	//	}

	//	public XCO getTrackingParent() {
	//		return trackingParent;
	//	}
	//
	//	public void setTrackingParent(XCO parent) {
	//		this.trackingParent = parent;
	//	}

	//	public XCO getHeader() {
	//		return header;
	//	}
	//
	//	public String getTraceId() {
	//		return traceId;
	//	}
	//
	//	public String getOrigin() {
	//		return origin;
	//	}
	//
	//	public String getComponent() {
	//		return (null == component) ? "" : component;
	//	}
	//
	//	private void setComponent(String component) {
	//		this.component = component;
	//	}

	//	private void clean0() {
	//		//		this.trackingParent = null;
	//		this.header = null;
	//	}
	//	/**
	//	 * 上下文的起源，从哪里创建的，会一直延续
	//	 */
	//	private String                              origin                 = null;
	//	/**
	//	 * 全局业务ID
	//	 */
	//	private String                              traceId                = null;
	//	/**
	//	 * 当前的组件
	//	 */
	//	private String                              component              = null;
	//	/**
	//	 * 头部
	//	 */
	//	private XCO                                 header                 = null;
	/**
	 * 服务跟踪管理器
	 */
	//private static TrackingManager				trackingManager			= null;
	//private XCO									trackingParent			= null;
	//	public static void beginFromContext(RuntimeContext2 Parent) {
	//
	//	}
	//	public static void begin() {}

	//	private static void cleanHeader(Object arg) {
	//		if (null == arg) {
	//			return;
	//		}
	//		if (arg instanceof XCO) {
	//			((XCO) arg).remove(TangYuanContainer.XCO_HEADER_KEY);
	//		} else if (arg instanceof JSONObject) {
	//			((JSONObject) arg).remove(TangYuanContainer.XCO_HEADER_KEY);
	//		} else {
	//			throw new TangYuanException("Not supported yet");
	//		}
	//	}

	//	public static Object beginService(AbstractServiceNode service, Object arg, Integer executeMode, long now) {
	//		//  要考虑 RuntimeContext2 rc = get(); == null
	//		return null;//
	//	}
	//
	//	public static void endService(Object previous, Object result, Throwable ex) {
	//		RuntimeContext rc = get();
	//		if (null == rc) {
	//			clean();
	//			return;
	//		}
	//		TempContext tempContext = (TempContext) previous;
	//
	//		if (null != trackingManager) {
	//			XCO current = tempContext.getTrackingCurrent();
	//			endTracking(current, result, ex);
	//			rc.setTrackingParent(tempContext.getTrackingParent());
	//		}
	//
	//		// 设置老的组件名
	//		rc.setComponent(tempContext.getComponent());
	//	}

	/////////////////////////////////////////////////////////////////

	//	/**
	//	 * 服务开始执行
	//	 */
	//	public static Object beginExecution(AbstractServiceNode service, Object arg, Integer executeMode, long now) {
	//		RuntimeContext rc = get();
	//		if (null == rc) {
	//			clean();
	//			return null;
	//		}
	//
	//		// fix bug
	//		if (null == service) {
	//			service = EmptyServiceNode.instance;
	//		}
	//
	//		TempContext tempContext = new TempContext();
	//		if (null != trackingManager) {
	//			// 需要忽略RPC, MQ
	//			if (TangYuanServiceType.PRCPROXY != service.getServiceType() && TangYuanServiceType.MQ != service.getServiceType()) {
	//
	//				// 设置executeMode默认值
	//				if (null == executeMode) {
	//					executeMode = TrackingManager.EXECUTE_MODE_SYNC;
	//				}
	//
	//				XCO    parent  = rc.getTrackingParent();
	//				Object current = startTracking(parent, service.getServiceKey(), arg, getTrackingServiceType(service), TrackingManager.RECORD_TYPE_SERVICE, executeMode, now);
	//
	//				// set current->parent
	//				rc.setTrackingParent((XCO) current);
	//				tempContext.setTracking(parent, (XCO) current);
	//			}
	//		}
	//
	//		// 设置新的组件名
	//		String previous = rc.getComponent();
	//		tempContext.setComponent(previous);
	//		rc.setComponent(service.getServiceType().toString());
	//
	//		return tempContext;
	//	}

	//	/**
	//	 * 服务结束执行
	//	 */
	//	public static void endExecution(Object previous, Object result, Throwable ex) {
	//
	//		RuntimeContext2 rc = get();
	//		if (null == rc) {
	//			clean();
	//			return;
	//		}
	//
	//		TempContext tempContext = (TempContext) previous;
	//
	//		if (null != trackingManager) {
	//			XCO current = tempContext.getTrackingCurrent();
	//			endTracking(current, result, ex);
	//			rc.setTrackingParent(tempContext.getTrackingParent());
	//		}
	//
	//		// 设置老的组件名
	//		rc.setComponent(tempContext.getComponent());
	//	}
	//	public static boolean setHeader(Object arg) {
	//		if (null == arg) {
	//			return false;
	//		}
	//
	//		RuntimeContext rc = get();
	//		if (null == rc) {
	//			return false;
	//		}
	//
	//		if (arg instanceof XCO) {
	//			XCO header = ((XCO) arg).getXCOValue(TangYuanContainer.XCO_HEADER_KEY);
	//			if (null == header) {
	//				header = new XCO();
	//				((XCO) arg).setXCOValue(TangYuanContainer.XCO_HEADER_KEY, header);
	//			}
	//			// set header: runtime
	//			header.setStringValue(HEADER_KEY_TRACE_ID, rc.getTraceId());
	//			header.setStringValue(CONTEXT_ORIGIN_UNKNOWN, rc.getOrigin());
	//
	//			// set header: trace
	//			if (null != trackingManager) {
	//				trackingManager.setTrackingHeader(header, rc.getTrackingParent());
	//			}
	//
	//			return true;
	//		} else if (arg instanceof JSONObject) {
	//			throw new TangYuanException("Not supported yet");
	//		}
	//
	//		return false;
	//	}
	//	public static final String                 HEADER_KEY_COMPONENT   = "component";
	//	private static TangYuanManager             tangYuanManager        = null;

}
