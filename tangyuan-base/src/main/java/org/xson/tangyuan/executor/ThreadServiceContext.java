package org.xson.tangyuan.executor;

import java.util.LinkedList;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;

/**
 * 线程中的上下文
 */
public class ThreadServiceContext {

	private static Log					log	= LogFactory.getLog(ThreadServiceContext.class);

	/** 单个上下文 */
	private ServiceContext				context;
	/** 单个上队列 */
	private LinkedList<ServiceContext>	contextQueue;

	//	/**
	//	 * 获取或者创建新的上下文
	//	 * 
	//	 * @param isNew 是否创建新的上下文
	//	 */
	//	public ServiceContext getOrCreate(boolean isNew) {
	//		// 最初
	//		if (null == context && null == contextQueue) {
	//			context = new ServiceContext();
	//			log.debug("open a new context. hashCode[" + context.hashCode() + "]");
	//			return context;
	//		}
	//		ServiceContext returnContext = null;
	//		// 之后的情况
	//		if (isNew) {
	//			if (null != context && null == contextQueue) {
	//				contextQueue = new LinkedList<ServiceContext>();
	//				contextQueue.push(context);
	//				context = null;
	//
	//				ServiceContext newContext = new ServiceContext();
	//				log.debug("open a new context. hashCode[" + newContext.hashCode() + "] in new");
	//				contextQueue.push(newContext);
	//
	//				returnContext = newContext;
	//			} else if (null == context && null != contextQueue) {
	//				ServiceContext newContext = new ServiceContext();
	//				log.debug("open a new context. hashCode[" + newContext.hashCode() + "] in new");
	//				contextQueue.push(newContext);
	//
	//				returnContext = newContext;
	//			} else {
	//				throw new ServiceException("Wrong context and contextQueue in new");
	//			}
	//		} else {
	//			if (null != this.context) {
	//				returnContext = this.context;
	//			} else if (null != this.contextQueue) {
	//				returnContext = this.contextQueue.peek();
	//			} else {
	//				throw new ServiceException("Wrong context and contextQueue");
	//			}
	//
	//			log.debug("follow an existing context. hashCode[" + returnContext.hashCode() + "], context[" + (returnContext.counter + 1) + "]");
	//			returnContext.counter++;
	//		}
	//
	//		return returnContext;
	//	}

	/**
	 * 获取或者创建新的上下文
	 * 
	 * @param isNew 是否创建新的上下文
	 */
	public ServiceContext getOrCreate(boolean isNew, ServiceContext parent) {
		// 最初
		if (null == context && null == contextQueue) {
			context = new ServiceContext(parent);
			log.debug("open a new context. hashCode[" + context.hashCode() + "]");
			return context;
		}
		ServiceContext returnContext = null;
		// 之后的情况
		if (isNew) {
			if (null != context && null == contextQueue) {

				parent = context;

				contextQueue = new LinkedList<ServiceContext>();
				contextQueue.push(context);
				context = null;

				ServiceContext newContext = new ServiceContext(parent);
				log.debug("open a new context. hashCode[" + newContext.hashCode() + "] in new");
				contextQueue.push(newContext);

				returnContext = newContext;
			} else if (null == context && null != contextQueue) {

				parent = this.contextQueue.peek();

				ServiceContext newContext = new ServiceContext(parent);
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
