package org.xson.tangyuan.service.context;

public class ValidateServiceContextFactory implements ServiceContextFactory {

	@Override
	public ServiceContext create() {
		return new DefaultServiceContext();
	}

}
