package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.executor.ServiceContext;

public interface TangYuanNode {

	/**
	 * 执行当前节点
	 * 
	 * @param context
	 * @param arg
	 *            support map and xco
	 * @return
	 * @throws Throwable
	 */
	boolean execute(ServiceContext context, Object arg) throws Throwable;
}
