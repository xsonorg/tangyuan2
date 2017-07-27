package org.xson.tangyuan.sql.executor;

import org.xson.tangyuan.executor.IServiceContext;
import org.xson.tangyuan.executor.ServiceContextFactory;

public class SqlServiceContextFactory implements ServiceContextFactory {

	@Override
	public IServiceContext create() {
		return new SqlServiceContext();
	}

}
