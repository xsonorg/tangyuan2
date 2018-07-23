package org.xson.tangyuan.mr;

import java.util.List;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.trace.TrackingManager;

public class MapReduce {

	//	@SuppressWarnings("unchecked")
	//	public static <T> T execute(Object context, String serviceURI, List<Object> args, MapReduceHander handler, long timeout) throws Throwable {
	//		int size = args.size();
	//		for (int i = 0; i < size; i++) {
	//			execute0(context, serviceURI, args.get(i), handler);
	//		}
	//		return (T) handler.getResult(context, timeout);
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	public static <T> T execute(Object context, List<String> services, List<Object> args, MapReduceHander handler, long timeout) throws Throwable {
	//		int size = services.size();
	//		for (int i = 0; i < size; i++) {
	//			execute0(context, services.get(i), args.get(i), handler);
	//		}
	//		return (T) handler.getResult(context, timeout);
	//	}
	//
	//	private static void execute0(final Object context, final String service, final Object arg, final MapReduceHander handler) {
	//		TangYuanContainer.getInstance().getThreadPool().execute(new Runnable() {
	//			@Override
	//			public void run() {
	//				Object result = ServiceActuator.executeAlone(service, arg);
	//				handler.merge(context, service, result);
	//			}
	//		});
	//	}

	@SuppressWarnings("unchecked")
	public static <T> T execute(Object context, String serviceURI, List<Object> args, MapReduceHander handler, long timeout, ServiceContext parent)
			throws Throwable {
		int size = args.size();
		for (int i = 0; i < size; i++) {
			execute0(context, serviceURI, args.get(i), handler, parent);
		}
		return (T) handler.getResult(context, timeout);
	}

	@SuppressWarnings("unchecked")
	public static <T> T execute(Object context, List<String> services, List<Object> args, MapReduceHander handler, long timeout,
			ServiceContext parent) throws Throwable {
		int size = services.size();
		for (int i = 0; i < size; i++) {
			execute0(context, services.get(i), args.get(i), handler, parent);
		}
		return (T) handler.getResult(context, timeout);
	}

	private static void execute0(final Object context, final String service, final Object arg, final MapReduceHander handler,
			final ServiceContext parent) {
		TangYuanContainer.getInstance().getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				Object result = ServiceActuator.executeAlone(service, arg, parent, TrackingManager.EXECUTE_MODE_ASYN);
				handler.merge(context, service, result);
			}
		});
	}
}
