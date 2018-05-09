package org.xson.tangyuan.executor;

import java.util.LinkedList;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.aop.AopSupport;
import org.xson.tangyuan.aop.AspectVo.PointCut;
import org.xson.tangyuan.mr.MapReduce;
import org.xson.tangyuan.mr.MapReduceHander;
import org.xson.tangyuan.ognl.convert.ParameterConverter;
import org.xson.tangyuan.rpc.RpcProxy;
import org.xson.tangyuan.rpc.RpcServiceNode;
import org.xson.tangyuan.task.AsyncTask;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

public class ServiceActuator {

	private static Log									log					= LogFactory.getLog(ServiceActuator.class);
	private static ThreadLocal<ThreadServiceContext>	contextThreadLocal	= new ThreadLocal<ThreadServiceContext>();
	private static ParameterConverter					converter			= new ParameterConverter();
	private static AopSupport							aop					= null;
	private static volatile boolean						running				= true;
	private static boolean								onlyProxy			= false;

	/** 线程中的上下文 */
	static class ThreadServiceContext {
		/** 单个上下文 */
		private ServiceContext				context;
		/** 单个上队列 */
		private LinkedList<ServiceContext>	contextQueue;

		/**
		 * 获取或者创建新的上下文
		 * 
		 * @param isNew 是否创建新的上下文
		 */
		public ServiceContext getOrCreate(boolean isNew) {
			// 最初
			if (null == context && null == contextQueue) {
				context = new ServiceContext();
				log.debug("open a new context. hashCode[" + context.hashCode() + "]");
				return context;
			}
			ServiceContext returnContext = null;
			// 之后的情况
			if (isNew) {
				if (null != context && null == contextQueue) {
					contextQueue = new LinkedList<ServiceContext>();
					contextQueue.push(context);
					context = null;

					ServiceContext newContext = new ServiceContext();
					log.debug("open a new context. hashCode[" + newContext.hashCode() + "] in new");
					contextQueue.push(newContext);

					returnContext = newContext;
				} else if (null == context && null != contextQueue) {
					ServiceContext newContext = new ServiceContext();
					log.debug("open a new context. hashCode[" + newContext.hashCode() + "] in new");
					contextQueue.push(newContext);

					returnContext = newContext;
				} else {
					throw new ServiceException("Wrong context and contextQueue in new");
				}
			} else {
				if (null != this.context) {
					returnContext = this.context;
				} else if (null != this.contextQueue) {
					returnContext = this.contextQueue.peek();
				} else {
					throw new ServiceException("Wrong context and contextQueue");
				}

				log.debug("follow an existing context. hashCode[" + returnContext.hashCode() + "], context[" + (returnContext.counter + 1) + "]");
				returnContext.counter++;
			}

			return returnContext;
		}

		/** 仅仅获取上下文 */
		public ServiceContext get() {
			if (null != this.context) {
				return this.context;
			} else if (null != this.contextQueue) {
				return this.contextQueue.peek();
			} else {
				return null;
			}
		}

		/**
		 * 回收上下文
		 * 
		 * @return true: 代表线程中需要删除
		 */
		public boolean recycle() {
			if (null != this.context) {
				this.context = null;
				return true;
			} else if (null != this.contextQueue) {
				this.contextQueue.pop();
				if (this.contextQueue.isEmpty()) {
					return true;
				}
			}
			return false;
		}
	}

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
	private static ServiceContext begin(boolean isNew) {
		ThreadServiceContext threadContext = contextThreadLocal.get();
		if (null == threadContext) {
			threadContext = new ThreadServiceContext();
			contextThreadLocal.set(threadContext);
		}
		return threadContext.getOrCreate(isNew);
	}

	/** 无异常的结束 */
	private static void endOnSuccess() {
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
						log.error("SqlService commit exception", e);
						// fix bug: 这里的异常需要上抛
						ex = e;
					}
				} else {
					context.finishOnException();
				}
				log.debug("close a context. hashCode[" + context.hashCode() + "]");
				context.stopMonitor();// stop monitor
				if (threadContext.recycle()) {
					contextThreadLocal.remove();
				}

				// fix bug: 这里的异常需要上抛
				if (null != ex) {
					if (ex instanceof ServiceException) {
						throw (ServiceException) ex;
					}
					throw new ServiceException(ex);
				}
			}
		} else {
			contextThreadLocal.remove();
		}
	}

	/** 发生异常的结束 */
	protected static void endOnException(Throwable throwable, AbstractServiceNode service) {
		// 如果当前可以处理,当前处理; 如果当前不能处理,上抛,不做日志输出
		ThreadServiceContext threadContext = contextThreadLocal.get();
		ServiceContext context = threadContext.get();
		if (--context.counter < 1) {
			context.finishOnException();
			log.error("Execute service exception: " + service.getServiceKey(), throwable);
			log.debug("close a context. hashCode[" + context.hashCode() + "] on exception");
			context.stopMonitor();
			if (threadContext.recycle()) {
				contextThreadLocal.remove();
			}
			// 最后一层抛出的异常
			if (throwable instanceof ServiceException) {
				throw (ServiceException) throwable;
			}
			throw new ServiceException("Execute service exception: " + service.getServiceKey(), throwable);
		} else {
			ServiceException ex = null;
			try {
				context.onException(service.getServiceType(), throwable, "Execute service exception: " + service.getServiceKey());
			} catch (ServiceException e) {
				ex = e;
			}
			if (null != ex) {
				throw ex;
			}
		}
	}

	protected static Object getResult(Object result, boolean ignoreWrapper) {
		if (ignoreWrapper) {
			return result;
		}
		boolean allServiceReturnXCO = TangYuanContainer.getInstance().isAllServiceReturnXCO();
		if (allServiceReturnXCO) {
			return TangYuanUtil.retObjToXco(result);
		}
		return result;
	}

	protected static Object getExceptionResult(Throwable e) {
		boolean allServiceReturnXCO = TangYuanContainer.getInstance().isAllServiceReturnXCO();
		if (allServiceReturnXCO) {
			return TangYuanUtil.getExceptionResult(e);
		}
		return null;// 发生错误, 一定是NULL
	}

	@SuppressWarnings("unchecked")
	public static <T> T execute(String serviceURI, Object arg, boolean ignoreWrapper) throws ServiceException {
		check();// 检查系统是否已经正在关闭中了
		log.info("actuator service: " + serviceURI);
		if (onlyProxy) {
			return (T) executeProxy(serviceURI, arg);
		}

		final AbstractServiceNode service = findService(serviceURI);
		if (null == service) {
			throw new ServiceException("Service does not exist: " + serviceURI);// 上抛异常
		}

		// Object data = converter.parameterConvert(arg, service.getResultType());

		if (null != aop) {
			aop.execBefore(service, arg, PointCut.BEFORE_CHECK);// 前置切面
			aop.execBefore(service, arg, PointCut.BEFORE_ALONE);// 前置切面
		}

		// A. 获取上下文
		ServiceContext context = begin(false);
		// B.执行服务
		context.updateMonitor(serviceURI);
		Object result = null;
		Throwable ex = null;
		try {

			if (null != aop) {
				aop.execBefore(service, arg, PointCut.BEFORE_JOIN);// 前置切面
			}

			Object data = converter.parameterConvert(arg, service.getResultType());
			service.execute(context, data);
			result = service.getResult(context);// 只有类型转换时发生异常

			if (null != aop) {
				aop.execAfter(service, arg, result, ex, PointCut.AFTER_JOIN);
			}

		} catch (Throwable e) {
			ex = e;
			result = getExceptionResult(e);// 防止异常处理后的返回
		} finally {
			// 新增后置处理
			ServiceException throwEx = null;
			try {
				if (null != ex) {
					endOnException(ex, service);
				} else {
					endOnSuccess();
				}
			} catch (ServiceException se) {
				throwEx = se;

				// fix bug: help aop
				if (null == ex) {
					ex = se;
				}
			}
			if (null != aop) {
				aop.execAfter(service, arg, result, ex, PointCut.AFTER_ALONE);
			}
			if (null != throwEx) {
				throw throwEx;
			}
		}
		// return (T) result;
		return (T) getResult(result, ignoreWrapper);
	}

	public static <T> T execute(String serviceURI, Object arg) throws ServiceException {
		return execute(serviceURI, arg, false);
	}

	/**
	 * 单独环境
	 */
	public static <T> T executeAlone(String serviceURI, Object arg) throws ServiceException {
		return executeAlone(serviceURI, arg, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> T executeAlone(String serviceURI, Object arg, boolean ignoreWrapper) throws ServiceException {
		return (T) executeAlone(serviceURI, arg, false, ignoreWrapper);
	}

	private static Object executeAlone(String serviceURI, Object arg, boolean throwException, boolean ignoreWrapper) throws ServiceException {
		check();// 检查系统是否已经正在关闭中了
		log.info("execute alone service: " + serviceURI);

		if (onlyProxy) {
			try {
				return executeProxy(serviceURI, arg);
			} catch (Throwable e) {
				log.error("Execute service exception: " + serviceURI, e);
				return getExceptionResult(e);
			}
		}

		final AbstractServiceNode service = findService(serviceURI);
		if (null == service) {
			throw new ServiceException("Service does not exist: " + serviceURI);
		}

		if (null != aop) {
			try {
				aop.execBefore(service, arg, PointCut.BEFORE_CHECK);// 前置切面
			} catch (Throwable e) {
				log.error("Execute aop exception on: " + serviceURI, e);
				return getExceptionResult(e);
			}
			aop.execBefore(service, arg, PointCut.BEFORE_ALONE);// 前置切面
		}

		ServiceContext context = begin(true);
		context.updateMonitor(serviceURI);// monitor
		Object result = null;
		Throwable ex = null;
		try {

			if (null != aop) {
				aop.execBefore(service, arg, PointCut.BEFORE_JOIN);// 前置切面
			}

			Object data = converter.parameterConvert(arg, service.getResultType());
			service.execute(context, data);
			result = service.getResult(context);// 只有类型转换是发生异常

			if (null != aop) {
				aop.execAfter(service, arg, result, ex, PointCut.AFTER_JOIN);
			}

		} catch (Throwable e) {
			ex = e;
			result = getExceptionResult(e);
		} finally {
			// 新增后置处理
			ServiceException throwEx = null;
			try {
				if (null != ex) {
					endOnException(ex, service);
				} else {
					endOnSuccess();
				}
			} catch (ServiceException se) {
				throwEx = se;

				// 防止提交产生新异常
				result = getExceptionResult(se);

				// fix bug: help aop
				if (null == ex) {
					ex = se;
				}
			}
			if (null != aop) {
				aop.execAfter(service, arg, result, ex, PointCut.AFTER_ALONE);
			}
			if (null != throwEx) {
				if (throwException) {
					throw throwEx;
				} else {
					log.error("Execute service exception: " + serviceURI, throwEx);
				}
			}
		}
		// return (T) result;
		return getResult(result, ignoreWrapper);
	}

	/**
	 * 单独环境, 异步执行
	 */
	public static void executeAsync(final String serviceURI, final Object arg) {
		check();

		log.info("execute async service: " + serviceURI);

		if (onlyProxy) {
			TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
				@Override
				public void run() {
					executeProxy(serviceURI, arg);
				}
			});
			return;
		}

		final AbstractServiceNode service = findService(serviceURI);
		if (null == service) {
			throw new ServiceException("Service does not exist: " + serviceURI);
		}

		if (null != aop) {
			// may be
			try {
				aop.execBefore(service, arg, PointCut.BEFORE_CHECK);// 前置切面
			} catch (Throwable e) {
				log.error("Execute aop exception on: " + serviceURI, e);
				return;
			}

			aop.execBefore(service, arg, PointCut.BEFORE_ALONE);// 前置切面
		}

		TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
			@Override
			public void run() {
				ServiceContext context = begin(false);// 这里是在新的线程中
				context.updateMonitor(serviceURI);// monitor
				Object result = null;
				Throwable ex = null;
				try {

					if (null != aop) {
						aop.execBefore(service, arg, PointCut.BEFORE_JOIN);// 前置切面
					}

					Object data = converter.parameterConvert(arg, service.getResultType());
					service.execute(context, data);
					result = service.getResult(context);

					if (null != aop) {
						aop.execAfter(service, arg, result, ex, PointCut.AFTER_JOIN);
					}

					// 这里是最终的提交
					context.finish();
					context.setResult(null);
				} catch (Throwable e) {
					ex = e;
					// 这里是最终的回滚
					context.finishOnException();
					log.error("Execute service exception: " + serviceURI, e);
				} finally {
					context.stopMonitor();
					contextThreadLocal.remove();
					log.debug("close a context. hashCode[" + context.hashCode() + "]");
					// 新增后置处理
					if (null != aop) {
						aop.execAfter(service, arg, result, ex, PointCut.AFTER_ALONE);
					}
				}
			}
		});
	}

	/**
	 * 获取服务<br />
	 * a/b|www.xx.com/a/b
	 */
	private static AbstractServiceNode findService(String serviceURL) {
		try {
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

	private static Object executeProxy(String serviceURI, Object arg) {
		Object result = null;
		try {
			result = RpcProxy.call(serviceURI, (XCO) arg);
		} catch (Throwable e) {
			if (e instanceof ServiceException) {
				throw (ServiceException) e;
			} else {
				throw new ServiceException(e);
			}
		}
		return result;
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

	/* ===================================Map Reduce====================================== */

	//	public static <T> T executeMapReduce(String serviceURI, List<Object> args, MapReduceHander handler, long timeout) throws ServiceException {
	//		return executeMapReduce(serviceURI, args, handler, timeout, false);
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	public static <T> T executeMapReduce(String serviceURI, List<Object> args, MapReduceHander handler, long timeout, boolean ignoreWrapper)
	//			throws ServiceException {
	//		// 检查系统是否已经正在关闭中了
	//		check();
	//		Object result = null;
	//		try {
	//			result = MapReduce.execute(serviceURI, args, handler, timeout, true);
	//		} catch (Throwable e) {
	//			result = getExceptionResult(e);// 防止异常处理后的返回
	//		}
	//		return (T) getResult(result, ignoreWrapper);
	//	}
	//
	//	public static <T> T executeMapReduce(List<String> services, List<Object> args, MapReduceHander handler, long timeout) throws ServiceException {
	//		return executeMapReduce(services, args, handler, timeout, false);
	//	}
	//
	//	public static <T> T executeMapReduce(List<String> services, List<Object> args, MapReduceHander handler, long timeout, boolean ignoreWrapper)
	//			throws ServiceException {
	//		return null;
	//	}

	public static <T> T executeAloneMapReduce(String serviceURI, List<Object> args, MapReduceHander handler, long timeout) throws ServiceException {
		return executeAloneMapReduce(serviceURI, args, handler, timeout, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> T executeAloneMapReduce(String serviceURI, List<Object> args, MapReduceHander handler, long timeout, boolean ignoreWrapper)
			throws ServiceException {
		check();
		Object result = null;
		try {
			result = MapReduce.execute(serviceURI, args, handler, timeout, true);
		} catch (Throwable e) {
			result = getExceptionResult(e);// 防止异常处理后的返回
		}
		return (T) getResult(result, ignoreWrapper);
	}

	public static <T> T executeAloneMapReduce(List<String> services, List<Object> args, MapReduceHander handler, long timeout)
			throws ServiceException {
		return executeAloneMapReduce(services, args, handler, timeout, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> T executeAloneMapReduce(List<String> services, List<Object> args, MapReduceHander handler, long timeout, boolean ignoreWrapper)
			throws ServiceException {
		check();
		Object result = null;
		try {
			result = MapReduce.execute(services, args, handler, timeout, true);
		} catch (Throwable e) {
			result = getExceptionResult(e);// 防止异常处理后的返回
		}
		return (T) getResult(result, ignoreWrapper);
	}
}
