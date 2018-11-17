package org.xson.tangyuan.executor;

import java.util.concurrent.atomic.AtomicLong;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.trace.TrackingContext;
import org.xson.tangyuan.trace.TrackingManager;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
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

	/** 服务追踪信息 */
	private Object					parentTrackingVo	= null;

	private TrackingManager			trackingManager		= null;

	public ServiceContext(ServiceContext parent) {
		if (null != parent) {
			this.parentTrackingVo = parent.parentTrackingVo;
		}
		this.trackingManager = TangYuanContainer.getInstance().getTrackingManager();
		globleCounter.getAndIncrement();
	}

	private int getTrackingServiceType(AbstractServiceNode service) {
		if (null == service) {
			return TrackingManager.SERVICE_TYPE_NO;
		}
		TangYuanServiceType type = service.getServiceType();
		if (TangYuanServiceType.SQL == type) {
			return TrackingManager.SERVICE_TYPE_SQL;
		} else if (TangYuanServiceType.MONGO == type) {
			return TrackingManager.SERVICE_TYPE_MONGO;
		} else if (TangYuanServiceType.JAVA == type) {
			return TrackingManager.SERVICE_TYPE_JAVA;
		} else if (TangYuanServiceType.HIVE == type) {
			return TrackingManager.SERVICE_TYPE_HIVE;
		} else if (TangYuanServiceType.HBASE == type) {
			return TrackingManager.SERVICE_TYPE_HBASE;
		} else if (TangYuanServiceType.MQ == type) {
			return TrackingManager.SERVICE_TYPE_MQ;
		} else if (TangYuanServiceType.ES == type) {
			return TrackingManager.SERVICE_TYPE_ES;
		}
		return TrackingManager.SERVICE_TYPE_NO;
	}

	public TrackingContext startTracking(String serviceURI, Object arg, Integer executeMode, long now, AbstractServiceNode service) {
		if (null == this.trackingManager) {
			return null;
		}
		if (null == this.parentTrackingVo) {
			this.parentTrackingVo = this.trackingManager.initTracking(arg, now);
		}
		if (null != service) {
			if (TangYuanServiceType.PRCPROXY == service.getServiceType() || TangYuanServiceType.MQ == service.getServiceType()) {
				return null;
			}
		}
		try {
			int serviceType = getTrackingServiceType(service);
			//			Object currentVo = this.trackingManager.startTracking(this.parentTrackingVo, serviceURI, arg, serviceType,
			//					TrackingManager.RECORD_TYPE_SERVICE, executeMode, now);
			Object currentVo = this.trackingManager.startTracking(this.parentTrackingVo, serviceURI, arg, null, serviceType,
					TrackingManager.RECORD_TYPE_SERVICE, executeMode, now);

			TrackingContext trackingContext = new TrackingContext(this.parentTrackingVo, currentVo);
			this.parentTrackingVo = currentVo;
			return trackingContext;
		} catch (Throwable e) {
			log.error("start tracking error.", e);
			return null;
		}
	}

	public void endTracking(TrackingContext trackingContext, Object result, Throwable ex) {
		if (null == this.trackingManager) {
			return;
		}
		if (null == trackingContext) {
			return;
		}
		try {
			this.trackingManager.endTracking(trackingContext.getCurrent(), result, ex);
			this.parentTrackingVo = trackingContext.getParent();
		} catch (Throwable e) {
			log.error("end tracking error.", e);
		}
	}

	public void addTrackingHeader(Object arg) {
		if (null == this.trackingManager) {
			return;
		}
		try {
			this.trackingManager.addTrackingHeader(this.parentTrackingVo, arg);
		} catch (Throwable e) {
			log.error("add tracking header error.", e);
		}
	}

	public void cleanTrackingHeader(Object arg) {
		if (null == this.trackingManager) {
			return;
		}
		try {
			this.trackingManager.cleanTrackingHeader(arg);
		} catch (Throwable e) {
			log.error("clean tracking header error.", e);
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
			throw TangYuanUtil.getServiceException(e, message);
		}
	}

	//	public TrackingContext startTracking0(String serviceURI, Object arg, Integer executeMode) {
	//		if (null == this.trackingManager) {
	//			return null;
	//		}
	//		if (null == this.parentTrackingVo) {
	//			this.parentTrackingVo = this.trackingManager.initTracking(arg);
	//		}
	//		try {
	//			Object currentVo = this.trackingManager.startTracking(this.parentTrackingVo, serviceURI, arg, null, TrackingManager.RECORD_TYPE_SERVICE,
	//					executeMode);
	//			TrackingContext trackingContext = new TrackingContext(this.parentTrackingVo, currentVo);
	//			this.parentTrackingVo = currentVo;
	//			return trackingContext;
	//		} catch (Throwable e) {
	//			log.error("start tracking error.", e);
	//			return null;
	//		}
	//	}

	//	public TrackingContext checkIgnoreTracking(TrackingContext trackingContext, AbstractServiceNode service) {
	//		if (null == this.trackingManager) {
	//			return null;
	//		}
	//		if (null == trackingContext) {
	//			return null;
	//		}
	//		if (TangYuanServiceType.PRCPROXY == service.getServiceType() || TangYuanServiceType.MQ == service.getServiceType()) {
	//			this.parentTrackingVo = trackingContext.getParent();
	//			return null;
	//		}
	//		this.trackingManager.setTracking(trackingContext.getCurrent(), service);
	//		return trackingContext;
	//	}
}
