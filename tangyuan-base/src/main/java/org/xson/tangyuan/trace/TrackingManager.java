package org.xson.tangyuan.trace;

import org.xson.common.object.XCO;

public interface TrackingManager {

	String	XCO_TRACE_KEY			= "$$TRACE";

	/** 同步 */
	int		EXECUTE_MODE_SYNC		= 1;
	/** 异步 */
	int		EXECUTE_MODE_ASYN		= 2;

	int		RECORD_TYPE_CONTROLLER	= 1;
	int		RECORD_TYPE_TIMER		= 2;
	int		RECORD_TYPE_SERVICE		= 3;
	int		RECORD_TYPE_FUNCTION	= 4;

	int		SERVICE_TYPE_NO			= 0;
	int		SERVICE_TYPE_SQL		= 1;
	int		SERVICE_TYPE_JAVA		= 2;
	int		SERVICE_TYPE_MONGO		= 3;
	int		SERVICE_TYPE_MQ			= 4;
	int		SERVICE_TYPE_ES			= 5;
	int		SERVICE_TYPE_HIVE		= 6;
	int		SERVICE_TYPE_HBASE		= 7;

	public void start() throws Throwable;

	public void stop();

	/**
	 * 追踪初始化信息
	 */
	public Object initTracking(Object arg);

	/**
	 * 追踪初始化信息
	 */
	public Object initTracking(Object arg, Long now);

	/**
	 * 追踪一个服务调用的开始
	 */
	public Object startTracking(Object parent, String serviceURI, Object arg, Integer serviceType, Integer recordType, Integer executeMode);

	public Object startTracking(Object parent, String serviceURI, Object arg, String headers, Integer serviceType, Integer recordType,
			Integer executeMode);

	public Object startTracking(Object parent, String serviceURI, Object arg, String headers, Integer serviceType, Integer recordType,
			Integer executeMode, Long now);

	/**
	 * 追踪一个服务, 信息补充
	 */
	//	public void setTracking(Object current, AbstractServiceNode service);

	/**
	 * 追踪一个服务, 信息补充
	 */
	public void setTracking(Object current, XCO arg);

	/**
	 * 追踪一个服务调用的结束
	 */
	public void endTracking(Object current, Object result, Throwable ex);

	/**
	 * 添加追踪信息到头部
	 */
	public void addTrackingHeader(Object parent, Object arg);

	/**
	 * 清除参数中追踪信息
	 */
	public void cleanTrackingHeader(Object arg);
}
