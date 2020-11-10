package org.xson.tangyuan.mongo.datasource;

import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;

public abstract class AbstractMongoDataSource {

	protected String logicDataSourceId;

	protected String realDataSourceId;

	/** 创建者 */
	protected String creator;

	public String getLogicDataSourceId() {
		return logicDataSourceId;
	}

	public String getRealDataSourceId() {
		return realDataSourceId;
	}

	abstract public void close(String creator);

	abstract public DBCollection getCollection(String collection);

	abstract public WriteConcern getDefaultWriteConcern();
}
