package org.xson.tangyuan.hive;

import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.hive.executor.HiveServiceContextFactory;
import org.xson.tangyuan.hive.xml.XmlConfigBuilder;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.sharding.ShardingDefManager;
import org.xson.tangyuan.sql.datasource.DataSourceManager;
import org.xson.tangyuan.type.TypeHandlerRegistry;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class HiveComponent implements TangYuanComponent {

	private static HiveComponent	instance			= new HiveComponent();

	private Log						log					= LogFactory.getLog(getClass());
	private DataSourceManager		dataSourceManager	= null;
	private ShardingDefManager		shardingDefManager	= new ShardingDefManager();
	private TypeHandlerRegistry		typeHandlerRegistry	= null;
	private int						defaultFetchSize	= 100;

	static {
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.HIVE, new HiveServiceContextFactory());
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "hive"));
	}

	private HiveComponent() {
	}

	public static HiveComponent getInstance() {
		return instance;
	}

	public DataSourceManager getDataSourceManager() {
		return dataSourceManager;
	}

	public void setDataSourceManager(DataSourceManager dataSourceManager) {
		this.dataSourceManager = dataSourceManager;
	}

	public int getDefaultFetchSize() {
		return defaultFetchSize;
	}

	public TypeHandlerRegistry getTypeHandlerRegistry() {
		return typeHandlerRegistry;
	}

	public void setTypeHandlerRegistry(TypeHandlerRegistry typeHandlerRegistry) {
		this.typeHandlerRegistry = typeHandlerRegistry;
	}

	public ShardingDefManager getShardingDefManager() {
		return shardingDefManager;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		log.info("config setting success...");
	}

	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("hive component starting, version: " + Version.getVersion());
		XmlConfigBuilder xmlConfigBuilder = new XmlConfigBuilder();
		xmlConfigBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);
		log.info("hive component successfully.");
	}

	@Override
	public void stop(boolean wait) {
		log.info("hive component stopping...");
		try {
			if (null != dataSourceManager) {
				dataSourceManager.close();
			}
		} catch (Throwable e) {
			log.error(null, e);
		}
		log.info("hive component stop successfully.");
	}

}
