package org.xson.tangyuan.sql;

import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.sharding.ShardingDefManager;
import org.xson.tangyuan.sql.datasource.DataSourceManager;
import org.xson.tangyuan.sql.executor.SqlServiceContextFactory;
import org.xson.tangyuan.sql.xml.XmlConfigBuilder;
import org.xson.tangyuan.type.TypeHandlerRegistry;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class SqlComponent implements TangYuanComponent {

	private static SqlComponent	instance			= new SqlComponent();

	private Log					log					= LogFactory.getLog(getClass());
	private DataSourceManager	dataSourceManager	= null;
	private ShardingDefManager	shardingDefManager	= new ShardingDefManager();
	private TypeHandlerRegistry	typeHandlerRegistry	= null;
	private int					defaultFetchSize	= 100;
	/** 打印错误SQL日志 */
	private boolean				printErrorSqlLog	= false;

	static {
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.SQL, new SqlServiceContextFactory());
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "sql"));
		// TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "sql", 40, 40));
	}

	private SqlComponent() {
	}

	public static SqlComponent getInstance() {
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

	public boolean getPrintErrorSqlLog() {
		return printErrorSqlLog;
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
		if (properties.containsKey("printErrorSqlLog".toUpperCase())) {
			this.printErrorSqlLog = Boolean.parseBoolean(properties.get("printErrorSqlLog".toUpperCase()));
		}
		log.info("config setting success...");
	}

	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("sql component starting, version: " + Version.getVersion());
		// log.info("Start parsing: " + resource);
		XmlConfigBuilder xmlConfigBuilder = new XmlConfigBuilder();
		xmlConfigBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);
		log.info("sql component successfully.");
	}

	@Override
	public void stop(boolean wait) {
		log.info("sql component stopping...");
		try {
			if (null != dataSourceManager) {
				dataSourceManager.close();
			}
		} catch (Throwable e) {
			log.error(null, e);
		}
		log.info("sql component stop successfully.");
	}

}
