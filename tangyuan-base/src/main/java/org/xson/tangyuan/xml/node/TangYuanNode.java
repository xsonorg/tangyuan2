package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.service.ActuatorContext;

public interface TangYuanNode {

	/**
	 * 执行服务
	 * 
	 * @param ac
	 *            执行上下文
	 * @param arg
	 *            参数
	 * @param acArg
	 *            执行上下文参数
	 * @return
	 * @throws Throwable
	 */
	boolean execute(ActuatorContext ac, Object arg, Object acArg) throws Throwable;
}
