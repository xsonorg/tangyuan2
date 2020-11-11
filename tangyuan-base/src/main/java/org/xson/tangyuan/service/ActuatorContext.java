package org.xson.tangyuan.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.service.context.ServiceContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class ActuatorContext {

	/** 当前容器持有的上下文数量,关闭时候使用 */
	protected static AtomicLong globleCounter = new AtomicLong(0);
	/** 服务的组件上下文 */
	protected ServiceContext    sc            = null;
	/** 结果返回对象:组合服务专用 */
	protected Object            result        = null;
	protected List<Runnable>    postTaskList  = null;

	public static long getGlobleContextCount() {
		return globleCounter.get();
	}

	public ActuatorContext() {
		globleCounter.getAndIncrement();
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public ServiceContext getServiceContext(TangYuanServiceType type) {
		if (null == this.sc) {
			this.sc = TangYuanContainer.getInstance().getContextFactory(type).create();
		}
		return this.sc;
	}

	public void finish() throws Throwable {
		if (null != this.sc) {
			this.sc.commit(true);// 这里是最终的提交
			this.sc = null;
		}
	}

	public void finishOnException() {
		if (null != this.sc) {
			this.sc.rollback();
			this.sc = null;
		}
	}

	public void post(boolean flag) {
		if (flag) {
			processPostTask();
		} else {
			if (null != this.postTaskList) {
				this.postTaskList.clear();
				this.postTaskList = null;
			}
		}
		globleCounter.getAndDecrement();
	}

	/**
	 * 后置任务
	 * 
	 * @param r
	 */
	public void addPostTask(Runnable r) {
		if (null == this.postTaskList) {
			this.postTaskList = new ArrayList<>();
		}
		this.postTaskList.add(r);
	}

	private void processPostTask() {
		if (null != this.postTaskList) {
			for (Runnable r : this.postTaskList) {
				r.run();
			}
		}
	}

}
