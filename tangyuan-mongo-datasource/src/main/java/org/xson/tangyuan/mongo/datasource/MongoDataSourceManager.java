package org.xson.tangyuan.mongo.datasource;

import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;

public interface MongoDataSourceManager {

	public boolean isValidDsKey(String dsKey);

	public DBCollection getCollection(String dsKey, String collection);

	public void close();

	public WriteConcern getDefaultWriteConcern(String dsKey);
}
