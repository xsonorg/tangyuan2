package org.xson.tangyuan.mongo.datasource;

import java.util.Map;

public class MongoDataSourceVo {

	private String				id;
	private Map<String, String>	properties;
	private boolean				defaultDs	= false;
	protected boolean			group		= false;
	// protected String jndiName = null;
	protected String			sharedUse	= null;
	protected String			creator		= null;

	public MongoDataSourceVo(String id, Map<String, String> properties, boolean defaultDs, String sharedUse, String creator) {
		this.id = id;
		this.properties = properties;
		this.defaultDs = defaultDs;
		this.sharedUse = sharedUse;
	}

	public String getId() {
		return id;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public boolean isDefaultDs() {
		return defaultDs;
	}

	public boolean isGroup() {
		return group;
	}

	public String getCreator() {
		return creator;
	}

	public String getSharedUse() {
		return sharedUse;
	}

	public void start(Map<String, MongoDataSourceVo> logicMap, Map<String, AbstractMongoDataSource> realMap) {
		new DataSourceCreaterFactory().newInstance(this.sharedUse).newInstance(this, logicMap, realMap);
	}

}
