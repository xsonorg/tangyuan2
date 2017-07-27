package org.xson.tangyuan.mongo.datasource.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.mongo.datasource.AbstractMongoDataSource;
import org.xson.tangyuan.mongo.datasource.DataSourceCreater;
import org.xson.tangyuan.mongo.datasource.DataSourceException;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceGroupVo;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceVo;
import org.xson.tangyuan.mongo.datasource.util.DSPropertyUtil;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

public class DefaultDataSourceCreater implements DataSourceCreater {

	private Log				log					= LogFactory.getLog(DefaultDataSourceCreater.class);

	private WriteConcern	defaultWriteConcern	= null;

	@Override
	public void newInstance(MongoDataSourceVo dsVo, Map<String, MongoDataSourceVo> logicMap, Map<String, AbstractMongoDataSource> realMap) {
		if (dsVo.isGroup()) {
			MongoDataSourceGroupVo dsGroupVo = (MongoDataSourceGroupVo) dsVo;
			for (int i = dsGroupVo.getStart(); i <= dsGroupVo.getEnd(); i++) {
				String realId = dsGroupVo.getId() + "." + i;
				if (realMap.containsKey(realId)) {
					throw new DataSourceException("Duplicate DataSourceID: " + realId);
				}
				DefaultMongoDataSource ds = createDataSource(dsVo, i + "");
				realMap.put(realId, ds);
				log.info("add mongo datasource[group]: " + realId);
			}

		} else {
			DefaultMongoDataSource ds = createDataSource(dsVo, null);
			realMap.put(dsVo.getId(), ds);
			log.info("add mongo datasource: " + dsVo.getId());
		}
		logicMap.put(dsVo.getId(), dsVo);
	}

	private MongoClientOptions createOptions(Map<String, String> properties) {

		// builder.autoConnectRetry(false) // 是否重试机制

		MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
		// 是否保持长链接
		builder.socketKeepAlive(DSPropertyUtil.getBooleanValue(properties, "socketKeepAlive".toUpperCase(), true));
		// 长链接的最大等待时间
		builder.maxWaitTime(DSPropertyUtil.getIntValue(properties, "maxWaitTime".toUpperCase(), 1000 * 60 * 10));
		// 链接超时时间
		builder.connectTimeout(DSPropertyUtil.getIntValue(properties, "connectTimeout".toUpperCase(), 60 * 1000));
		// read数据超时时间
		builder.socketTimeout(DSPropertyUtil.getIntValue(properties, "socketTimeout".toUpperCase(), 60 * 1000));
		// 最近优先策略
		builder.readPreference(ReadPreference.primary());
		// 每个地址最大请求数
		builder.connectionsPerHost(DSPropertyUtil.getIntValue(properties, "connectionsPerHost".toUpperCase(), 30));
		// 一个socket最大的等待请求数
		builder.threadsAllowedToBlockForConnectionMultiplier(
				DSPropertyUtil.getIntValue(properties, "threadsAllowedToBlockForConnectionMultiplier".toUpperCase(), 50));

		if (properties.containsKey("minConnectionsPerHost".toUpperCase())) {
			builder.minConnectionsPerHost(Integer.parseInt(properties.get("minConnectionsPerHost".toUpperCase())));
		}

		if (properties.containsKey("maxConnectionIdleTime".toUpperCase())) {
			builder.maxConnectionIdleTime(Integer.parseInt(properties.get("maxConnectionIdleTime".toUpperCase())));
		}

		if (properties.containsKey("maxConnectionLifeTime".toUpperCase())) {
			builder.maxConnectionLifeTime(Integer.parseInt(properties.get("maxConnectionLifeTime".toUpperCase())));
		}

		if (properties.containsKey("minHeartbeatFrequency".toUpperCase())) {
			builder.minHeartbeatFrequency(Integer.parseInt(properties.get("minHeartbeatFrequency".toUpperCase())));
		}

		if (properties.containsKey("serverSelectionTimeout".toUpperCase())) {
			builder.serverSelectionTimeout(Integer.parseInt(properties.get("serverSelectionTimeout".toUpperCase())));
		}

		if (properties.containsKey("sslInvalidHostNameAllowed".toUpperCase())) {
			builder.sslInvalidHostNameAllowed(Boolean.parseBoolean(properties.get("sslInvalidHostNameAllowed".toUpperCase())));
		}

		if (properties.containsKey("sslEnabled".toUpperCase())) {
			builder.sslEnabled(Boolean.parseBoolean(properties.get("sslEnabled".toUpperCase())));
		}

		if (properties.containsKey("requiredReplicaSetName".toUpperCase())) {
			builder.requiredReplicaSetName(properties.get("requiredReplicaSetName".toUpperCase()));
		}

		// 这里统一设置，其他地方需要保持一致
		if (properties.containsKey("writeConcern".toUpperCase())) {
			String writeConcern = properties.get("writeConcern".toUpperCase());
			WriteConcern defaultWriteConcern = getWriteConcern(writeConcern);
			builder.writeConcern(getWriteConcern(writeConcern));
			this.defaultWriteConcern = defaultWriteConcern;
		}

		if (properties.containsKey("readConcern".toUpperCase())) {
			String readConcern = properties.get("readConcern".toUpperCase());
			builder.readConcern(getReadConcern(readConcern));
		}

		// alwaysUseMBeans = options.isAlwaysUseMBeans();
		// heartbeatFrequency = options.getHeartbeatFrequency();
		// minHeartbeatFrequency = options.getMinHeartbeatFrequency();
		// heartbeatConnectTimeout = options.getHeartbeatConnectTimeout();
		// heartbeatSocketTimeout = options.getHeartbeatSocketTimeout();
		// localThreshold = options.getLocalThreshold();
		// requiredReplicaSetName = options.getRequiredReplicaSetName();
		// dbDecoderFactory = options.getDbDecoderFactory();
		// dbEncoderFactory = options.getDbEncoderFactory();
		// socketFactory = options.getSocketFactory();
		// cursorFinalizerEnabled = options.isCursorFinalizerEnabled();

		return builder.build();
	}

	private ReadConcern getReadConcern(String readConcern) {
		if ("LOCAL".equalsIgnoreCase(readConcern)) {
			return ReadConcern.LOCAL;
		}
		if ("MAJORITY".equalsIgnoreCase(readConcern)) {
			return ReadConcern.MAJORITY;
		}
		return ReadConcern.DEFAULT;
	}

	@SuppressWarnings("deprecation")
	private WriteConcern getWriteConcern(String writeConcern) {
		if ("ACKNOWLEDGED".equalsIgnoreCase(writeConcern)) {
			return WriteConcern.ACKNOWLEDGED;
		}
		if ("W1".equalsIgnoreCase(writeConcern)) {
			return WriteConcern.W1;
		}
		if ("W2".equalsIgnoreCase(writeConcern)) {
			return WriteConcern.W2;
		}
		if ("W3".equalsIgnoreCase(writeConcern)) {
			return WriteConcern.W3;
		}
		if ("UNACKNOWLEDGED".equalsIgnoreCase(writeConcern)) {
			return WriteConcern.UNACKNOWLEDGED;
		}
		if ("FSYNCED".equalsIgnoreCase(writeConcern)) {
			return WriteConcern.FSYNCED;
		}
		if ("REPLICA_ACKNOWLEDGED".equalsIgnoreCase(writeConcern)) {
			return WriteConcern.REPLICA_ACKNOWLEDGED;
		}
		if ("NORMAL".equalsIgnoreCase(writeConcern)) {
			return WriteConcern.NORMAL;
		}
		if ("SAFE".equalsIgnoreCase(writeConcern)) {
			return WriteConcern.SAFE;
		}
		if ("MAJORITY".equalsIgnoreCase(writeConcern)) {
			return WriteConcern.MAJORITY;
		}
		if ("FSYNC_SAFE".equalsIgnoreCase(writeConcern)) {
			return WriteConcern.FSYNC_SAFE;
		}
		if ("JOURNAL_SAFE".equalsIgnoreCase(writeConcern)) {
			return WriteConcern.JOURNAL_SAFE;
		}
		if ("REPLICAS_SAFE".equalsIgnoreCase(writeConcern)) {
			return WriteConcern.REPLICAS_SAFE;
		}
		return WriteConcern.ACKNOWLEDGED;
	}

	private List<MongoCredential> getCredentials(String username, String password, String dbName) {
		List<MongoCredential> list = new ArrayList<MongoCredential>();
		MongoCredential credential = MongoCredential.createCredential(username, dbName, password.toCharArray());
		list.add(credential);
		return list;
	}

	private ServerAddress parseAddr(String url) {
		int start = "mongodb://".length();
		int end = url.lastIndexOf("/");
		String host = url.substring(start, end).trim();
		String[] array = host.split(":");
		return new ServerAddress(array[0], Integer.parseInt(array[1]));
	}

	private void checkMongoUrl(String url) {
		if (null == url) {
			throw new DataSourceException("mongo url is null");
		}
		Pattern pattern = Pattern.compile("^mongodb://[a-zA-Z0-9\\.]+?:[0-9]{2,5}/.+?$");
		Matcher m = pattern.matcher(url);
		if (!m.matches()) {
			throw new DataSourceException(
					"Illegal mongo url: " + url + ", The correct format should be similar: [mongodb://127.0.0.1:27027/tangyuan_db]");
		}
	}

	@SuppressWarnings("deprecation")
	public DefaultMongoDataSource createDataSource(MongoDataSourceVo dsVo, String urlPattern) {
		this.defaultWriteConcern = null;
		Map<String, String> properties = dsVo.getProperties();
		MongoClient mongo = null;
		DB db = null;

		// mongodb://127.0.0.1:27027/tangyuan_db
		String url = properties.get("url".toUpperCase());
		// 检测URL
		checkMongoUrl(url);
		if (null != urlPattern) {
			url = DSPropertyUtil.replace(url, "{}", urlPattern);
		}
		ServerAddress addr = parseAddr(url);

		// String host = null;// int port = 0;
		String dbName = url.substring(url.lastIndexOf("/") + 1).trim();
		String username = properties.get("username".toUpperCase());
		String password = properties.get("password".toUpperCase());

		MongoClientOptions options = createOptions(properties);
		List<MongoCredential> credentialsList = null;
		if (null != username && null != password) {
			credentialsList = getCredentials(username, password, dbName);
		}

		if (null == options && null == credentialsList) {
			mongo = new MongoClient(addr);
		} else if (null != options && null == credentialsList) {
			mongo = new MongoClient(addr, options);
		} else if (null == options && null != credentialsList) {
			mongo = new MongoClient(addr, credentialsList);
		} else {
			mongo = new MongoClient(addr, credentialsList, options);
		}

		db = mongo.getDB(dbName);

		String realId = dsVo.getId();
		if (null != urlPattern) {
			realId = realId + "." + urlPattern;
		}
		DefaultMongoDataSource dataSource = new DefaultMongoDataSource(dsVo.getCreator(), mongo, db, dsVo.getId(), realId);
		if (null != this.defaultWriteConcern) {
			dataSource.setDefaultWriteConcern(this.defaultWriteConcern);
		}

		return dataSource;
	}
}
