package org.xson.tangyuan.service.context;

public class JavaServiceContextFactory implements ServiceContextFactory {

	@Override
	public ServiceContext create() {
		return new DefaultServiceContext();
	}

}
