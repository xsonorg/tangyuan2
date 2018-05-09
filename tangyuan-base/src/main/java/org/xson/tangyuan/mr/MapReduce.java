package org.xson.tangyuan.mr;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceActuator;

public class MapReduce {

	// TODO
	private static ExecutorService executorService = Executors.newFixedThreadPool(100);

	@SuppressWarnings("unchecked")
	public static <T> T execute(String serviceURI, List<Object> args, MapReduceHander handler, long timeout, boolean alone) throws Throwable {
		int size = args.size();
		XCO context = new XCO();
		for (int i = 0; i < size; i++) {
			execute0(context, serviceURI, args.get(i), handler, alone);
		}
		return (T) handler.getResult(context, timeout);
	}

	@SuppressWarnings("unchecked")
	public static <T> T execute(List<String> services, List<Object> args, MapReduceHander handler, long timeout, boolean alone) throws Throwable {
		int size = services.size();
		XCO context = new XCO();
		for (int i = 0; i < size; i++) {
			execute0(context, services.get(i), args.get(i), handler, alone);
		}
		return (T) handler.getResult(context, timeout);
	}

	private static void execute0(final XCO context, final String service, final Object arg, final MapReduceHander handler, final boolean alone) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				Object result = null;
				if (alone) {
					result = ServiceActuator.executeAlone(service, arg);
				} else {
					result = ServiceActuator.execute(service, arg);
				}
				handler.merge(context, service, result);
			}
		});
	}
}
