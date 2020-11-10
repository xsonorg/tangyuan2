package org.xson.tangyuan.service.context;

public class EsServiceContextFactory implements ServiceContextFactory {

	@Override
	public ServiceContext create() {
		return new EsServiceContext();
	}

}
