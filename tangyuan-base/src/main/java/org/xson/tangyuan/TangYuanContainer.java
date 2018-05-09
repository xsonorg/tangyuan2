package org.xson.tangyuan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.app.ExtArg;
import org.xson.tangyuan.bootstrap.StartupAndShutdownVo;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceContextFactory;
import org.xson.tangyuan.monitor.DeadlockMonitorWriter;
import org.xson.tangyuan.monitor.ServiceDeadlockMonitor;
import org.xson.tangyuan.task.AsyncTask;
import org.xson.tangyuan.task.AsyncTaskThread;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.LicensesHelper;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlTangYuanBuilder;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class TangYuanContainer implements TangYuanComponent {

	private static TangYuanContainer				instance						= new TangYuanContainer();

	public final static String						XCO_DATA_KEY					= "$$DATA";
	public final static String						XCO_CODE_KEY					= "$$CODE";
	public final static String						XCO_MESSAGE_KEY					= "$$MESSAGE";
	// XCO返回对象包装标识
	public final static String						XCO_PACKAGE_KEY					= "$$PACKAGE";
	public final static String						XCO_HEADER_KEY					= "$$HEADER";

	public final static int							SUCCESS_CODE					= 0;

	// 默认扩展参数前缀
	//	public final static String						DEFAULT_EXT_ARG_PREFIX			= "EXT:";

	private Log										log								= LogFactory.getLog(getClass());
	private volatile boolean						closing							= false;
	private String									systemName						= "tangyuan";
	private XmlGlobalContext						xmlGlobalContext				= null;

	private final Map<String, AbstractServiceNode>	tangyuanServices				= new HashMap<String, AbstractServiceNode>();
	private final Map<String, AbstractServiceNode>	tangyuanDynamicServices			= new ConcurrentHashMap<String, AbstractServiceNode>();
	private AsyncTaskThread							asyncTaskThread					= null;
	private Map<String, ServiceContextFactory>		scFactoryMap					= new HashMap<String, ServiceContextFactory>();

	// private List<ComponentVo> components = new ArrayList<ComponentVo>();
	// private Map<String, TangYuanComponent> componentMap = new HashMap<String, TangYuanComponent>();

	private Map<String, ComponentVo>				componentMap					= new HashMap<String, ComponentVo>();

	// ss-aop
	private List<StartupAndShutdownVo>				closingBeforeList				= null;
	private List<StartupAndShutdownVo>				closingAfterList				= null;

	private Class<?>								defaultResultType				= XCO.class;
	private boolean									licenses						= false;

	/** true:jdk, false:cglib */
	private boolean									jdkProxy						= false;

	// 错误信息编码
	private int										errorCode						= -1;
	private String									errorMessage					= "服务异常";
	private String									nsSeparator						= "/";

	// 服务死锁监控
	private boolean									openDeadlockMonitor				= false;
	private ServiceDeadlockMonitor					deadlockMonitor					= null;
	private String									deadlockMonitorWriterClassName	= null;
	private long									deadlockMonitorSleepTime		= 2L;
	private long									deadlockIntervalTime			= 10L;

	// 最大关闭等待时间(秒)
	private long									maxWaitTimeForShutDown			= 10L;

	// 所有服务统一返回XCO
	private boolean									allServiceReturnXCO				= false;

	// 外部扩展参数
	private ExtArg									extArg							= new ExtArg();

	private TangYuanContainer() {
	}

	public static TangYuanContainer getInstance() {
		return instance;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		if (properties.containsKey("errorCode".toUpperCase())) {
			errorCode = Integer.parseInt(properties.get("errorCode".toUpperCase()));
		}
		if (properties.containsKey("errorMessage".toUpperCase())) {
			errorMessage = properties.get("errorMessage".toUpperCase());
		}
		if (properties.containsKey("jdkProxy".toUpperCase())) {
			jdkProxy = Boolean.parseBoolean(properties.get("jdkProxy".toUpperCase()));
		}
		// if (properties.containsKey("nsSeparator".toUpperCase())) {
		// nsSeparator = properties.get("nsSeparator".toUpperCase());
		// }
		if (properties.containsKey("systemName".toUpperCase())) {
			systemName = properties.get("systemName".toUpperCase());
		}

		// deadlock
		if (properties.containsKey("openDeadlockMonitor".toUpperCase())) {
			openDeadlockMonitor = Boolean.parseBoolean(properties.get("openDeadlockMonitor".toUpperCase()));
		}
		if (properties.containsKey("deadlockMonitorWriter".toUpperCase())) {
			deadlockMonitorWriterClassName = properties.get("deadlockMonitorWriter".toUpperCase());
		}
		if (properties.containsKey("deadlockMonitorSleepTime".toUpperCase())) {
			deadlockMonitorSleepTime = Long.parseLong(properties.get("deadlockMonitorSleepTime".toUpperCase()));
		}
		if (properties.containsKey("deadlockIntervalTime".toUpperCase())) {
			deadlockIntervalTime = Long.parseLong(properties.get("deadlockIntervalTime".toUpperCase()));
		}

		//
		if (properties.containsKey("maxWaitTimeForShutDown".toUpperCase())) {
			maxWaitTimeForShutDown = Long.parseLong(properties.get("maxWaitTimeForShutDown".toUpperCase()));
		}

		if (properties.containsKey("allServiceReturnXCO".toUpperCase())) {
			allServiceReturnXCO = Boolean.parseBoolean(properties.get("allServiceReturnXCO".toUpperCase()));
			log.info("open the unified return object mode.");
		}

		log.info("config setting success...");
	}

	public void setClosingList(List<StartupAndShutdownVo> closingBeforeList, List<StartupAndShutdownVo> closingAfterList) {
		this.closingBeforeList = closingBeforeList;
		this.closingAfterList = closingAfterList;
	}

	@Override
	public void start(String resource) throws Throwable {
		log.info("tangyuan framework starting, version: " + Version.getVersion());
		try {
			licenses = LicensesHelper.check();
			if (licenses) {
				log.info("tangyuan licenses verification is successful.");
			}
		} catch (Exception e) {
		}

		// 异步队列
		asyncTaskThread = new AsyncTaskThread();
		asyncTaskThread.start();

		// 死锁服务监控
		if (openDeadlockMonitor) {
			DeadlockMonitorWriter writer = null;
			if (null != deadlockMonitorWriterClassName) {
				Class<?> clazz = ClassUtils.forName(deadlockMonitorWriterClassName);
				writer = (DeadlockMonitorWriter) TangYuanUtil.newInstance(clazz);
			}
			deadlockMonitor = new ServiceDeadlockMonitor(writer);
			deadlockMonitor.start();
		}

		xmlGlobalContext = new XmlGlobalContext();

		XmlTangYuanBuilder xmlBuilder = new XmlTangYuanBuilder();
		xmlBuilder.parse(xmlGlobalContext, resource);
		xmlGlobalContext.clean();

		log.info("tangyuan framework successfully.");
	}

	@Override
	public void stop(boolean wait) {
		log.info("tangyuan framework stopping...");

		// for (ComponentVo component : ComponentVo.sort(components, false)) {
		// component.getComponent().stop(true);
		// }

		closing = true;
		wait = true;

		executeSSAop(this.closingBeforeList);

		String type = "web".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(wait);
		}

		// 如果是RPC服务,先停止服务
		type = "rpc".toUpperCase();
		if (componentMap.containsKey("rpc-server")) {
			componentMap.get("rpc-server").getComponent().stop(wait);
		}

		type = "timer".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(wait);
		}

		type = "mq".toUpperCase();
		if (componentMap.containsKey("mq-listener".toUpperCase())) {
			componentMap.get("mq-listener".toUpperCase()).getComponent().stop(wait);
		}

		// 停止服务
		ServiceActuator.shutdown();

		type = "sql".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(wait);
		}

		type = "java".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(wait);
		}

		type = "mongo".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(wait);
		}

		type = "hbase".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(wait);
		}

		type = "hive".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(wait);
		}

		type = "es".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(wait);
		}

		type = "mq".toUpperCase();
		if (componentMap.containsKey("mq-service".toUpperCase())) {
			componentMap.get("mq-service".toUpperCase()).getComponent().stop(wait);
		}

		type = "rpc".toUpperCase();
		if (componentMap.containsKey("rpc-client")) {
			componentMap.get("rpc-client").getComponent().stop(wait);
		}

		type = "cache".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(wait);
		}

		type = "validate".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(wait);
		}

		if (null != asyncTaskThread) {
			asyncTaskThread.stop();
		}
		if (null != deadlockMonitor) {
			deadlockMonitor.stop();
		}

		executeSSAop(this.closingAfterList);

		log.info("tangyuan framework stop successfully.");
	}

	private void executeSSAop(List<StartupAndShutdownVo> ssList) {
		if (null == ssList) {
			return;
		}
		for (StartupAndShutdownVo ssVo : ssList) {
			try {
				ssVo.getHandler().execute(ssVo.getProperties());
				log.info("execute ss-aop class: " + ssVo.getClassName());
			} catch (Throwable e) {
				log.error("execute ss-aop exception.", e);
			}
		}
	}

	public XmlGlobalContext getXmlGlobalContext() {
		return xmlGlobalContext;
	}

	public boolean hasLicenses() {
		return licenses;
	}

	public Class<?> getDefaultResultType() {
		return defaultResultType;
	}

	public void addService(AbstractServiceNode service) {
		tangyuanServices.put(service.getServiceKey(), service);
	}

	public void addDynamicService(AbstractServiceNode service) {
		this.tangyuanDynamicServices.put(service.getServiceKey(), service);
	}

	public void addAsyncTask(AsyncTask task) {
		asyncTaskThread.addTask(task);
	}

	public ServiceContextFactory getContextFactory(TangYuanServiceType type) {
		return scFactoryMap.get(type.name());
	}

	public void registerContextFactory(TangYuanServiceType type, ServiceContextFactory factory) {
		scFactoryMap.put(type.name(), factory);
	}

	public AbstractServiceNode getService(String serviceKey) {
		return tangyuanServices.get(serviceKey);
	}

	public AbstractServiceNode getDynamicService(String serviceKey) {
		return tangyuanDynamicServices.get(serviceKey);
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public boolean isOpenDeadlockMonitor() {
		return openDeadlockMonitor;
	}

	public long getDeadlockMonitorSleepTime() {
		return deadlockMonitorSleepTime * 1000L;
	}

	public long getDeadlockIntervalTime() {
		return deadlockIntervalTime * 1000L;
	}

	public ServiceDeadlockMonitor getDeadlockMonitor() {
		return deadlockMonitor;
	}

	public String getNsSeparator() {
		return nsSeparator;
	}

	public Set<String> getServicesKeySet() {
		return this.tangyuanServices.keySet();
	}

	public TangYuanComponent getComponent(String type) {
		type = type.toUpperCase();
		ComponentVo cVo = componentMap.get(type);
		if (null != cVo) {
			return cVo.getComponent();
		}
		return null;
	}

	public void registerComponent(ComponentVo componentVo) {
		String type = componentVo.getType().toUpperCase();
		if (componentMap.containsKey(type)) {
			throw new TangYuanException("Duplicate component registration: " + componentVo.getType());
		}
		componentMap.put(type, componentVo);
	}

	public String getSystemName() {
		return systemName;
	}

	public boolean isJdkProxy() {
		return jdkProxy;
	}

	public long getMaxWaitTimeForShutDown() {
		return maxWaitTimeForShutDown * 1000L;
	}

	public boolean isClosing() {
		return closing;
	}

	public boolean isAllServiceReturnXCO() {
		return allServiceReturnXCO;
	}

	public ExtArg getExtArg() {
		return extArg;
	}

}
