package org.xson.tangyuan.hive.datasource;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceManager {

	protected String defaultDsKey = null;

	protected String creator      = null;

	public boolean isValidDsKey(String dsKey) {
		return dsKey.equalsIgnoreCase(this.defaultDsKey);
	}

	public String getDefaultDsKey() {
		return defaultDsKey;
	}

	abstract public Connection getConnection(String dsKey) throws SQLException;

	abstract public void recycleConnection(String dsKey, Connection connection) throws SQLException;

	abstract public void close() throws SQLException;
}
