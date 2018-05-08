package org.xson.tangyuan.hbase.datasource;

import java.util.Map;

public class DataSourceVo {

	protected String				id			= null;
	protected Map<String, String>	properties	= null;
	protected String				sharedUse	= null;
	protected String				creator		= null;
	protected String				resource	= null;

	public DataSourceVo(String id, Map<String, String> properties, String sharedUse, String creator, String resource) {
		this.id = id;
		this.properties = properties;
		this.sharedUse = sharedUse;
		this.creator = creator;

		this.resource = resource;
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
