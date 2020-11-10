package org.xson.tangyuan.service.context;

public class DefaultServiceContext implements ServiceContext {

	@Override
	public void commit(boolean confirm) throws Throwable {
	}

	@Override
	public void rollback() {
	}

	@Override
	public void onException(Object info) {
	}

	//	@Override
	//	public boolean onException(ServiceExceptionInfo exceptionInfo) throws ServiceException {
	//		// 这里不能处理任务错误,统一上抛
	//		return false;
	//	}
}
