package org.xson.tangyuan.hive.executor;

import org.xson.tangyuan.executor.IServiceContext;
import org.xson.tangyuan.executor.ServiceContextFactory;

public class HiveServiceContextFactory implements ServiceContextFactory {

	@Override
	public IServiceContext create() {
		return new HiveServiceContext();
	}

}
