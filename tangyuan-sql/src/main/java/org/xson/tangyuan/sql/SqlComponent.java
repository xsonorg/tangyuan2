package org.xson.tangyuan.sql;

import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.Version;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.TangYuanState.ComponentState;
import org.xson.tangyuan.sharding.ShardingDefManager;
import org.xson.tangyuan.sql.datasource.DataSourceManager;
import org.xson.tangyuan.sql.service.SqlActuator;
import org.xson.tangyuan.sql.service.context.SqlServiceContextFactory;
import org.xson.tangyuan.sql.transaction.DefaultTransactionMatcher;
import org.xson.tangyuan.sql.transaction.MultipleTransactionManager;
import org.xson.tangyuan.sql.transaction.XTransactionManager;
import org.xson.tangyuan.sql.util.SqlLog;
import org.xson.tangyuan.sql.xml.XmlSqlComponentBuilder;
import org.xson.tangyuan.sql.xml.XmlSqlContext;
import org.xson.tangyuan.type.TypeHandlerRegistry;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class SqlComponent implements TangYuanComponent {

	private static SqlComponent       instance            = new SqlComponent();

	private Log                       log                 = LogFactory.getLog(getClass());
	private DataSourceManager         dataSourceManager   = null;
	private ShardingDefManager        shardingDefManager  = new ShardingDefManager();
	private TypeHandlerRegistry       typeHandlerRegistry = null;
	private DefaultTransactionMatcher transactionMatcher  = null;
	private int                       defaultFetchSize    = 100;
	// 需要静态化的对象
	private XTransactionManager       transactionManager  = null;
	private SqlActuator               sqlActuator         = null;
	private SqlLog                    sqlLog              = null;

	private volatile ComponentState   state               = ComponentState.UNINITIALIZED;

	static {
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.SQL, new SqlServiceContextFactory());
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "sql"));
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

	public SqlActuator getSqlActuator() {
		return sqlActuator;
	}

	public XTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public DefaultTransactionMatcher getTransactionMatcher() {
		return transactionMatcher;
	}

	public SqlLog getSqlLog() {
		return sqlLog;
	}

	public boolean isRunning() {
		return ComponentState.RUNNING == this.state;
	}

	@Override
	public void config(Map<String, String> properties) {
		//		if (properties.containsKey("printErrorSqlLog".toUpperCase())) {
		//			this.printErrorSqlLog = Boolean.parseBoolean(properties.get("printErrorSqlLog".toUpperCase()));
		//		}
		//		log.info(TangYuanLang.get("config.property.load"), "sql-component");
	}

	private void post(XmlSqlContext componentContext) {
		//		this.sqlActuator = new SqlActuator(getTypeHandlerRegistry(), printErrorSqlLog);
		this.sqlActuator = new SqlActuator(getTypeHandlerRegistry());
		this.sqlLog = new SqlLog(getTypeHandlerRegistry());
		this.transactionManager = new MultipleTransactionManager(getDataSourceManager());
		this.transactionMatcher = componentContext.getTransactionMatcher();
	}

	public void start(String resource) throws Throwable {

		//		log.info("sql component starting, version: " + Version.getVersion());
		log.info(TangYuanLang.get("component.dividing.line"));
		log.info(TangYuanLang.get("component.starting"), "sql", Version.getVersion());

		this.state = ComponentState.INITIALIZING;

		TangYuanLang.getInstance().load("tangyuan-lang-sql");
		
		XmlSqlContext componentContext = new XmlSqlContext();
		componentContext.setXmlContext(TangYuanContainer.getInstance().getXmlGlobalContext());

		XmlSqlComponentBuilder builder = new XmlSqlComponentBuilder();
		builder.parse(componentContext, resource);
		post(componentContext);
		componentContext.clean();

		this.state = ComponentState.RUNNING;

		log.info(TangYuanLang.get("component.starting.successfully"), "sql");
	}

	@Override
	public void stop(long waitingTime, boolean asyn) {
		log.info(TangYuanLang.get("component.stopping"), "sql");

		this.state = ComponentState.CLOSING;

		try {
			if (null != dataSourceManager) {
				dataSourceManager.close();
			}
		} catch (Throwable e) {
			log.error(e);
		}

		this.state = ComponentState.CLOSED;

		log.info(TangYuanLang.get("component.stopping.successfully"), "sql");
	}

	////////////////////////////////

	/** 打印错误SQL日志 */
	//	private boolean                   printErrorSqlLog    = false;

}
