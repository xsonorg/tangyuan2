package org.xson.tangyuan.sql.datasource;

import java.util.Map;

public class DataSourceVo {

	public enum ConnPoolType {
		JNDI, C3P0, DBCP, PROXOOL, DRUID, SHARE
	}

	private String				id			= null;
	private Map<String, String>	properties	= null;
	private boolean				defaultDs	= false;
	protected ConnPoolType		type		= null;
	protected boolean			group		= false;
	// protected String jndiName = null;
	protected String			sharedUse	= null;
	protected String			creator		= null;

	public DataSourceVo(String id, ConnPoolType type, boolean defaultDs, Map<String, String> properties, String sharedUse, String creator) {
		this.id = id;
		this.type = type;
		this.defaultDs = defaultDs;
		this.properties = properties;
		// this.jndiName = jndiName;
		this.sharedUse = sharedUse;
	}

	public String getId() {
		return id;
	}

	protected ConnPoolType getType() {
		return type;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public boolean isGroup() {
		return group;
	}

	public boolean isDefaultDs() {
		return defaultDs;
	}

	// public String getJndiName() {
	// return jndiName;
	// }

	public String getSharedUse() {
		return sharedUse;
	}

	public String getCreator() {
		return creator;
	}

	public void start(Map<String, DataSourceVo> logicMap, Map<String, AbstractDataSource> realMap) {
		new DataSourceCreaterFactory().newInstance(type).newInstance(this, logicMap, realMap);
	}
}
