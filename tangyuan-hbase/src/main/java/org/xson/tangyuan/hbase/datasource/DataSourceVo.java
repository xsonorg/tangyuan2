package org.xson.tangyuan.hbase.datasource;

import java.util.Map;

public class DataSourceVo {

	private String				id			= null;
	private Map<String, String>	properties	= null;
	protected String			sharedUse	= null;
	protected String			creator		= null;

	public DataSourceVo(String id, Map<String, String> properties, String sharedUse, String creator) {
		this.id = id;
		this.properties = properties;
		this.sharedUse = sharedUse;
		this.creator = creator;
	}

	public String getId() {
		return id;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public String getSharedUse() {
		return sharedUse;
	}

	public String getCreator() {
		return creator;
	}

	public HBaseDataSource start() throws Throwable {
		HBaseDataSource dataSource = new HBaseDataSource(this);
		dataSource.init();
		return dataSource;
	}
}
