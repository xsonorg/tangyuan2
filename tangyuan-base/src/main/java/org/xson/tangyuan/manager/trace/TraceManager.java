package org.xson.tangyuan.manager.trace;

import org.xson.common.object.XCO;
import org.xson.tangyuan.service.runtime.RuntimeContext;

/**
 * 日志配置管理器
 */
public interface TraceManager {

	String                     XCO_TRACE_KEY          = "$$TRACE";

	/** 同步 */
	int                        EXECUTE_MODE_SYNC      = 1;
	/** 异步 */
	int                        EXECUTE_MODE_ASYN      = 2;

	int                        RECORD_TYPE_CONTROLLER = 1;
	int                        RECORD_TYPE_TIMER      = 2;
	int                        RECORD_TYPE_SERVICE    = 3;
	int                        RECORD_TYPE_FUNCTION   = 4;

	// log type
	public static final String TRACE_ORIGIN_SYS       = "SYS";
	public static final String TRACE_ORIGIN_TEST      = "TEST";
	public static final String TRACE_ORIGIN_UNKNOWN   = "UNKNOWN";
	public static final String TRACE_ORIGIN_TIMER     = "TIMER";
	public static final String TRACE_ORIGIN_MQ        = "MQ";
	public static final String TRACE_ORIGIN_WEB       = "WEB";

	public static void main(String[] args) {
		System.out.println("trace".toUpperCase());
	}

	//	int    SERVICE_TYPE_NO        = 0;
	//	int    SERVICE_TYPE_SQL       = 1;
	//	int    SERVICE_TYPE_JAVA      = 2;
	//	int    SERVICE_TYPE_MONGO     = 3;
	//	int    SERVICE_TYPE_MQ        = 4;
	//	int    SERVICE_TYPE_ES        = 5;
	//	int    SERVICE_TYPE_HIVE      = 6;
	//	int    SERVICE_TYPE_HBASE     = 7;
	//	int    SERVICE_TYPE_WEB       = 99;

	void init(String resource) throws Throwable;

	void start();

	void stop();

	void addTask(Object task);

	//////////////////////////////////////////////////

	XCO initTracking(RuntimeContext rc);

	XCO startTracking(XCO parent, String serviceKey, Integer serviceType, Object arg, Integer executeMode, long now);

	void appendTrackingArg(XCO current, Object arg);

	void appendTrackingCommand(XCO current, Object command);

	void appendHeader(XCO parent, XCO header);

	void endTracking(XCO current, Object result, Throwable ex);

	boolean isTrackingCommand(int type);

	//	XCO startTracking(XCO parent, AbstractServiceNode service, Object arg, Integer executeMode, long now);

}
