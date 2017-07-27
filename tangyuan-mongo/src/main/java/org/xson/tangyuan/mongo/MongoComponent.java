package org.xson.tangyuan.mongo;

import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceManager;
import org.xson.tangyuan.mongo.executor.MongoServiceContextFactory;
import org.xson.tangyuan.mongo.xml.XmlMongoConfigBuilder;
import org.xson.tangyuan.sharding.ShardingDefManager;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class MongoComponent implements TangYuanComponent {

	private static MongoComponent	instance			= new MongoComponent();

	private Log						log					= LogFactory.getLog(getClass());
	private MongoDataSourceManager	dataSourceManager	= null;
	private ShardingDefManager		shardingDefManager	= new ShardingDefManager();
	private int						defaultFetchSize	= 100;

	// 以后考虑放在每个DS中
	// private WriteConcern defaultWriteConcern = WriteConcern.ACKNOWLEDGED;

	static {
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.MONGO, new MongoServiceContextFactory());
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "mongo", 40, 40));
	}

	private MongoComponent() {
	}

	public static MongoComponent getInstance() {
		return instance;
	}

	public MongoDataSourceManager getDataSourceManager() {
		return dataSourceManager;
	}

	public void setDataSourceManager(MongoDataSourceManager dataSourceManager) {
		this.dataSourceManager = dataSourceManager;
	}

	public ShardingDefManager getShardingDefManager() {
		return shardingDefManager;
	}

	public int getDefaultFetchSize() {
		return defaultFetchSize;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		// if (properties.containsKey("errorCode".toUpperCase())) {
		// errorCode = Integer.parseInt(properties.get("errorCode".toUpperCase()));
		// }
		// if (properties.containsKey("errorMessage".toUpperCase())) {
		// errorMessage = properties.get("errorMessage".toUpperCase());
		// }
		// if (properties.containsKey("nsSeparator".toUpperCase())) {
		// nsSeparator = properties.get("nsSeparator".toUpperCase());
		// }
		log.info("config setting success...");
	}

	@Override
	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("mongo component starting, version: " + Version.getVersion());
		XmlMongoConfigBuilder xmlConfigBuilder = new XmlMongoConfigBuilder();
		xmlConfigBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);
		log.info("mongo component successfully.");
	}

	@Override
	public void stop(boolean wait) {
		log.info("mongo component stopping...");
		if (null != dataSourceManager) {
			dataSourceManager.close();
		}
		log.info("mongo component stop successfully.");
	}

}
