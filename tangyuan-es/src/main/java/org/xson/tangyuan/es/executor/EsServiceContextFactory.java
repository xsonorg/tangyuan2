package org.xson.tangyuan.es.executor;

import org.xson.tangyuan.executor.IServiceContext;
import org.xson.tangyuan.executor.ServiceContextFactory;

public class EsServiceContextFactory implements ServiceContextFactory {

	@Override
	public IServiceContext create() {
		return new EsServiceContext();
	}

}
