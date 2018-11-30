package org.xson.tangyuan.executor;

import java.util.List;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.aop.AopSupport;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mr.MapReduceHander;
import org.xson.tangyuan.ognl.convert.ParameterConverter;
import org.xson.tangyuan.rpc.RpcProxy;
import org.xson.tangyuan.rpc.RpcServiceNode;
import org.xson.tangyuan.runtime.RuntimeContext;
import org.xson.tangyuan.runtime.trace.TrackingManager;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

public class ServiceActuator {

	private static Log									log					= LogFactory.getLog(ServiceActuator.class);
	private static ThreadLocal<ThreadServiceContext>	contextThreadLocal	= new ThreadLocal<ThreadServiceContext>();
	private static ParameterConverter					converter			= new ParameterConverter();
	private static AopSupport							aop					= null;
	private static volatile boolean						running				= true;
	private static boolean								onlyProxy			= false;

	@SuppressWarnings("static-access")
	public static void shutdown() {
		running = false;
		long start = System.currentTimeMillis();
		long maxWaitTimeForShutDown = TangYuanContainer.getInstance().getMaxWaitTimeForShutDown();
		while (ServiceContext.globleCounter.get() > 0) {
			if ((System.currentTimeMillis() - start) > maxWaitTimeForShutDown) {
				log.error("wait for service to close timeout, The current context number " + ServiceContext.globleCounter.get());
				return;
			}
			log.info("waiting for service to close.");
			try {
				Thread.currentThread().sleep(500L);
			} catch (InterruptedException e) {
				log.error("wait for service to close exception", e);
				return;
			}
		}
		log.info("service has been closed normally.");
	}

	public static void openOnlyProxyMode() {
		onlyProxy = true;
	}

	public static void openAop() {
		aop = AopSupport.getInstance();
	}

	/* ================================================================================== */

	/** 检查是否是还有存在的上下文中调用, 用于用户安全的关闭 */
	private static void check() {
		if (running) {
			return;
		}
		boolean existingContext = (null == contextThreadLocal.get()) ? false : true;
		if (!existingContext) {
			throw new ServiceException("The current system is shutting down and no longer handles the new service.");
		}
	}

	/** 方法之前的线程调用 */
	private static ServiceContext begin(boolean isNew, ServiceContext parent) {
		ThreadServiceContext threadContext = contextThreadLocal.get();
		if (null == threadContext) {
			threadContext = new ThreadServiceContext();
			contextThreadLocal.set(threadContext);
		}
		return threadContext.getOrCreate(isNew, parent);
	}

	private static ServiceContext getServiceContext() {
		ThreadServiceContext threadContext = contextThreadLocal.get();
		if (null != threadContext) {
			return threadContext.get();
		}
		return null;
	}

	/** 无异常的结束 */
	private static void onSuccess() {
		ThreadServiceContext threadContext = contextThreadLocal.get();
		if (null != threadContext) {
			ServiceContext context = threadContext.get();
			if (null == context) {
				contextThreadLocal.remove();
				return;
			}
			log.debug("context--. hashCode[" + context.hashCode() + "], context[" + (context.counter - 1) + "]");
			if (--context.counter < 1) {
				Throwable ex = null;
				if (null == context.getExceptionInfo()) {
					try {
						context.finish();// 这里是确定的提交
					} catch (Throwable e) {
						context.finishOnException();
						log.error("tangyuan service commit exception", e);
						ex = e; // fix bug: 这里的异常需要上抛
					}
				} else {
					context.finishOnException();
				}
				log.debug("close a context. hashCode[" + context.hashCode() + "]");
				// context.stopMonitor();// stop monitor
				if (threadContext.recycle()) {
					contextThreadLocal.remove();
				}

				// fix bug: 这里的异常需要上抛
				if (null != ex) {
					throw TangYuanUtil.getServiceException(ex);
				}
			}
		} else {
			contextThreadLocal.remove();
		}
	}

	/** 发生异常的结束 */
	private static void onException(Throwable throwable, String serviceURI, AbstractServiceNode service, boolean throwException) {
		// 如果当前可以处理,当前处理; 如果当前不能处理,上抛,不做日志输出
		ThreadServiceContext threadContext = contextThreadLocal.get();
		ServiceContext context = threadContext.get();
		if (--context.counter < 1) { // 最后一层
			context.finishOnException();
			log.error("execute service exception: " + serviceURI, throwable);
			log.debug("close a context. hashCode[" + context.hashCode() + "] on exception");
			// context.stopMonitor();
			if (threadContext.recycle()) {
				contextThreadLocal.remove();
			}

			if (throwException) {
				// 最后一层抛出的异常
				throw TangYuanUtil.getServiceException(throwable);
			}
		} else { // 不是最后一层
			if (null != service) {
				try {
					context.onException(service.getServiceType(), throwable, "execute service exception: " + serviceURI);
				} catch (Throwable e) {
					throw TangYuanUtil.getServiceException(e);
				}
			} else {
				// service为空,则表示未找到服务,未做任务业务, 仅作日志输出.
				log.error("execute service exception: " + serviceURI, throwable);
			}
		}
	}

	private static Object getResult(Object result, boolean ignoreWrapper) {
		if (ignoreWrapper) {
			return result;
		}
		boolean allServiceReturnXCO = TangYuanContainer.getInstance().isAllServiceReturnXCO();
		if (allServiceReturnXCO) {
			return TangYuanUtil.retObjToXco(result);
		}
		return result;
	}

	private static Object getExceptionResult(Throwable e) {
		boolean allServiceReturnXCO = TangYuanContainer.getInstance().isAllServiceReturnXCO();
		if (allServiceReturnXCO) {
			return TangYuanUtil.getExceptionResult(e);
		}
		return null;// 发生错误, 一定是NULL
	}

	/* ================================================================================== */

	/**
	 * 获取服务<br />
	 * a/b|www.xx.com/a/b|{xxx}/a/b
	 */
	private static AbstractServiceNode findService(String serviceURL) {
		try {
			if (onlyProxy) {
				return findProxyService(serviceURL);
			}

			// 查询本地服务
			AbstractServiceNode service = TangYuanContainer.getInstance().getService(serviceURL);
			if (null != service) {
				return service;
			}

			// 查询远程服务
			service = TangYuanContainer.getInstance().getDynamicService(serviceURL);
			if (null != service) {
				return service;
			}

			// 处理本地占位URL
			if (null != RpcProxy.getPlaceHolderHandler()) {
				String newServiceURL = RpcProxy.getPlaceHolderHandler().parse(serviceURL);
				if (null != newServiceURL) {
					return TangYuanContainer.getInstance().getService(newServiceURL);
				}
			}

			// 创建新的远程服务
			return createDynamicService(serviceURL);
		} catch (Throwable e) {
			log.error("Invalid service url: " + serviceURL, e);
		}
		return null;
	}

	private static AbstractServiceNode findProxyService(String serviceURL) {
		AbstractServiceNode service = TangYuanContainer.getInstance().getDynamicService(serviceURL);
		if (null != service) {
			return service;
		}
		return createDynamicService(serviceURL);
	}

	private static AbstractServiceNode createDynamicService(String serviceURL) {

		AbstractServiceNode service = null;

		if (null == aop) {
			service = new RpcServiceNode(serviceURL);
			TangYuanContainer.getInstance().addDynamicService(service);
			return service;
		}

		// 是否是拦截方
		if (aop.isInterceptor(serviceURL)) {
			service = new RpcServiceNode(serviceURL);
			TangYuanContainer.getInstance().addDynamicService(service);
			return service;
		}

		service = new RpcServiceNode(serviceURL);
		// 检查并设置被拦截方
		aop.checkAndsetIntercepted(serviceURL, service);
		TangYuanContainer.getInstance().addDynamicService(service);
		return service;
	}

	/* ================================================================================== */

	public static <T> T execute(String serviceURI, Object arg) throws ServiceException {
		return execute(serviceURI, arg, false);
	}

	public static <T> T execute(String serviceURI, Object arg, boolean ignoreWrapper) throws ServiceException {
		return execute(serviceURI, arg, ignoreWrapper, null);
	}

	@SuppressWarnings("unchecked")
	private static <T> T execute(String serviceURI, Object arg, boolean ignoreWrapper, ServiceContext parent) throws ServiceException {
		// 1. 检查系统是否已经正在关闭中了
		check();

		log.info("execute service: " + serviceURI);

		long now = System.currentTimeMillis();

		// 2. 获取上下文
		ServiceContext context = begin(false, parent);

		Object tempObject = null;
		AbstractServiceNode service = null;
		Object result = null;
		Throwable ex = null;

		try {
			service = findService(serviceURI);

			tempObject = RuntimeContext.beginExecution(service, arg, TrackingManager.EXECUTE_MODE_SYNC, now);

			if (null == service) {
				throw new ServiceException("service does not exist: " + serviceURI);// 上抛异常
			}

			if (null != aop) {
				aop.execBefore(service, arg);// 前置切面
			}

			// TODO 如果是XCO 需要考虑入参的只读特性,:::不需要
			Object data = converter.parameterConvert(arg, service.getResultType()); // TODO 有可能发生异常
			service.execute(context, data);
			result = service.getResult(context); // TODO 类型转换时可能发生异常

			// fix bug: 结果需要预先处理, AOP, Tracking保持一致
			result = getResult(result, ignoreWrapper);

			if (null != aop) {
				aop.execAfter(null, service, arg, result, ex, true);
			}
		} catch (Throwable e) {
			ex = e;
			result = getExceptionResult(e);// 防止异常处理后的返回
		} finally {
			// 新增后置处理
			ServiceException throwEx = null;
			try {
				if (null != ex) {
					onException(ex, serviceURI, service, true);
				} else {
					onSuccess();
				}
			} catch (ServiceException se) {
				// 这里的异常, 有可能是onSuccess的, 也有可能是onException的, 但无论如何, 都已经打印过了
				throwEx = se;
				if (null == ex) {
					ex = se;// fix bug: help aop
				}

				// help aop and tracking
				result = getExceptionResult(se);

			}
			if (null != aop) {
				aop.execAfter(context, service, arg, result, ex, false);
			}

			RuntimeContext.endExecution(tempObject, result, ex);

			if (null != throwEx) {
				throw throwEx;
			}
		}
		// return (T) getResult(result, ignoreWrapper);
		return (T) result;
	}

	/**
	 * 单独环境
	 */
	public static <T> T executeAlone(String serviceURI, Object arg) throws ServiceException {
		return executeAlone(serviceURI, arg, false);
	}

	public static <T> T executeAlone(String serviceURI, Object arg, boolean ignoreWrapper) throws ServiceException {
		return executeAlone(serviceURI, arg, false, ignoreWrapper, null, null);
	}

	private static <T> T executeAlone(String serviceURI, Object arg, ServiceContext parent, Integer executeMode) throws ServiceException {
		return executeAlone(serviceURI, arg, false, false, parent, executeMode);
	}

	@SuppressWarnings("unchecked")
	private static <T> T executeAlone(String serviceURI, Object arg, boolean throwException, boolean ignoreWrapper, ServiceContext parent,
			Integer executeMode) throws ServiceException {

		check();// 检查系统是否已经正在关闭中了

		log.info("execute alone service: " + serviceURI);

		long now = System.currentTimeMillis();

		ServiceContext context = begin(true, parent);

		Object tempObject = null;
		AbstractServiceNode service = null;
		Object result = null;
		Throwable ex = null;

		try {

			service = findService(serviceURI);

			tempObject = RuntimeContext.beginExecution(service, arg, executeMode, now);

			if (null == service) {
				throw new ServiceException("service does not exist: " + serviceURI);
			}

			if (null != aop) {
				aop.execBefore(service, arg);// 前置切面
			}

			Object data = converter.parameterConvert(arg, service.getResultType());
			service.execute(context, data);
			result = service.getResult(context);

			// fix bug: 结果需要预先处理, AOP, Tracking保持一致
			result = getResult(result, ignoreWrapper);

			if (null != aop) {
				aop.execAfter(null, service, arg, result, ex, true);
			}

		} catch (Throwable e) {
			ex = e;
			result = getExceptionResult(e);
		} finally {
			// 新增后置处理
			ServiceException throwEx = null;
			try {
				if (null != ex) {
					onException(ex, serviceURI, service, false);
				} else {
					onSuccess();
				}
			} catch (ServiceException se) {
				// 这里产生的异常只能是onSuccess()抛出的
				throwEx = se;

				// 防止提交产生新异常
				result = getExceptionResult(se);

				// fix bug: help aop
				if (null == ex) {
					ex = se;
				}
			}
			if (null != aop) {
				aop.execAfter(context, service, arg, result, ex, false);
			}

			RuntimeContext.endExecution(tempObject, result, ex);

			if (null != throwEx) {
				if (throwException) {
					throw throwEx;
				} else {
					// 这里有可能打印重复(commit异常)
					log.error("execute service exception: " + serviceURI, throwEx);
				}
			}
		}

		// return (T) getResult(result, ignoreWrapper);
		return (T) result;
	}

	/**
	 * 单独环境, 异步执行
	 */
	public static void executeAsync(final String serviceURI, final Object arg, final ServiceContext parent) {

		check();
		log.info("execute async service: " + serviceURI);

		final RuntimeContext rc = RuntimeContext.get();

		TangYuanContainer.getInstance().getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				// 添加上下文记录
				RuntimeContext.beginFromContext(rc);
				// execute
				executeAlone(serviceURI, arg, parent, TrackingManager.EXECUTE_MODE_ASYN);
				// 清理上下文记录
				RuntimeContext.clean();
			}
		});
	}

	/**
	 * 单独环境, 异步执行
	 */
	public static void executeAsync(final String serviceURI, final Object arg) {

		check();
		log.info("execute async service: " + serviceURI);

		final ServiceContext parent = getServiceContext();

		final RuntimeContext rc = RuntimeContext.get();

		TangYuanContainer.getInstance().getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				// 添加上下文记录
				RuntimeContext.beginFromContext(rc);
				// execute
				executeAlone(serviceURI, arg, parent, TrackingManager.EXECUTE_MODE_ASYN);
				// 清理上下文记录
				RuntimeContext.clean();
			}
		});
	}

	/* ===================================Map Reduce====================================== */

	public static <T> T executeMapReduce(String serviceURI, List<Object> args, MapReduceHander handler, long timeout) throws ServiceException {
		return executeMapReduce(null, serviceURI, args, handler, timeout);
	}

	public static <T> T executeMapReduce(Object mapReduceContext, String serviceURI, List<Object> args, MapReduceHander handler, long timeout)
			throws ServiceException {
		return executeMapReduce(mapReduceContext, serviceURI, args, handler, timeout, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> T executeMapReduce(Object mapReduceContext, String serviceURI, List<Object> args, MapReduceHander handler, long timeout,
			boolean ignoreWrapper) throws ServiceException {
		check();
		Object result = null;
		ServiceContext parent = getServiceContext();
		RuntimeContext rc = RuntimeContext.get();
		try {
			int size = args.size();
			for (int i = 0; i < size; i++) {
				executeMapReduce0(mapReduceContext, serviceURI, args.get(i), handler, parent, rc);
			}
			result = handler.getResult(mapReduceContext, timeout);

		} catch (Throwable e) {
			result = getExceptionResult(e);// 防止异常处理后的返回
		}
		return (T) getResult(result, ignoreWrapper);
	}

	public static <T> T executeMapReduce(List<String> services, List<Object> args, MapReduceHander handler, long timeout) throws ServiceException {
		return executeMapReduce(null, services, args, handler, timeout);
	}

	public static <T> T executeMapReduce(Object mapReduceContext, List<String> services, List<Object> args, MapReduceHander handler, long timeout)
			throws ServiceException {
		return executeMapReduce(mapReduceContext, services, args, handler, timeout, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> T executeMapReduce(Object mapReduceContext, List<String> services, List<Object> args, MapReduceHander handler, long timeout,
			boolean ignoreWrapper) throws ServiceException {
		check();
		Object result = null;
		ServiceContext parent = getServiceContext();
		RuntimeContext rc = RuntimeContext.get();
		try {
			int size = services.size();
			for (int i = 0; i < size; i++) {
				executeMapReduce0(mapReduceContext, services.get(i), args.get(i), handler, parent, rc);
			}
			result = handler.getResult(mapReduceContext, timeout);
		} catch (Throwable e) {
			result = getExceptionResult(e);// 防止异常处理后的返回
		}
		return (T) getResult(result, ignoreWrapper);
	}

	///////////////////////////////

	private static void executeMapReduce0(final Object context, final String service, final Object arg, final MapReduceHander handler,
			final ServiceContext parent, final RuntimeContext rc) {
		TangYuanContainer.getInstance().getThreadPool().execute(new Runnable() {
			@Override
			public void run() {

				// 添加上下文记录
				RuntimeContext.beginFromContext(rc);

				try {
					Object result = ServiceActuator.executeAlone(service, arg, parent, TrackingManager.EXECUTE_MODE_ASYN);
					handler.merge(context, service, result);
				} catch (Throwable e) {
					log.error(e);
				}

				// 清理上下文记录
				RuntimeContext.clean();
			}
		});
	}
}
