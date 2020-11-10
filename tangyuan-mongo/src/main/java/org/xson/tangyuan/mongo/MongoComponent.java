package org.xson.tangyuan.mongo;

import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.Version;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.TangYuanState.ComponentState;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceManager;
import org.xson.tangyuan.mongo.util.MongoStaticMethod;
import org.xson.tangyuan.mongo.xml.XmlMongoComponentBuilder;
import org.xson.tangyuan.mongo.xml.XmlMongoContext;
import org.xson.tangyuan.service.context.MongoServiceContextFactory;
import org.xson.tangyuan.sharding.ShardingDefManager;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

import com.mongodb.WriteConcern;
import com.mongodb.util.JSONExtCallback;

public class MongoComponent implements TangYuanComponent {

	private static MongoComponent	instance				= new MongoComponent();

	private Log						log						= LogFactory.getLog(getClass());
	private MongoDataSourceManager	dataSourceManager		= null;
	private ShardingDefManager		shardingDefManager		= new ShardingDefManager();

	private volatile ComponentState	state					= ComponentState.UNINITIALIZED;

	private WriteConcern			defaultWriteConcern		= WriteConcern.ACKNOWLEDGED;
	private String					defaultMongoDatePattern	= JSONExtCallback._dateTimeFormat;

	static {
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.MONGO, new MongoServiceContextFactory());
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "mongo"));
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

	public String getDefaultMongoDatePattern() {
		return defaultMongoDatePattern;
	}

	public WriteConcern getDefaultWriteConcern() {
		return defaultWriteConcern;
	}

	public boolean isRunning() {
		return ComponentState.RUNNING == this.state;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {

		if (properties.containsKey("defaultMongoDatePattern".toUpperCase())) {
			this.defaultMongoDatePattern = properties.get("defaultMongoDatePattern".toUpperCase()).trim();
		}

		log.info(TangYuanLang.get("config.property.load"), "mongo-component");
	}

	@Override
	public void start(String resource) throws Throwable {
		log.info(TangYuanLang.get("component.dividing.line"));
		log.info(TangYuanLang.get("component.starting"), "mongo", Version.getVersion());

		this.state = ComponentState.INITIALIZING;

		TangYuanLang.getInstance().load("tangyuan-lang-sql");

		XmlMongoContext componentContext = new XmlMongoContext();
		componentContext.setXmlContext(TangYuanContainer.getInstance().getXmlGlobalContext());

		MongoStaticMethod.register();

		XmlMongoComponentBuilder builder = new XmlMongoComponentBuilder();
		builder.parse(componentContext, resource);
		componentContext.clean();

		this.state = ComponentState.RUNNING;

		log.info(TangYuanLang.get("component.starting.successfully"), "mongo");
	}

	@Override
	public void stop(long waitingTime, boolean asyn) {
		log.info(TangYuanLang.get("component.stopping"), "mongo");

		this.state = ComponentState.CLOSING;

		try {
			if (null != dataSourceManager) {
				dataSourceManager.close();
			}
		} catch (Throwable e) {
			log.error(e);
		}

		this.state = ComponentState.CLOSED;

		log.info(TangYuanLang.get("component.stopping.successfully"), "mongo");
	}

}
