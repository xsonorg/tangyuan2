package org.xson.tangyuan.mongo.executor.sql;

public interface SqlVo {

	public String toSQL();

	public String getTable();

	public void check();
}
