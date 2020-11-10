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
 * 1. 最初的起源 2. 单机进行的组件 3. 系统信息 4. 请求的Header信息
 */
public class RuntimeContext {

	public static final String					HEADER_KEY_TRACE_ID	= "trace_id";
	public static final String					HEADER_KEY_ORIGIN	= "origin";

	private static Log							log					= LogFactory.getLog(RuntimeContext.class);

	private static ThreadLocal<RuntimeContext>	contextThreadLocal	= new ThreadLocal<RuntimeContext>();

	/**
	 * 包含全部的所需内容. e.g: origin, traceId, component, header
	 */
	private XCO									data				= null;
	/**
	 * 跟踪信息
	 */
	private XCO									trackingParent		= null;

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
		XCO data = createRCData(header, origin);
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
			XCO data = createRCData(header, origin);
			rc.updateData(data);
		}
	}

	/**
	 * 解析从参数传递过来的header
	 * 
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
				JSONObject obj = (JSONObject) arg;
				JSONObject jsonHeader = obj.getJSONObject(TangYuanContainer.XCO_HEADER_KEY);
				if (null != jsonHeader) {
					// 清理头部Header
					((JSONObject) arg).remove(TangYuanContainer.XCO_HEADER_KEY);
					// 需要把JSON格式的Header转换成XCO格式
					header = FastJsonUtil.toXCO(jsonHeader);
				}
			} else {
				log.warn("暂不支持的header载体类型:" + arg.getClass().getName());
			}
		} catch (Throwable e) {
			log.error(e);
		}
		return header;
	}

}
