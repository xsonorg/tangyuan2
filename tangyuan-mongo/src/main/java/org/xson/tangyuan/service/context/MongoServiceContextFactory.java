package org.xson.tangyuan.service.context;

public class MongoServiceContextFactory implements ServiceContextFactory {

	@Override
	public ServiceContext create() {
		return new MongoServiceContext();
	}

}
