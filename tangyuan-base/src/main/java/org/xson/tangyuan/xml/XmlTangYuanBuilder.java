package org.xson.tangyuan.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.aop.sys.SystemAopHandler;
import org.xson.tangyuan.aop.sys.SystemAopVo;
import org.xson.tangyuan.app.AppProperty;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.trace.TrackingBuilder;
import org.xson.tangyuan.trace.TrackingManager;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.INIXLoader;
import org.xson.tangyuan.util.PlaceholderResourceSupport;
import org.xson.tangyuan.util.ResourceManager;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanAssert;
import org.xson.tangyuan.util.TangYuanUtil;

public class XmlTangYuanBuilder implements XmlExtendBuilder {

	private Log					log					= LogFactory.getLog(getClass());
	private XPathParser			xPathParser			= null;

	private List<SystemAopVo>	startingBeforeList	= new ArrayList<SystemAopVo>();
	private List<SystemAopVo>	startingAfterList	= new ArrayList<SystemAopVo>();

	private XCO					systemInfo			= new XCO();
	private XCO					appInfo				= null;

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = Resources.getResourceAsStream(resource);
		this.xPathParser = new XPathParser(inputStream);
		configurationElement(xPathParser.evalNode("/tangyuan-component"));
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildSystemInfo();
		buildPlaceholderNode(context.evalNodes("app-placeholder"));		// 解析占位属性
		buildAppPropertyNode(context.evalNodes("app-property"));		// 解析App配置文件
		buildConfigNodes(context.evalNodes("config-property"));			// 解析配置项
		setSystemAndAppInfo();
		buildThreadPoolNode(context.evalNodes("thread-pool"));			// 配置线程池
		buildTraceNode(context.evalNodes("trace-config"));				// 服务追踪配置
		buildSystemAopNodes(context.evalNodes("system-aop"));			// 配置系统拦截器

		executeSSAop(startingBeforeList, false);
		buildComponentNodes(context.evalNodes("component"));
		executeSSAop(startingAfterList, false);

		clean();
	}

	private void clean() {
		startingBeforeList.clear();
		startingAfterList.clear();
		startingBeforeList = null;
		startingAfterList = null;
	}

	private void executeSSAop(List<SystemAopVo> ssList, boolean ignoreException) throws Throwable {
		for (SystemAopVo ssVo : ssList) {
			try {
				ssVo.getHandler().execute(ssVo.getProperties());
				log.info("execute system-aop class: " + ssVo.getClassName());
			} catch (Throwable e) {
				if (ignoreException) {
					log.error("execute system-aop exception.", e);
				} else {
					throw e;
				}
			}
		}
	}

	private void buildSystemInfo() throws Throwable {
		String hostIp = TangYuanUtil.getHostIp();
		String hostName = TangYuanUtil.getHostName();

		this.systemInfo.setStringValue(AppProperty.HOST_IP, hostIp);
		this.systemInfo.setStringValue(AppProperty.HOST_NAME, hostName);
	}

	private void setSystemAndAppInfo() {
		XCO data = null;
		if (null == this.appInfo) {
			data = systemInfo;
			this.appInfo = new XCO();// fix bug
		} else {
			this.appInfo.append(systemInfo);
			data = this.appInfo;
		}
		String appName = TangYuanContainer.getInstance().getAppName();
		if (null != appName) {
			data.setStringValue(AppProperty.APP_NAME, appName);
		}

		appName = this.appInfo.getStringValue("appName");
		if (null != appName) {
			data.setStringValue(AppProperty.APP_NAME, appName);
		}

		if (!data.exists(AppProperty.APP_NAME)) {
			data.setStringValue(AppProperty.APP_NAME, TangYuanContainer.getInstance().getSystemName());
		}

		AppProperty.init(data);
		// 添加外部参数使用前缀
		TangYuanContainer.getInstance().getExtArg().addExtArg("EXT:", data);

		this.systemInfo = null;
		this.appInfo = null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void buildPlaceholderNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size > 1) {
			throw new XmlParseException("The <app-placeholder> node can have at most one.");
		}
		if (size == 0) {
			return;
		}

		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource"));
		TangYuanAssert.stringEmpty(resource, "in the <app-placeholder> tag, the 'resource' property can not be empty.");

		// TODO 这个要考虑加密和解密
		Properties props = ResourceManager.getProperties(resource);

		TangYuanContainer.getInstance().getXmlGlobalContext().setPlaceholderMap((Map) props);
		log.info("add app-placeholder resource: " + resource);
	}

	private void buildAppPropertyNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size > 1) {
			throw new XmlParseException("The <app-property> node can have at most one.");
		}
		if (size == 0) {
			return;
		}

		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource"));
		TangYuanAssert.stringEmpty(resource, "in the <app-property> tag, the 'resource' property can not be empty.");

		//		InputStream in = ResourceManager.getInputStream(resource, true);
		//		XCO data = new INIXLoader().load(in);
		//		in.close();
		//		AppProperty.init(data);
		//
		//		// 添加外部参数使用前缀
		//		TangYuanContainer.getInstance().getExtArg().addExtArg("EXT:", data);

		InputStream in = ResourceManager.getInputStream(resource, true);
		this.appInfo = new INIXLoader().load(in);
		in.close();

		log.info("add app-property resource: " + resource);
	}

	private void buildThreadPoolNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size > 1) {
			throw new XmlParseException("The <thread-pool> node can have at most one.");
		}
		if (size == 0) {
			Properties p = new Properties();
			TangYuanContainer.getInstance().startThreadPool(p);
			return;
		}

		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource"));
		TangYuanAssert.stringEmpty(resource, "in the <thread-pool> tag, the 'resource' property can not be empty.");

		InputStream in = ResourceManager.getInputStream(resource, true);
		Properties p = new Properties();
		p.load(in);
		in.close();

		TangYuanContainer.getInstance().startThreadPool(p);

		log.info("add thread-pool resource: " + resource);
	}

	private void buildTraceNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size > 1) {
			throw new XmlParseException("The <trace-config> node can have at most one.");
		}
		if (size == 0) {
			return;
		}

		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource"));
		TangYuanAssert.stringEmpty(resource, "in the <trace-config> tag, the 'resource' property can not be empty.");

		Class<?> clazz = ClassUtils.forName("org.xson.tangyuan.trace.DefaultTrackingBuilder");
		TrackingBuilder trackingBuilder = (TrackingBuilder) TangYuanUtil.newInstance(clazz);
		TrackingManager trackingManager = trackingBuilder.parse(resource);
		//		TrackingManager trackingManager = new TrackingBuilder().parse(resource);
		TangYuanContainer.getInstance().startTracking(trackingManager);

		log.info("add trace-config resource: " + resource);
	}

	private void buildConfigNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		// <config-property name="A" value="B" />
		Map<String, String> configMap = new HashMap<String, String>();
		for (XmlNodeWrapper context : contexts) {
			String name = StringUtils.trim(context.getStringAttribute("name"));
			String value = StringUtils.trim(context.getStringAttribute("value"));
			if (null == name || null == value) {
				throw new XmlParseException("<config-property> missing name or value");
			}
			configMap.put(name.toUpperCase(), value);
		}
		if (configMap.size() > 0) {
			//			PlaceholderResourceSupport.processMap(configMap, TangYuanContainer.getInstance().getXmlGlobalContext().getPlaceholderMap());
			PlaceholderResourceSupport.processMap(configMap);
			TangYuanContainer.getInstance().config(configMap);
		}
	}

	/**
	 * build SystemAopHandler
	 */
	private void buildSystemAopNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}

		List<SystemAopVo> closingBeforeList = new ArrayList<SystemAopVo>();
		List<SystemAopVo> closingAfterList = new ArrayList<SystemAopVo>();

		for (XmlNodeWrapper context : contexts) {
			String className = StringUtils.trim(context.getStringAttribute("class"));
			String type = StringUtils.trim(context.getStringAttribute("pointcut"));

			TangYuanAssert.stringEmpty(className, "in the <system-aop> tag, the 'class' property can not be empty.");
			TangYuanAssert.stringEmpty(type, "in the <system-aop> tag, the 'pointcut' property can not be empty.");

			Class<?> handlerClass = ClassUtils.forName(className);
			if (!SystemAopHandler.class.isAssignableFrom(handlerClass)) {
				throw new XmlParseException("system-aop class must implement 'SystemAopHandler' interface: " + className);
			}

			Map<String, String> properties = new HashMap<String, String>();
			List<XmlNodeWrapper> innerContexts = context.evalNodes("property");
			for (XmlNodeWrapper innerContext : innerContexts) {
				String name = StringUtils.trim(innerContext.getStringAttribute("name"));
				String value = StringUtils.trim(innerContext.getStringAttribute("value"));
				TangYuanAssert.stringEmpty(name, "in the <system-aop->property> tag, the 'name' property can not be empty.");
				TangYuanAssert.stringEmpty(value, "in the <system-aop->property> tag, the 'value' property can not be empty.");
				properties.put(name, value);
			}
			PlaceholderResourceSupport.processMap(properties, TangYuanContainer.getInstance().getXmlGlobalContext().getPlaceholderMap());

			// startup-before
			// startup-after
			// shutdown-before
			// shutdown-after

			SystemAopHandler handler = (SystemAopHandler) TangYuanUtil.newInstance(handlerClass);
			if ("startup-before".equalsIgnoreCase(type)) {
				this.startingBeforeList.add(new SystemAopVo(handler, properties, className));
			} else if ("startup-after".equalsIgnoreCase(type)) {
				this.startingAfterList.add(new SystemAopVo(handler, properties, className));
			} else if ("shutdown-before".equalsIgnoreCase(type)) {
				closingBeforeList.add(new SystemAopVo(handler, properties, className));
			} else if ("shutdown-after".equalsIgnoreCase(type)) {
				closingAfterList.add(new SystemAopVo(handler, properties, className));
			} else {
				throw new XmlParseException(TangYuanUtil.format("Unsupported system-aop pointcut: {}", type));
			}
			log.info("add system-aop class: " + className);
		}

		TangYuanContainer.getInstance().setClosingList(closingBeforeList, closingAfterList);
	}

	private void buildComponentNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		Map<String, String> resourceMap = new HashMap<String, String>();
		Map<String, String> typeMap = new HashMap<String, String>();
		Map<String, String> typeResourceMap = new HashMap<String, String>();
		for (XmlNodeWrapper context : contexts) {
			String resource = StringUtils.trim(context.getStringAttribute("resource"));
			String type = StringUtils.trim(context.getStringAttribute("type"));
			if (null == resource || null == type) {
				throw new XmlParseException("Startup component parameter is incomplete.");
			}
			if (resourceMap.containsKey(resource)) {
				throw new XmlParseException("Component resources are duplicated:" + resource);
			}
			if (typeMap.containsKey(type)) {
				throw new XmlParseException("Component type are duplicated:" + type);
			}
			preLoadComponent(type);
			resourceMap.put(resource, resource);
			typeMap.put(type, type);
			typeResourceMap.put(type.toUpperCase(), resource);
		}
		startupComponent(typeResourceMap);
		resourceMap = null;
		typeMap = null;
		typeResourceMap = null;
	}

	private void startupComponent(Map<String, String> typeResourceMap) throws Throwable {
		log.info("Ready to start all components.");
		// for (ComponentVo component : ComponentVo.sort(TangYuanContainer.getInstance().getComponents(), true)) {
		// component.getComponent().start(typeResourceMap.get(component.getType()));
		// }

		// int serviceComponent = 0;// TangYuan服务组件

		String type = "validate".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "rpc".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent("rpc-client").start(typeResourceMap.get(type));
		}

		type = "cache".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "sql".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
			// serviceComponent++;
		}

		type = "java".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
			// serviceComponent++;
		}

		type = "mongo".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
			// serviceComponent++;
		}

		type = "hbase".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "hive".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "es".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "mq".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent("mq-service").start(typeResourceMap.get(type));
			// serviceComponent++;
		}

		type = "aop".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
			ServiceActuator.openAop();
		}

		// TODO 在这里启动服务
		// 服务启动完毕:自动启动
		// ServiceActuator.start();

		// fix bug
		if (0 == TangYuanContainer.getInstance().getServicesKeySet().size()) {
			ServiceActuator.openOnlyProxyMode();
		}

		// fix
		type = "rpc".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent("rpc-server").start(typeResourceMap.get(type));
		}

		type = "mq".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent("mq-listener").start(typeResourceMap.get(type));
		}

		type = "timer".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "web".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}
	}

	private void preLoadComponent(String type) {
		try {
			if ("sql".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.sql.SqlComponent");
			} else if ("java".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.java.JavaComponent");
			} else if ("mongo".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.mongo.MongoComponent");
			}

			else if ("hbase".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.hbase.HBaseComponent");
			} else if ("hive".equalsIgnoreCase(type)) {
				// Class.forName("org.xson.tangyuan.mongo.MongoComponent");
			}

			else if ("es".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.es.EsComponent");
			} else if ("mq".equalsIgnoreCase(type)) {
				// Class.forName("org.xson.tangyuan.mq.MqComponent");
				Class.forName("org.xson.tangyuan.mq.MqContainer");
				Class.forName("org.xson.tangyuan.mq.MqServiceComponent");
				Class.forName("org.xson.tangyuan.mq.MqListenerComponent");
			} else if ("timer".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.timer.TimerComponent");
			} else if ("rpc".equalsIgnoreCase(type)) {
				// Class.forName("org.xson.tangyuan.rpc.RpcComponent");
				Class.forName("org.xson.tangyuan.rpc.RpcContainer");
				Class.forName("org.xson.tangyuan.rpc.RpcClientComponent");
				Class.forName("org.xson.tangyuan.rpc.RpcServerComponent");
			} else if ("web".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.web.WebComponent");
			} else if ("cache".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.cache.CacheComponent");
			} else if ("validate".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.validate.ValidateComponent");
			} else if ("aop".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.aop.AopComponent");
			} else {
				throw new XmlParseException("Unsupported component type: " + type);
			}
		} catch (ClassNotFoundException e) {
			throw new XmlParseException("missing component dependent jar: " + type, e);
		}
	}

}
