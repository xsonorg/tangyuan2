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

}
