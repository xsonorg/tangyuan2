package org.xson.tangyuan.service;

import java.util.List;

import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.service.Pipeline.ErrorHandler;
import org.xson.tangyuan.service.mr.MapReduceHander;
import org.xson.tangyuan.util.TangYuanUtil;

/**
 * 服务调用执行器
 */
public class Actuator {

	private static ActuatorImpl impl = null;

	/**
	 * 初始化执行器，该方法供系统调用，而且只有一次
	 */
	public static void init(ActuatorImpl actuatorImpl) {
		if (null == impl && null != actuatorImpl) {
			impl = actuatorImpl;
		}
	}

	// =================================== execute ==========================================

	@SuppressWarnings("unchecked")
	public static <T> T execute(String serviceURI, Object arg) {
		try {
			impl.checkContainerState();
		} catch (ServiceException e) {
			return (T) TangYuanUtil.getExceptionResult(e);
		}
		return execute(serviceURI, arg, false);
	}

	@SuppressWarnings("unchecked")
	private static <T> T execute(String serviceURI, Object arg, boolean ignoreWrapper) {
		T result = impl.execute(serviceURI, arg);
		if (ignoreWrapper) {
			result = (T) TangYuanUtil.getRealData(result);
		}
		return result;
	}

	// =================================== executeAlone =====================================

	//	@Deprecated
	//	public static <T> T executeAlone(String serviceURI, Object arg) {
	//		return execute(serviceURI, arg);
	//	}

	// =================================== executeAsync ======================================

	public static void executeAsync(final String serviceURI, final Object arg) {
		executeAsync(serviceURI, arg, null);
	}

	public static void executeAsync(final String serviceURI, final Object arg, final String callBackService) {
		impl.checkContainerState();
		impl.executeAsync(serviceURI, arg, callBackService);
	}

	// =================================== executeMapReduce ==================================

	public static <T> T executeMapReduce(String serviceURI, List<Object> args, MapReduceHander handler, long timeout) {
		return executeMapReduce(null, serviceURI, args, handler, timeout);
	}

	public static <T> T executeMapReduce(Object mapReduceContext, String serviceURI, List<Object> args, MapReduceHander handler, long timeout) {
		impl.checkContainerState();
		return impl.executeMapReduce(mapReduceContext, serviceURI, args, handler, timeout);
	}

	public static <T> T executeMapReduce(List<String> services, List<Object> args, MapReduceHander handler, long timeout) {
		return executeMapReduce(null, services, args, handler, timeout);
	}

	public static <T> T executeMapReduce(Object mapReduceContext, List<String> services, List<Object> args, MapReduceHander handler, long timeout) {
		impl.checkContainerState();
		return impl.executeMapReduce(mapReduceContext, services, args, handler, timeout);
	}

	// =================================== Pipeline ==================================

	public static Pipeline createPipeline(final String serviceURI, final Object arg) {
		return createPipeline(serviceURI, arg, null);
	}

	public static Pipeline createPipeline(final String serviceURI, final Object arg, ErrorHandler errorHandler) {
		impl.checkContainerState();
		return impl.createPipeline(serviceURI, arg, errorHandler);
	}
}
