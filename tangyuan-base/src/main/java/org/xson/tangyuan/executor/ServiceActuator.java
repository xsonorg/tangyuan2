package org.xson.tangyuan.executor;

import java.util.List;

import org.xson.tangyuan.service.Actuator;
import org.xson.tangyuan.service.mr.MapReduceHander;

/**
 * @see org.xson.tangyuan.service.Actuator
 */
@Deprecated
public class ServiceActuator {

	/**
	 * @see Actuator.execute
	 */
	@Deprecated
	public static <T> T execute(String serviceURI, Object arg) {
		return Actuator.execute(serviceURI, arg);
	}

	/**
	 * @see Actuator.execute
	 */
	@Deprecated
	public static <T> T executeAlone(String serviceURI, Object arg) {
		return Actuator.execute(serviceURI, arg);
	}

	/**
	 * @see Actuator.executeAsync
	 */
	@Deprecated
	public static void executeAsync(final String serviceURI, final Object arg) {
		Actuator.executeAsync(serviceURI, arg);
	}

	/**
	 * @see Actuator.executeMapReduce
	 */
	@Deprecated
	public static <T> T executeMapReduce(String serviceURI, List<Object> args, MapReduceHander handler, long timeout) {
		return executeMapReduce(null, serviceURI, args, handler, timeout);
	}

	/**
	 * @see Actuator.executeMapReduce
	 */
	@Deprecated
	public static <T> T executeMapReduce(Object mapReduceContext, String serviceURI, List<Object> args, MapReduceHander handler, long timeout) {
		return Actuator.executeMapReduce(mapReduceContext, serviceURI, args, handler, timeout);
	}

	/**
	 * @see Actuator.executeMapReduce
	 */
	@Deprecated
	public static <T> T executeMapReduce(List<String> services, List<Object> args, MapReduceHander handler, long timeout) {
		return executeMapReduce(null, services, args, handler, timeout);
	}

	/**
	 * @see Actuator.executeMapReduce
	 */
	@Deprecated
	public static <T> T executeMapReduce(Object mapReduceContext, List<String> services, List<Object> args, MapReduceHander handler, long timeout) {
		return Actuator.executeMapReduce(mapReduceContext, services, args, handler, timeout);
	}
}
