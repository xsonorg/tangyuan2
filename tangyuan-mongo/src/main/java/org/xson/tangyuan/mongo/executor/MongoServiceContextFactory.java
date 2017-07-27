package org.xson.tangyuan.mongo.executor;

import org.xson.tangyuan.executor.IServiceContext;
import org.xson.tangyuan.executor.ServiceContextFactory;

public class MongoServiceContextFactory implements ServiceContextFactory {

	@Override
	public IServiceContext create() {
		return new MongoServiceContext();
	}

}
