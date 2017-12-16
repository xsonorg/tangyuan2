package org.xson.tangyuan.es.executor;

import org.xson.tangyuan.executor.IServiceContext;
import org.xson.tangyuan.executor.IServiceExceptionInfo;
import org.xson.tangyuan.executor.ServiceException;

public class EsServiceContext implements IServiceContext {

	private StringBuilder sqlBuilder = null;

	public void resetExecEnv() {
		this.sqlBuilder = new StringBuilder();
	}

	public void addSql(String sql) {
		sqlBuilder.append(sql);
	}

	public String getSql() {
		// return sqlBuilder.toString().trim();
		return sqlBuilder.toString();
	}

	@Override
	public void commit(boolean confirm) throws Throwable {
	}

	@Override
	public void rollback() {
	}

	@Override
	public boolean onException(IServiceExceptionInfo exceptionInfo) throws ServiceException {
		// 这里不能处理任务错误,统一上抛
		return false;
	}

}
