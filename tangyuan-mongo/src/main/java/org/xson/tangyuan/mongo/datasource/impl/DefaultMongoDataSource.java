package org.xson.tangyuan.mongo.datasource.impl;

import org.xson.tangyuan.mongo.datasource.AbstractMongoDataSource;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class DefaultMongoDataSource extends AbstractMongoDataSource {

	private MongoClient		mongo				= null;

	private DB				db					= null;

	private WriteConcern	defaultWriteConcern	= WriteConcern.ACKNOWLEDGED;

	public DefaultMongoDataSource(String creator, MongoClient mongo, DB db, String logicDataSourceId, String realDataSourceId) {
		this.mongo = mongo;
		this.db = db;
		this.creator = creator;
		this.logicDataSourceId = logicDataSourceId;
		this.realDataSourceId = realDataSourceId;
	}

	public void close(String creator) {
		if (null != this.mongo) {
			this.mongo.close();
		}
	}

	public DBCollection getCollection(String collection) {
		return this.db.getCollection(collection);
	}

	public WriteConcern getDefaultWriteConcern() {
		return defaultWriteConcern;
	}

	protected void setDefaultWriteConcern(WriteConcern defaultWriteConcern) {
		this.defaultWriteConcern = defaultWriteConcern;
	}

}
