package org.xson.tangyuan.mongo.datasource;

import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;

public class SimpleMongoDataSourceManager implements MongoDataSourceManager {

	private String					creator			= null;

	private AbstractMongoDataSource	dataSource		= null;

	private String					dataSourceId	= null;

	public SimpleMongoDataSourceManager(String creator, AbstractMongoDataSource dataSource, String dataSourceId) {
		this.creator = creator;
		this.dataSource = dataSource;
		this.dataSourceId = dataSourceId;
	}

	@Override
	public boolean isValidDsKey(String dsKey) {
		return dsKey.equals(this.dataSourceId);
	}

	@Override
	public DBCollection getCollection(String dsKey, String collection) {
		return dataSource.getCollection(collection);
	}

	@Override
	public void close() {
		dataSource.close(creator);
	}

	@Override
	public WriteConcern getDefaultWriteConcern(String dsKey) {
		return dataSource.getDefaultWriteConcern();
	}
}
