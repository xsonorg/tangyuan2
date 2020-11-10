package org.xson.tangyuan.service.context;

public class DefaultServiceContextFactory implements ServiceContextFactory {

	@Override
	public ServiceContext create() {
		return new DefaultServiceContext();
	}

}
