package org.xson.tangyuan.runtime;

import org.xson.common.object.XCO;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

/**
 * 运行时上下文
 */
public class RuntimeContext {

	public static final String					HEADER_KEY_TRACE_ID		= "trace_id";
	public static final String					HEADER_KEY_LOG_TYPE		= "log_type";
	public static final String					HEADER_KEY_COMPONENT	= "component";

	private static ThreadLocal<RuntimeContext>	contextThreadLocal		= new ThreadLocal<RuntimeContext>();

	/**
	 * 日志类型(一旦确定，不在改变)
	 */
	private String								logType;

	/**
	 * 全局业务ID
	 */
	private String								traceId;

	/**
	 * 当前的组件
	 */
	private String								component;

	// private String spanId;
	// private String parentSpanId;

	private RuntimeContext(String traceId, String logType) {
		this.traceId = traceId;
		this.logType = logType;
	}

	private RuntimeContext(RuntimeContext rc) {
		this.traceId = rc.getTraceId();
		this.logType = rc.getLogType();
	}

	public String getTraceId() {
		return traceId;
	}

	public String getLogType() {
		return logType;
	}

	public String getComponent() {
		return component;
	}

	private void setComponent(String component) {
		this.component = component;
	}

	private static String createTraceId() {
		return "T:" + System.currentTimeMillis();
	}

	// private static String createSpanId() {
	// return "S:" + System.currentTimeMillis();
	// }

	/**
	 * 开启一个上下文, 容器入口调用
	 */
	// public static void begin() {
	// beginWithHeader(null);
	// }

	/**
	 * 开启一个上下文, 容器入口调用
	 */
	public static void beginFromArg(Object arg) {
		beginFromArg(arg, null);
	}

	public static void beginFromArg(Object arg, Object info) {
		// beginWithHeader(header);
		// TODO
	}

	// TODO XCO arg
	private static void beginWithHeader(XCO header) {
		RuntimeContext rc = get();
		if (null == rc) {
			String traceId = null;
			String logType = null;
			if (null == header) {
				// 全新的上下文
				traceId = createTraceId();
				logType = "unknown";
			} else {
				// 通过参数传递的
				traceId = header.getStringValue(HEADER_KEY_TRACE_ID);
				logType = header.getStringValue(HEADER_KEY_LOG_TYPE);
				traceId = (null == traceId) ? createTraceId() : traceId;
				logType = (null == logType) ? "unknown" : logType;
			}
			rc = new RuntimeContext(traceId, logType);
			contextThreadLocal.set(rc);
		}
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

	public static void updateHeader(Object arg) {
		// TODO 需要判断类型, 并且是否包含$$HEADER
	}

	/**
	 * 服务开始执行
	 */
	public static Object beginExecution(AbstractServiceNode service) {
		Object previous = null;
		// 需要知道当前的组件
		RuntimeContext rc = get();
		if (null != rc) {
			previous = rc.getComponent();
			rc.setComponent(service.getServiceType().toString());
		} else {
			clean();
		}
		return previous;
	}

	/**
	 * 服务结束执行
	 */
	public static void endExecution(Object previous) {
		RuntimeContext rc = get();
		if (null != rc) {
			rc.setComponent((String) previous);
		} else {
			clean();
		}
	}

	public static boolean setHeader(Object arg) {
		// TODO 向参数中设置上下文信息
		return false;
	}

	public static void cleanHeader() {
		// TODO 清除向参数中设置上下文信息
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
		contextThreadLocal.remove();
	}
}
