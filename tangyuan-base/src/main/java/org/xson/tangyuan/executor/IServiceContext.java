package org.xson.tangyuan.executor;

/**
 * 上下文接口
 */
public interface IServiceContext {

	/**
	 * 提交服务
	 * 
	 * @param confirm
	 *            是否是确定提交, 有具体实现判断
	 * @throws Throwable
	 */
	public void commit(boolean confirm) throws Throwable;

	/**
	 * 回滚服务
	 */
	public void rollback();

	/**
	 * 异常处理(上下文模式中调用), 如果不影响外部服务，可以内部处理，否则返回false
	 */
	public boolean onException(IServiceExceptionInfo exceptionInfo) throws ServiceException;

}
