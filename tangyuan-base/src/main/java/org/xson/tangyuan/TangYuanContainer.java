package org.xson.tangyuan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xson.common.object.XCO;
import org.xson.tangyuan.aop.sys.SystemAopVo;
import org.xson.tangyuan.client.http.HttpClientManager;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.ManagerLauncher;
import org.xson.tangyuan.manager.TangYuanManager;
import org.xson.tangyuan.manager.TangYuanState.ComponentState;
import org.xson.tangyuan.service.context.ServiceContextFactory;
import org.xson.tangyuan.service.pool.AsyncTask;
import org.xson.tangyuan.service.pool.ThreadPool;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlTangYuanBuilder;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class TangYuanContainer implements TangYuanComponent {

	private static TangYuanContainer				instance				= new TangYuanContainer();

	public final static String						XCO_DATA_KEY			= "$$DATA";
	public final static String						XCO_CODE_KEY			= "$$CODE";
	public final static String						XCO_MESSAGE_KEY			= "$$MESSAGE";
	/** XCO返回对象包装标识 */
	public final static String						XCO_PACKAGE_KEY			= "$$PACKAGE";
	public final static String						XCO_HEADER_KEY			= "$$HEADER";
	public final static int							SUCCESS_CODE			= 0;

	private Log										log						= null;
	private XmlGlobalContext						xmlGlobalContext		= null;
	private volatile ComponentState					state					= ComponentState.UNINITIALIZED;

	private final Map<String, AbstractServiceNode>	tangyuanServices		= new HashMap<String, AbstractServiceNode>();
	private final Map<String, AbstractServiceNode>	tangyuanDynamicServices	= new ConcurrentHashMap<String, AbstractServiceNode>();

	private ThreadPool								threadPool				= null;

	private Map<String, ServiceContextFactory>		scFactoryMap			= new HashMap<String, ServiceContextFactory>();
	private Map<String, ComponentVo>				componentMap			= new HashMap<String, ComponentVo>();

	/** system AOP */
	private List<SystemAopVo>						closingBeforeList		= null;
	private List<SystemAopVo>						closingAfterList		= null;
	private Class<?>								defaultResultType		= XCO.class;
	/** true:jdk, false:cglib */
	private boolean									jdkProxy				= false;
	/** 错误信息编码 */
	private int										errorCode				= -1;
	private String									errorMessage			= "服务异常";
	private String									nsSeparator				= "/";

	/** 最大关闭等待时间(秒) */
	private long									maxWaitTimeForShutDown	= 10L;
	/** 所有服务统一返回XCO */
	private boolean									allServiceReturnXCO		= true;
	/** 关闭的时候是否启动一个新的线程 */
	private boolean									shutdownInNewThread		= false;
	/** 克隆服务参数 */
	private boolean									cloneServiceArg			= false;
	/** 后缀参数, 通过配置文件后缀传入 */
	private Map<String, String>						suffixArgs				= new HashMap<>();

	private TangYuanContainer() {
	}

	public static TangYuanContainer getInstance() {
		return instance;
	}

	public void setClosingList(List<SystemAopVo> closingBeforeList, List<SystemAopVo> closingAfterList) {
		this.closingBeforeList = closingBeforeList;
		this.closingAfterList = closingAfterList;
	}

	public XmlGlobalContext getXmlGlobalContext() {
		return xmlGlobalContext;
	}

	public Class<?> getDefaultResultType() {
		return defaultResultType;
	}

	public void addService(AbstractServiceNode service) {
		tangyuanServices.put(service.getServiceKey(), service);
	}

	public boolean checkServiceKey(String serviceKey) {
		return tangyuanServices.containsKey(serviceKey);
	}

	public void addDynamicService(AbstractServiceNode service) {
		this.tangyuanDynamicServices.put(service.getServiceKey(), service);
	}

	public void addAsyncTask(AsyncTask task) {
		this.threadPool.execute(task);
	}

	public ServiceContextFactory getContextFactory(TangYuanServiceType type) {
		return this.scFactoryMap.get(type.name());
	}

	public void registerContextFactory(TangYuanServiceType type, ServiceContextFactory factory) {
		this.scFactoryMap.put(type.name(), factory);
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

	public String getNsSeparator() {
		return nsSeparator;
	}

	public boolean isCloneServiceArg() {
		return cloneServiceArg;
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

	public boolean isJdkProxy() {
		return jdkProxy;
	}

	public boolean isRunning() {
		return ComponentState.RUNNING == this.state;
	}

	public boolean isAllServiceReturnXCO() {
		return allServiceReturnXCO;
	}

	public ThreadPool getThreadPool() {
		return threadPool;
	}

	public void startThreadPool(Properties p) throws Throwable {
		if (null == this.threadPool) {
			threadPool = new ThreadPool();
			threadPool.start(p, this.maxWaitTimeForShutDown);
		}
	}

	private void executeSSAop(List<SystemAopVo> ssList) {
		if (null == ssList) {
			return;
		}
		for (SystemAopVo ssVo : ssList) {
			try {
				ssVo.getHandler().execute(ssVo.getProperties());
				log.info("execute system-aop class: " + ssVo.getClassName());
			} catch (Throwable e) {
				log.error("execute system-aop exception.", e);
			}
		}
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		if (properties.containsKey("errorCode".toUpperCase())) {
			this.errorCode = Integer.parseInt(properties.get("errorCode".toUpperCase()));
		}
		if (properties.containsKey("errorMessage".toUpperCase())) {
			this.errorMessage = properties.get("errorMessage".toUpperCase());
		}
		if (properties.containsKey("jdkProxy".toUpperCase())) {
			this.jdkProxy = Boolean.parseBoolean(properties.get("jdkProxy".toUpperCase()));
		}
		if (properties.containsKey("maxWaitTimeForShutDown".toUpperCase())) {
			this.maxWaitTimeForShutDown = Long.parseLong(properties.get("maxWaitTimeForShutDown".toUpperCase()));
		}
		if (properties.containsKey("shutdownInNewThread".toUpperCase())) {
			this.shutdownInNewThread = Boolean.parseBoolean(properties.get("shutdownInNewThread".toUpperCase()));
		}

		if (properties.containsKey("cloneServiceArg".toUpperCase())) {
			this.cloneServiceArg = Boolean.parseBoolean(properties.get("cloneServiceArg".toUpperCase()));
		}

		log.info(TangYuanLang.get("config.property.load"), "tangyuan-component");
	}

	/** 解析日志语言 */
	private String parseLang(String resource) throws Throwable {
		// 1. 解析后缀参数, E.g: tangyuan.xml?lang=cn
		resource = TangYuanUtil.parseSuffixGetResource(resource, suffixArgs);
		// 2. 加载日志语言
		TangYuanLang.getInstance().init(suffixArgs.get("lang"));
		TangYuanLang.getInstance().load("tangyuan-lang-base");

		return resource;
	}

	/**
	 * 是否忽略异常，由内部控制
	 */
	private void startManagerLauncher() throws Throwable {
		new ManagerLauncher().start(null);
	}

	private void initLog() {
		this.log = LogFactory.getLog(getClass());
	}

	@Override
	public void start(String resource) throws Throwable {
		// 1. 初始化ManagerLauncher
		startManagerLauncher();
		// 2. Pre-work
		resource = parseLang(resource);
		// 3. 初始化Log
		initLog();

		log.info(TangYuanLang.get("tangyuan.starting"), Version.getVersion());

		this.state = ComponentState.INITIALIZING;

		// 4. Parsing
		this.xmlGlobalContext = new XmlGlobalContext();
		XmlTangYuanBuilder xmlBuilder = new XmlTangYuanBuilder();
		xmlBuilder.parse(this.xmlGlobalContext, resource);
		this.xmlGlobalContext.clean();

		this.state = ComponentState.RUNNING;
		reportInitialized();

		log.info(TangYuanLang.get("tangyuan.starting.successfully"));
		log.info("#####################################################");
	}

	public void stop(boolean asyn) {
		stop(maxWaitTimeForShutDown, asyn);
	}

	public void reportInitialized() {
		TangYuanManager tm = TangYuanManager.getInstance();
		if (null != tm) {
			tm.reportInitialized();
		}
	}

	public void reportClosing() {
		TangYuanManager tm = TangYuanManager.getInstance();
		if (null != tm) {
			tm.reportClosing();
		}
	}

	public void reportClosed() {
		TangYuanManager tm = TangYuanManager.getInstance();
		if (null != tm) {
			tm.reportClosed();
		}
	}

	@Override
	public void stop(long waitingTime, boolean asyn) {
		log.info("#####################################################");
		log.info(TangYuanLang.get("tangyuan.stopping"));

		reportClosing();

		this.state = ComponentState.CLOSING;

		boolean _wait = true;
		if (this.shutdownInNewThread) {
			Thread shutdownThread = new Thread(new Runnable() {
				@Override
				public void run() {
					stop0(waitingTime, _wait);
				}
			});
			shutdownThread.setDaemon(false);
			shutdownThread.start();
		} else {
			stop0(waitingTime, _wait);
		}
	}

	private void stop0(long waitingTime, boolean wait) {

		// 0. 关闭时的AOP
		executeSSAop(this.closingBeforeList);

		String type = null;

		// 1. 关闭监控
		type = "manager".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(waitingTime, false);
		}

		// 2. 关闭入口
		type = "web".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(waitingTime, false);
		}
		type = "timer".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(waitingTime, false);
		}
		type = "mq".toUpperCase();
		if (componentMap.containsKey("mq-listener".toUpperCase())) {
			componentMap.get("mq-listener".toUpperCase()).getComponent().stop(waitingTime, false);
		}

		// 3. 停止服务 TODO

		type = "sql".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(waitingTime, wait);
		}

		type = "java".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(waitingTime, wait);
		}

		type = "mongo".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(waitingTime, wait);
		}

		type = "hbase".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(waitingTime, wait);
		}

		type = "hive".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(waitingTime, wait);
		}

		type = "es".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(waitingTime, wait);
		}

		type = "mq".toUpperCase();
		if (componentMap.containsKey("mq-service".toUpperCase())) {
			componentMap.get("mq-service".toUpperCase()).getComponent().stop(waitingTime, wait);
		}

		// 4. 停止基础组件

		type = "rpc".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(waitingTime, wait);
		}
		type = "cache".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(waitingTime, wait);
		}
		type = "validate".toUpperCase();
		if (componentMap.containsKey(type)) {
			componentMap.get(type).getComponent().stop(waitingTime, wait);
		}

		// 5. 关闭线程池
		if (null != threadPool) {
			threadPool.stop();
		}

		executeSSAop(this.closingAfterList);

		HttpClientManager.getInstance().shutdown();

		this.state = ComponentState.CLOSED;

		reportClosed();

		log.info(TangYuanLang.get("tangyuan.stopping.successfully"));
	}

}
