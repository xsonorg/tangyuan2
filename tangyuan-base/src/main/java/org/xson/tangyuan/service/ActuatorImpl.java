package org.xson.tangyuan.service;

import java.util.List;

import org.xson.common.object.ActuatorContextXCO;
import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
//import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.aop.Aop;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.manager.ManagerComponent;
import org.xson.tangyuan.manager.TangYuanManager;
import org.xson.tangyuan.manager.trace.TraceManager;
import org.xson.tangyuan.rpc.RpcPlaceHolderHandler;
import org.xson.tangyuan.rpc.xml.node.RpcServiceNode;
import org.xson.tangyuan.service.converter.ServiceArgConverter;
import org.xson.tangyuan.service.converter.TangyuanServiceArgConverter;
import org.xson.tangyuan.service.mr.MapReduceHander;
import org.xson.tangyuan.service.runtime.RuntimeContext;
import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

/**
 * 服务调用执行器实现类
 */
public class ActuatorImpl {

	private static ActuatorImpl		instance				= null;

	private Log						log						= LogFactory.getLog(getClass());
	private boolean					onlyProxy				= false;
	private Aop						aop						= null;
	private RpcPlaceHolderHandler	rpcPlaceHolderHandler	= null;
	// TODO转移到外层
	private ServiceArgConverter		converter				= null;
	private TangYuanManager			tangYuanManager			= null;

	private TangYuanContainer		container				= null;

	private ActuatorImpl(boolean onlyProxy, Aop aop, RpcPlaceHolderHandler rpcPlaceHolderHandler, TangYuanManager tangYuanManager) {
		this.onlyProxy = onlyProxy;
		this.aop = aop;
		this.rpcPlaceHolderHandler = rpcPlaceHolderHandler;
		this.tangYuanManager = tangYuanManager;
		this.converter = new TangyuanServiceArgConverter();

		this.container = TangYuanContainer.getInstance();
	}

	public static ActuatorImpl create(boolean onlyProxy, Aop aop, RpcPlaceHolderHandler rpcPlaceHolderHandler, TangYuanManager tangYuanManager) {
		if (null == instance) {
			instance = new ActuatorImpl(onlyProxy, aop, rpcPlaceHolderHandler, tangYuanManager);
		}
		return instance;
	}

	protected Object cloneArg(AbstractServiceNode service, Object arg) {
		TangYuanServiceType serviceType = service.getServiceType();
		if (TangYuanServiceType.SQL == serviceType || TangYuanServiceType.MONGO == serviceType || TangYuanServiceType.ES == serviceType
				|| TangYuanServiceType.HIVE == serviceType || TangYuanServiceType.HBASE == serviceType) {
			return new ActuatorContextXCO((XCO) arg);
		}
		// NO(0), SQL(1), JAVA(2), MONGO(3), ES(4), MQ(5), HIVE(6), HBASE(7), PRCPROXY(8), VALIDATE(9), CACHE(10), WEB(99);
		return arg;
	}

	// =================================== execute ==========================================

	public <T> T execute(String serviceURI, Object arg) {
		return execute(serviceURI, arg, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T execute(String serviceURI, Object arg, Integer executeMode) {

		String mode = "";
		if (null != executeMode && TraceManager.EXECUTE_MODE_ASYN == executeMode) {
			mode = " async";
		}
		log.info("execute" + mode + " service: " + serviceURI);
		long now = System.currentTimeMillis();
		ActuatorContext context = null;
		Object traceContext = null;
		AbstractServiceNode service = null;
		Object result = null;
		Throwable ex = null;
		try {

			// checkContainerState();

			// 1. 查找服务
			service = findService(serviceURI);
			if (null == service) {
				throw new ServiceException("service does not exist: " + serviceURI);
			}
			// 2. 类型转换
			arg = converter.convert(arg); // other type --> xco
			// 3. 追加监控
			// appendTracking(service, arg);
			// 3. 开启监控（调整顺序，可能会有跟踪丢失，是在极端情况下）
			traceContext = startTracking(service, arg, executeMode, now);
			// 5. 状态判断
			checkService(service);
			// 6. 前置AOP执行
			if (null != aop) {
				aop.execBefore(service, arg);// 有可能抛出异常
			}
			// 7. 克隆参数
			Object acArg = cloneArg(service, arg);
			// 8. 服务执行
			context = new ActuatorContext();
			service.execute(context, arg, acArg);
			result = service.getResult(context);
			result = getResultWrapper(result);
		} catch (Throwable e) {
			log.error("execute service exception: " + serviceURI, e);
			ex = e;
			result = getResultExceptionWrapper(ex);
		}

		// 9. 服务提交(正常情况)
		if (null == ex) {
			try {
				context.finish();
			} catch (Throwable e) {
				log.error("finish service exception: " + serviceURI, e);
				ex = e;
				result = getResultExceptionWrapper(ex);
			}
		}

		// 9. 服务提交(异常情况)
		if (null != ex && null != context) {
			context.finishOnException();
		}

		// 10. 后续AOP处理
		if (null != aop) {
			try {
				aop.execAfter(service, arg, result, ex);
			} catch (Throwable e) {
				// 这里的异常仅用作流程控制，无需打印；具体的错误信息在after内部打印
				ex = e;
				result = getResultExceptionWrapper(ex);
			}
		}

		// 11. 上下文后置处理
		if (null != context) {
			context.post(null == ex);
		}

		// 12. 结束当前服务过程的跟踪
		endTracking(traceContext, result, ex);

		return (T) result;
	}

	// =================================== executeAsync ======================================

	public void executeAsync(String serviceURI, Object arg) {
		RuntimeContext rc = RuntimeContext.get();
		container.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				// 添加上下文记录
				RuntimeContext.begin(rc);
				// execute
				execute(serviceURI, arg, TraceManager.EXECUTE_MODE_ASYN);
				// 清理上下文记录
				RuntimeContext.clean();
			}
		});
	}

	// =================================== executeMapReduce ==================================

	public <T> T executeMapReduce(String serviceURI, List<Object> args, MapReduceHander handler, long timeout) {
		return executeMapReduce(null, serviceURI, args, handler, timeout);
	}

	@SuppressWarnings("unchecked")
	public <T> T executeMapReduce(Object mapReduceContext, String serviceURI, List<Object> args, MapReduceHander handler, long timeout) {

		// checkContainerState();

		if (CollectionUtils.isEmpty(args)) {
			throw new ServiceException("Invalid parameter 'args'");
		}
		Object result = null;
		RuntimeContext rc = RuntimeContext.get();
		try {
			int size = args.size();
			for (int i = 0; i < size; i++) {
				executeMapReduce0(mapReduceContext, serviceURI, args.get(i), handler, rc);
			}
			result = handler.getResult(mapReduceContext, timeout);
			result = getResultWrapper(result);
		} catch (Throwable e) {
			result = getResultExceptionWrapper(e);// 防止异常处理后的返回
		}
		return (T) result;
	}

	public <T> T executeMapReduce(List<String> services, List<Object> args, MapReduceHander handler, long timeout) {
		return executeMapReduce(null, services, args, handler, timeout);
	}

	@SuppressWarnings("unchecked")
	public <T> T executeMapReduce(Object mapReduceContext, List<String> services, List<Object> args, MapReduceHander handler, long timeout) {

		// checkContainerState();

		if (CollectionUtils.isEmpty(args)) {
			throw new ServiceException("Invalid parameter 'args'");
		}
		if (CollectionUtils.isEmpty(services)) {
			throw new ServiceException("Invalid parameter 'services'");
		}
		if (services.size() != args.size()) {
			throw new ServiceException("Invalid parameter 'args' or 'services'");
		}

		Object result = null;
		RuntimeContext rc = RuntimeContext.get();
		try {
			int size = args.size();
			for (int i = 0; i < size; i++) {
				executeMapReduce0(mapReduceContext, services.get(i), args.get(i), handler, rc);
			}
			result = handler.getResult(mapReduceContext, timeout);
			result = getResultWrapper(result);
		} catch (Throwable e) {
			result = getResultExceptionWrapper(e);// 防止异常处理后的返回
		}
		return (T) result;
	}

	private void executeMapReduce0(final Object context, final String service, final Object arg, final MapReduceHander handler,
			final RuntimeContext rc) {
		container.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				// 添加上下文记录
				RuntimeContext.begin(rc);
				Object result = execute(service, arg, TraceManager.EXECUTE_MODE_ASYN);
				handler.merge(context, service, result);
				// 清理上下文记录
				RuntimeContext.clean();
			}
		});
	}

	// =================================== runtime =========================================

	/**
	 * 开始服务监控跟踪
	 */
	private Object startTracking(AbstractServiceNode service, Object arg, Integer executeMode, long now) {
		if (null != this.tangYuanManager) {
			return this.tangYuanManager.startTracking(service.getServiceKey(), service.getServiceType().getVal(), arg, executeMode, now);
		}
		return null;
	}

	/**
	 * 结束服务监控跟踪
	 */
	private void endTracking(Object traceContext, Object result, Throwable ex) {
		if (null != this.tangYuanManager) {
			this.tangYuanManager.endTracking(traceContext, result, ex);
		}
	}

	// =================================== other =========================================

	/**
	 * 检查是否是还有存在的上下文中调用, 用于用户安全的关闭 应该只允许：内部调用，排除外部调用 ????
	 */
	private void checkService(AbstractServiceNode service) {
		if (null == this.tangYuanManager) {
			return;
		}
		if (TangYuanServiceType.PRCPROXY == service.getServiceType()) {
			return;
		}
		String serviceURI = service.getServiceKey();
		this.tangYuanManager.checkService(serviceURI);
		// if (!this.tangYuanManager.checkService(serviceURI)) {
		// throw new ServiceException(ManagerComponent.getInstance().getServiceErrorCode(), ManagerComponent.getInstance().getServiceErrorMessage());
		// }
	}

	protected void checkContainerState() {
		if (!container.isRunning()) {
			throw new ServiceException(ManagerComponent.getInstance().getAppErrorCode(), ManagerComponent.getInstance().getAppErrorMessage());
		}
	}

	private Object getResultWrapper(Object result) {
		return TangYuanUtil.retObjToXco(result);
	}

	private Object getResultExceptionWrapper(Throwable e) {
		return TangYuanUtil.getExceptionResult(e);
	}

	/**
	 * 获取服务<br />
	 * 
	 * a/b|www.xx.com/a/b|{xxx}/a/b
	 */
	private AbstractServiceNode findService(String serviceURI) {
		try {
			// 1. 是代理服务
			if (onlyProxy) {
				return findProxyService(serviceURI);
			}
			// 2. 查询本地服务
			AbstractServiceNode service = container.getService(serviceURI);
			if (null != service) {
				return service;
			}
			// 3. 查询远程服务
			service = container.getDynamicService(serviceURI);
			if (null != service) {
				return service;
			}
			// 处理本地占位URL
			if (null != this.rpcPlaceHolderHandler) {
				String newServiceURL = this.rpcPlaceHolderHandler.parse(serviceURI);
				if (null != newServiceURL) {
					return container.getService(newServiceURL);
				}
			}
			// 创建新的远程服务
			return createDynamicService(serviceURI);
		} catch (Throwable e) {
			log.error("Invalid serviceURI: " + serviceURI, e);
		}
		return null;
	}

	private AbstractServiceNode findProxyService(String serviceURI) {
		AbstractServiceNode service = container.getDynamicService(serviceURI);
		if (null != service) {
			return service;
		}
		return createDynamicService(serviceURI);
	}

	private AbstractServiceNode createDynamicService(String serviceURI) {
		// 是否是本地服务 optimize
		if (TangYuanUtil.isLocalService(serviceURI)) {
			return null;
		}
		// 优化
		AbstractServiceNode service = new RpcServiceNode(serviceURI);
		container.addDynamicService(service);
		if (null != aop) {
			aop.bindDynamic(service);
		}
		return service;
	}

}
