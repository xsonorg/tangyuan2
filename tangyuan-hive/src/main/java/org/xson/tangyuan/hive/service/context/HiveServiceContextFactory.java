package org.xson.tangyuan.hive.service.context;

import org.xson.tangyuan.service.context.ServiceContext;
import org.xson.tangyuan.service.context.ServiceContextFactory;

public class HiveServiceContextFactory implements ServiceContextFactory {

	@Override
	public ServiceContext create() {
		return new HiveServiceContext();
	}

}
