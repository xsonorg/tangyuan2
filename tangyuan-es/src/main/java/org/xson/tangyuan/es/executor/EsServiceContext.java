package org.xson.tangyuan.es.executor;

import org.xson.tangyuan.executor.IServiceContext;
import org.xson.tangyuan.executor.IServiceExceptionInfo;
import org.xson.tangyuan.executor.ServiceException;

public class EsServiceContext implements IServiceContext {

	private StringBuilder	sqlBuilder		= null;

	/* 忽略引号 */
	private boolean			ignoreQuotes	= false;

	public void resetExecEnv() {
		this.sqlBuilder = new StringBuilder();
	}

	public void addSql(String sql) {
		sqlBuilder.append(sql);
	}

	public String getSql() {
		return sqlBuilder.toString();
	}

	public void setIgnoreQuotes() {
		ignoreQuotes = true;
	}

	public boolean getIgnoreQuotes() {
		boolean tempIgnoreQuotes = this.ignoreQuotes;
		this.ignoreQuotes = false;
		return tempIgnoreQuotes;
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
