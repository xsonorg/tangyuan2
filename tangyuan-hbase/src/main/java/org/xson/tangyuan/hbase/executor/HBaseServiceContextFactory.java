package org.xson.tangyuan.hbase.executor;

import org.xson.tangyuan.executor.IServiceContext;
import org.xson.tangyuan.executor.ServiceContextFactory;

public class HBaseServiceContextFactory implements ServiceContextFactory {

	@Override
	public IServiceContext create() {
		return new HBaseServiceContext();
	}

}
