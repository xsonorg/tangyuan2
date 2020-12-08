package org.xson.tangyuan.hive;

import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.Version;
import org.xson.tangyuan.hive.datasource.DataSourceManager;
import org.xson.tangyuan.hive.service.SqlActuator;
import org.xson.tangyuan.hive.service.context.HiveServiceContextFactory;
import org.xson.tangyuan.hive.transaction.MultipleTransactionManager;
import org.xson.tangyuan.hive.transaction.XTransactionManager;
import org.xson.tangyuan.hive.util.SqlLog;
import org.xson.tangyuan.hive.xml.XmlHiveComponentBuilder;
import org.xson.tangyuan.hive.xml.XmlHiveContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.TangYuanState.ComponentState;
import org.xson.tangyuan.sharding.ShardingDefManager;
import org.xson.tangyuan.type.TypeHandlerRegistry;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class HiveComponent implements TangYuanComponent {

	private static HiveComponent    instance            = new HiveComponent();

	private Log                     log                 = LogFactory.getLog(getClass());
	private DataSourceManager       dataSourceManager   = null;
	private ShardingDefManager      shardingDefManager  = new ShardingDefManager();
	private TypeHandlerRegistry     typeHandlerRegistry = null;
	private int                     defaultFetchSize    = 100;

	// 需要静态化的对象
	private XTransactionManager     transactionManager  = null;
	private SqlActuator             sqlActuator         = null;
	private SqlLog                  sqlLog              = null;

	private volatile ComponentState state               = ComponentState.UNINITIALIZED;

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

	public boolean isRunning() {
		return ComponentState.RUNNING == this.state;
	}

	public SqlLog getSqlLog() {
		return sqlLog;
	}

	public XTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public SqlActuator getSqlActuator() {
		return sqlActuator;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		// log.info(TangYuanLang.get("config.property.load"), "hive-component");
	}

	private void post(XmlHiveContext componentContext) {
		this.sqlActuator = new SqlActuator(getTypeHandlerRegistry());
		this.sqlLog = new SqlLog(getTypeHandlerRegistry());
		this.transactionManager = new MultipleTransactionManager(getDataSourceManager());
	}

	public void start(String resource) throws Throwable {

		log.info(TangYuanLang.get("component.dividing.line"));
		log.info(TangYuanLang.get("component.starting"), "hive", Version.getVersion());

		this.state = ComponentState.INITIALIZING;

		TangYuanLang.getInstance().load("tangyuan-lang-hive");

		XmlHiveContext componentContext = new XmlHiveContext();
		componentContext.setXmlContext(TangYuanContainer.getInstance().getXmlGlobalContext());

		XmlHiveComponentBuilder builder = new XmlHiveComponentBuilder();
		builder.parse(componentContext, resource);
		post(componentContext);
		componentContext.clean();

		this.state = ComponentState.RUNNING;

		log.info(TangYuanLang.get("component.starting.successfully"), "hive");
	}

	@Override
	public void stop(long waitingTime, boolean asyn) {
		log.info(TangYuanLang.get("component.stopping"), "hive");

		this.state = ComponentState.CLOSING;

		try {
			if (null != dataSourceManager) {
				dataSourceManager.close();
			}
		} catch (Throwable e) {
			log.error(e);
		}

		this.state = ComponentState.CLOSED;

		log.info(TangYuanLang.get("component.stopping.successfully"), "hive");
	}

}
