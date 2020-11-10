package org.xson.tangyuan.service.context;

/**
 * 服务上下文接口
 */
public interface ServiceContext {

	/**
	 * 提交服务
	 * 
	 * @param confirm
	 *            是否是确定提交, 有具体实现判断
	 */
	public void commit(boolean confirm) throws Throwable;

	/**
	 * 回滚服务
	 */
	public void rollback();

	/**
	 * 发生异常是设置异常信息
	 * 
	 * @param info
	 */
	public void onException(Object info);

}
