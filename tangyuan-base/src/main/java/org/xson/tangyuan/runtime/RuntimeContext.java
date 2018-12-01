package org.xson.tangyuan.runtime;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.runtime.trace.TrackingManager;
import org.xson.tangyuan.util.FastJsonUtil;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

import com.alibaba.fastjson.JSONObject;

/**
 * 运行时上下文
 */
public class RuntimeContext {

	public static final String					HEADER_KEY_TRACE_ID		= "trace_id";
	public static final String					HEADER_KEY_LOG_TYPE		= "log_type";
	public static final String					HEADER_KEY_COMPONENT	= "component";

	private static Log							log						= LogFactory.getLog(RuntimeContext.class);

	private static ThreadLocal<RuntimeContext>	contextThreadLocal		= new ThreadLocal<RuntimeContext>();

	/**
	 * 服务跟踪管理器
	 */
	private static TrackingManager				trackingManager			= null;

	/**
	 * 初始的日志类型(一旦确定，不在改变)
	 */
	private String								logType					= null;

	/**
	 * 全局业务ID
	 */
	private String								traceId					= null;

	/**
	 * 当前的组件
	 */
	private String								component				= null;

	private XCO									trackingParent			= null;
	private XCO									header					= null;

	private RuntimeContext(String traceId, String logType) {
		this.traceId = traceId;
		this.logType = logType;
	}

	private RuntimeContext(RuntimeContext rc) {
		this.traceId = rc.getTraceId();
		this.logType = rc.getLogType();
		this.trackingParent = rc.getTrackingParent();
		this.header = rc.getHeader();
	}

	public XCO getTrackingParent() {
		return trackingParent;
	}

	public void setTrackingParent(XCO parent) {
		this.trackingParent = parent;
	}

	public XCO getHeader() {
		return header;
	}

	public String getTraceId() {
		return traceId;
	}

	public String getLogType() {
		return logType;
	}

	public String getComponent() {
		return (null == component) ? "" : component;
	}

	private void setComponent(String component) {
		this.component = component;
	}

	private void clean0() {
		this.trackingParent = null;
		this.header = null;
	}

	/**
	 * 开启一个上下文, 容器入口调用
	 */
	public static void beginFromArg(Object arg) {
		beginFromArg(arg, null);
	}

	public static void beginFromArg(Object arg, Object info) {
		RuntimeContext rc = get();
		if (null != rc) {
			throw new TangYuanException("previously left uncleaned context: " + arg);
		}
		XCO header = null;
		String traceId = null;
		String logType = null;

		// 通过参数传递的
		if (null != arg && arg instanceof XCO) {
			header = ((XCO) arg).getXCOValue(TangYuanContainer.XCO_HEADER_KEY);
			if (null != header) {
				((XCO) arg).remove(TangYuanContainer.XCO_HEADER_KEY); // 清理头部Header
			}
		} else if (null != arg && arg instanceof JSONObject) {
			JSONObject _header = ((JSONObject) arg).getJSONObject(TangYuanContainer.XCO_HEADER_KEY);
			if (null != _header) {
				((JSONObject) arg).remove(TangYuanContainer.XCO_HEADER_KEY); // 清理头部Header
				// 需要吧JSON格式的Header转换成XCO格式
				header = FastJsonUtil.toXCO(_header);
			}
		}

		if (null == header) {
			header = new XCO();
		}
		traceId = header.getStringValue(HEADER_KEY_TRACE_ID);
		logType = header.getStringValue(HEADER_KEY_LOG_TYPE);
		traceId = (null == traceId) ? RuntimeContextUtils.createTraceId() : traceId;
		logType = (null == logType) ? "unknown" : logType;
		rc = new RuntimeContext(traceId, logType);

		// 直接放入组件名称
		if (null != info) {
			rc.setComponent(info.toString());
		}

		// 将Header保存到上下文中
		rc.header = header;

		// 开启服务追踪
		if (null != trackingManager) {
			rc.trackingParent = trackingManager.initTracking(traceId, header);
		}
		contextThreadLocal.set(rc);
	}

	/**
	 * 开启一个上下文(线程开始时调用)
	 */
	public static void beginFromContext(RuntimeContext rc) {
		if (null != rc) {
			RuntimeContext newRc = new RuntimeContext(rc);
			contextThreadLocal.set(newRc);
		}
	}

	/**
	 * 服务开始执行
	 */
	public static Object beginExecution(AbstractServiceNode service, Object arg, Integer executeMode, long now) {
		RuntimeContext rc = get();
		if (null == rc) {
			clean();
			return null;
		}

		TempContext tempContext = new TempContext();
		if (null != trackingManager) {
			// 需要忽略RPC, MQ
			if (TangYuanServiceType.PRCPROXY != service.getServiceType() && TangYuanServiceType.MQ != service.getServiceType()) {

				// 设置executeMode默认值
				if (null == executeMode) {
					executeMode = TrackingManager.EXECUTE_MODE_SYNC;
				}

				XCO parent = rc.getTrackingParent();
				Object current = startTracking(parent, service.getServiceKey(), arg, getTrackingServiceType(service),
						TrackingManager.RECORD_TYPE_SERVICE, executeMode, now);

				// set current->parent
				rc.setTrackingParent((XCO) current);
				tempContext.setTracking(parent, (XCO) current);
			}
		}

		// 设置新的组件名
		String previous = rc.getComponent();
		tempContext.setComponent(previous);
		rc.setComponent(service.getServiceType().toString());

		return tempContext;
	}

	/**
	 * 服务结束执行
	 */
	public static void endExecution(Object previous, Object result, Throwable ex) {

		RuntimeContext rc = get();
		if (null == rc) {
			clean();
			return;
		}

		TempContext tempContext = (TempContext) previous;

		if (null != trackingManager) {
			XCO current = tempContext.getTrackingCurrent();
			endTracking(current, result, ex);
			rc.setTrackingParent(tempContext.getTrackingParent());
		}

		// 设置老的组件名
		rc.setComponent(tempContext.getComponent());
	}

	public static boolean setHeader(Object arg) {
		if (null == arg) {
			return false;
		}

		RuntimeContext rc = get();
		if (null == rc) {
			return false;
		}

		if (arg instanceof XCO) {
			XCO header = ((XCO) arg).getXCOValue(TangYuanContainer.XCO_HEADER_KEY);
			if (null == header) {
				header = new XCO();
				((XCO) arg).setXCOValue(TangYuanContainer.XCO_HEADER_KEY, header);
			}
			// set header: runtime
			header.setStringValue(HEADER_KEY_TRACE_ID, rc.getTraceId());
			header.setStringValue(HEADER_KEY_LOG_TYPE, rc.getLogType());

			// set header: trace
			if (null != trackingManager) {
				trackingManager.setTrackingHeader(header, rc.getTrackingParent());
			}

			return true;
		} else if (arg instanceof JSONObject) {
			throw new TangYuanException("Not supported yet");
		}

		return false;
	}

	public static void cleanHeader(Object arg) {
		if (null == arg) {
			return;
		}
		if (arg instanceof XCO) {
			((XCO) arg).remove(TangYuanContainer.XCO_HEADER_KEY);
		} else if (arg instanceof JSONObject) {
			((JSONObject) arg).remove(TangYuanContainer.XCO_HEADER_KEY);
		} else {
			throw new TangYuanException("Not supported yet");
		}
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
			contextThreadLocal.remove();
		}
	}

	/**
	 * 启动一些上下文相关的服务
	 */
	public static void start(int type) throws Throwable {
		if (null != trackingManager) {
			trackingManager.start();
		}
	}

	/**
	 * 关闭一些上下文相关的服务
	 */
	public static void shutdown() {
		try {
			if (null != trackingManager) {
				trackingManager.stop();
			}
		} catch (Throwable e) {
			log.error(e);
		}
	}

	// ============================================================================

	public static void setTrackingManager(TrackingManager _trackingManager) {
		trackingManager = _trackingManager;
	}

	public static boolean isTracking() {
		return (null == trackingManager) ? false : true;
	}

	public static boolean isTraceCommand(int type) {
		boolean tc = isTracking();
		if (tc) {
			return trackingManager.isTraceCommand(type);
		}
		return tc;
	}

	public static Object startTracking(XCO parent, String serviceURI, Object arg, Integer serviceType, Integer recordType, Integer executeMode,
			Long now) {
		if (null != trackingManager) {
			try {
				if (null == parent) {
					parent = get().getTrackingParent();
				}
				Object tracking = trackingManager.startTracking(parent, serviceURI, arg, serviceType, recordType, executeMode, now);
				return tracking;
			} catch (Throwable e) {
				log.error(e);
			}
		}
		return null;
	}

	public static void endTracking(Object current, Object result, Throwable ex) {
		if (null != trackingManager) {
			try {
				trackingManager.endTracking((XCO) current, result, ex);
			} catch (Throwable e) {
				log.error(e);
			}
		}
	}

	public static void appendTracking(int type, Object arg) {
		if (isTraceCommand(type)) {
			try {
				XCO current = get().getTrackingParent();
				trackingManager.appendTracking(type, current, arg);
			} catch (Throwable e) {
				log.error(e);
			}
		}
	}

	private static int getTrackingServiceType(AbstractServiceNode service) {
		if (null == service) {
			return TrackingManager.SERVICE_TYPE_NO;
		}
		TangYuanServiceType type = service.getServiceType();
		if (TangYuanServiceType.SQL == type) {
			return TrackingManager.SERVICE_TYPE_SQL;
		} else if (TangYuanServiceType.MONGO == type) {
			return TrackingManager.SERVICE_TYPE_MONGO;
		} else if (TangYuanServiceType.JAVA == type) {
			return TrackingManager.SERVICE_TYPE_JAVA;
		} else if (TangYuanServiceType.HIVE == type) {
			return TrackingManager.SERVICE_TYPE_HIVE;
		} else if (TangYuanServiceType.HBASE == type) {
			return TrackingManager.SERVICE_TYPE_HBASE;
		} else if (TangYuanServiceType.MQ == type) {
			return TrackingManager.SERVICE_TYPE_MQ;
		} else if (TangYuanServiceType.ES == type) {
			return TrackingManager.SERVICE_TYPE_ES;
		}
		return TrackingManager.SERVICE_TYPE_NO;
	}
}
