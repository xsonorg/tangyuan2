package org.xson.tangyuan.hive.datasource;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDataSource {

	protected boolean	group;

	protected boolean	supportXA;

	protected boolean	supportSavepoint;

	protected String	logicDataSourceId;

	protected String	realDataSourceId;

	/** 创建者 */
	protected String	creator;

	public boolean isGroup() {
		return this.group;
	}

	public boolean isSupportXA() {
		return this.supportXA;
	}

	public boolean isSupportSavepoint() {
		return supportSavepoint;
	}

	public String getLogicDataSourceId() {
		return logicDataSourceId;
	}

	public String getRealDataSourceId() {
		return realDataSourceId;
	}

	abstract public Connection getConnection(String dsKey) throws SQLException;

	abstract public void recycleConnection(Connection connection) throws SQLException;

	abstract public void close(String creator) throws SQLException;

}
