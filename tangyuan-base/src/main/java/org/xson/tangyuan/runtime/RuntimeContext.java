package org.xson.tangyuan.runtime;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

import com.alibaba.fastjson.JSONObject;

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
	public static void beginFromArg(Object arg) {
		beginFromArg(arg, null);
	}

	public static void beginFromArg(Object arg, Object info) {
		RuntimeContext rc = get();
		if (null == rc) {
			String traceId = null;
			String logType = null;
			if (null == arg) {
				// 全新的上下文
				traceId = createTraceId();
				logType = "unknown";
			} else if (arg instanceof XCO) {
				// 通过参数传递的
				XCO header = ((XCO) arg).getXCOValue(TangYuanContainer.XCO_HEADER_KEY);
				if (null != header) {
					traceId = header.getStringValue(HEADER_KEY_TRACE_ID);
					logType = header.getStringValue(HEADER_KEY_LOG_TYPE);
					traceId = (null == traceId) ? createTraceId() : traceId;
					logType = (null == logType) ? "unknown" : logType;
				}
			} else if (arg instanceof JSONObject) {
				// 通过参数传递的
				try {
					JSONObject header = ((JSONObject) arg).getJSONObject(TangYuanContainer.XCO_HEADER_KEY);
					if (null != header) {
						traceId = header.getString(HEADER_KEY_TRACE_ID);
						logType = header.getString(HEADER_KEY_LOG_TYPE);
						traceId = (null == traceId) ? createTraceId() : traceId;
						logType = (null == logType) ? "unknown" : logType;
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			rc = new RuntimeContext(traceId, logType);

			// 直接放入组件名称
			if (null != info) {
				rc.setComponent(info.toString());
			}
			contextThreadLocal.set(rc);
		} else {
			throw new TangYuanException("previously left uncleaned context: " + arg);
		}
	}

	// // XCO arg
	// private static void beginWithHeader(XCO header) {
	// RuntimeContext rc = get();
	// if (null == rc) {
	// String traceId = null;
	// String logType = null;
	// if (null == header) {
	// // 全新的上下文
	// traceId = createTraceId();
	// logType = "unknown";
	// } else {
	// // 通过参数传递的
	// traceId = header.getStringValue(HEADER_KEY_TRACE_ID);
	// logType = header.getStringValue(HEADER_KEY_LOG_TYPE);
	// traceId = (null == traceId) ? createTraceId() : traceId;
	// logType = (null == logType) ? "unknown" : logType;
	// }
	// rc = new RuntimeContext(traceId, logType);
	// contextThreadLocal.set(rc);
	// }
	// }

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

			header.setStringValue(HEADER_KEY_TRACE_ID, rc.getTraceId());
			header.setStringValue(HEADER_KEY_LOG_TYPE, rc.getLogType());
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
			XCO header = ((XCO) arg).getXCOValue(TangYuanContainer.XCO_HEADER_KEY);
			header.remove(HEADER_KEY_TRACE_ID);
			header.remove(HEADER_KEY_LOG_TYPE);
		} else if (arg instanceof JSONObject) {
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
		contextThreadLocal.remove();
	}
}
