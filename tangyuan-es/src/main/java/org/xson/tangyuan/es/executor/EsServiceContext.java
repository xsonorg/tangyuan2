package org.xson.tangyuan.es.executor;

import org.xson.tangyuan.executor.DefaultServiceContext;

public class EsServiceContext extends DefaultServiceContext {

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

}
