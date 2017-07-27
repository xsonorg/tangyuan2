package org.xson.tangyuan.java.executor;

import org.xson.tangyuan.executor.DefaultServiceContext;
import org.xson.tangyuan.executor.IServiceContext;
import org.xson.tangyuan.executor.ServiceContextFactory;

public class JavaServiceContextFactory implements ServiceContextFactory {

	@Override
	public IServiceContext create() {
		return new DefaultServiceContext();
	}

}
