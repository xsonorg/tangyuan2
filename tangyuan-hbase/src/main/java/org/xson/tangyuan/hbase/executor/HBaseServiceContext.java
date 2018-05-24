package org.xson.tangyuan.hbase.executor;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.executor.DefaultServiceContext;

public class HBaseServiceContext extends DefaultServiceContext {

	private HBaseActuator	actuator	= new HBaseActuator();

	private StringBuilder	sqlBuilder	= null;

	private List<String>	sqlList		= null;

	public void resetExecEnv() {
		this.sqlBuilder = new StringBuilder();
		this.sqlList = null;
	}

	public void addSql(String sql) {
		sqlBuilder.append(sql);
	}

	public String getSql() {
		return sqlBuilder.toString();
	}

	public void appendAndClean(String sql) {
		if (null == this.sqlList) {
			this.sqlList = new ArrayList<String>();
		}
		this.sqlList.add(sql);
		this.sqlBuilder = new StringBuilder();
	}

	public List<String> getSqlList() {
		return sqlList;
	}

	public HBaseActuator getActuator() {
		return actuator;
	}

}
