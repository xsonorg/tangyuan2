package org.xson.tangyuan.executor;

import java.util.concurrent.atomic.AtomicLong;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.monitor.ServiceDeadlockInfo;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class ServiceContext {

	private static Log				log					= LogFactory.getLog(ServiceContext.class);

	/** 当前容器持有的上下文数量,关闭时候使用 */
	protected static AtomicLong		globleCounter		= new AtomicLong(0);

	private IServiceContext			sqlServiceContext	= null;
	private IServiceContext			javaServiceContext	= null;
	private IServiceContext			mongoServiceContext	= null;
	private IServiceContext			mqServiceContext	= null;
	private IServiceContext			esServiceContext	= null;

	private IServiceContext			hbaseServiceContext	= null;
	private IServiceContext			hiveServiceContext	= null;

	/** 使用计数器 */
	protected int					counter				= 1;

	/** 结果返回对象:组合服务专用 */
	private Object					result				= null;

	/** 服务执行过程中的异常辅助信息 */
	private IServiceExceptionInfo	exceptionInfo		= null;

	/** 监控信息 */
	private ServiceDeadlockInfo		contextInfo			= null;

	public ServiceContext() {
		if (TangYuanContainer.getInstance().isOpenDeadlockMonitor()) {
			this.contextInfo = new ServiceDeadlockInfo(this.hashCode());// TODO
			this.contextInfo.joinMonitor();
		}
		globleCounter.getAndIncrement();
	}

	/**
	 * 更新监控信息
	 */
	public void updateMonitor(String service) {
		if (null != contextInfo) {
			contextInfo.update(service);
		}
	}

	/**
	 * 停止监控信息
	 */
	public void stopMonitor() {
		if (null != contextInfo) {
			contextInfo.stop();
		}
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public void setExceptionInfo(IServiceExceptionInfo exceptionInfo) {
		this.exceptionInfo = exceptionInfo;
	}

	public IServiceExceptionInfo getExceptionInfo() {
		return exceptionInfo;
	}

	public IServiceContext getSqlServiceContext() {
		return getServiceContext(TangYuanServiceType.SQL);
	}

	public IServiceContext getServiceContext(TangYuanServiceType type) {
		if (TangYuanServiceType.SQL == type) {
			if (null == sqlServiceContext) {
				sqlServiceContext = TangYuanContainer.getInstance().getContextFactory(type).create();
			}
			return sqlServiceContext;
		} else if (TangYuanServiceType.MONGO == type) {
			if (null == mongoServiceContext) {
				mongoServiceContext = TangYuanContainer.getInstance().getContextFactory(type).create();
			}
			return mongoServiceContext;
		} else if (TangYuanServiceType.JAVA == type) {
			if (null == javaServiceContext) {
				javaServiceContext = TangYuanContainer.getInstance().getContextFactory(type).create();
			}
			return javaServiceContext;
		} else if (TangYuanServiceType.HIVE == type) {
			if (null == hiveServiceContext) {
				hiveServiceContext = TangYuanContainer.getInstance().getContextFactory(type).create();
			}
			return hiveServiceContext;
		} else if (TangYuanServiceType.HBASE == type) {
			if (null == hbaseServiceContext) {
				hbaseServiceContext = TangYuanContainer.getInstance().getContextFactory(type).create();
			}
			return hbaseServiceContext;
		} else if (TangYuanServiceType.MQ == type) {
			if (null == mqServiceContext) {
				mqServiceContext = TangYuanContainer.getInstance().getContextFactory(type).create();
			}
			return mqServiceContext;
		} else if (TangYuanServiceType.ES == type) {
			if (null == esServiceContext) {
				esServiceContext = TangYuanContainer.getInstance().getContextFactory(type).create();
			}
			return esServiceContext;
		}

		// return getSqlServiceContext();
		// return null;
		// 返回某类型的上下文，无需记录在案
		return TangYuanContainer.getInstance().getContextFactory(type).create();
	}

	public void finish() throws Throwable {
		if (null != sqlServiceContext) {
			sqlServiceContext.commit(true);// 这里是最终的提交
			sqlServiceContext = null;
		}
		if (null != mongoServiceContext) {
			mongoServiceContext.commit(true);
			mongoServiceContext = null;
		}
		if (null != javaServiceContext) {
			javaServiceContext.commit(true);
			javaServiceContext = null;
		}

		if (null != hiveServiceContext) {
			hiveServiceContext.commit(true);
			hiveServiceContext = null;
		}
		if (null != hbaseServiceContext) {
			hbaseServiceContext.commit(true);
			hbaseServiceContext = null;
		}

		if (null != mqServiceContext) {
			mqServiceContext.commit(true);
			mqServiceContext = null;
		}

		if (null != esServiceContext) {
			esServiceContext.commit(true);
			esServiceContext = null;
		}

		globleCounter.getAndDecrement();
	}

	public void finishOnException() {
		if (null != sqlServiceContext) {
			sqlServiceContext.rollback();
			sqlServiceContext = null;
		}

		if (null != mongoServiceContext) {
			mongoServiceContext.rollback();
			mongoServiceContext = null;
		}

		if (null != javaServiceContext) {
			javaServiceContext.rollback();
			javaServiceContext = null;
		}

		if (null != hiveServiceContext) {
			hiveServiceContext.rollback();
			hiveServiceContext = null;
		}

		if (null != hbaseServiceContext) {
			hbaseServiceContext.rollback();
			hbaseServiceContext = null;
		}

		if (null != mqServiceContext) {
			mqServiceContext.rollback();
			mqServiceContext = null;
		}

		if (null != esServiceContext) {
			esServiceContext.rollback();
			esServiceContext = null;
		}

		this.exceptionInfo = null;
		globleCounter.getAndDecrement();
	}

	/**
	 * 异常发生时候入口方法
	 */
	public void onException(TangYuanServiceType type, Throwable e, String message) throws ServiceException {
		boolean canProcess = getServiceContext(type).onException(exceptionInfo);
		if (canProcess) {
			this.exceptionInfo = null;
			log.error(message, e);
		} else {
			//			if (e instanceof ServiceException) {
			//				throw (ServiceException) e;
			//			}
			//			throw new ServiceException(message, e);
			throw TangYuanUtil.getServiceException(e, message);
		}
	}

}
