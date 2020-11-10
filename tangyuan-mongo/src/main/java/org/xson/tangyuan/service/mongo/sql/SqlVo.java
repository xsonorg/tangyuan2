package org.xson.tangyuan.service.mongo.sql;

public interface SqlVo {

	public String toSQL();

	public String getTable();

	public void check();
}
