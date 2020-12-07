package org.xson.tangyuan.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.aop.Aop;
import org.xson.tangyuan.aop.sys.SystemAopHandler;
import org.xson.tangyuan.aop.sys.SystemAopVo;
import org.xson.tangyuan.app.AppPlaceholder;
import org.xson.tangyuan.app.AppProperty;
import org.xson.tangyuan.app.SystemProperty;
import org.xson.tangyuan.client.http.HttpClientManager;
import org.xson.tangyuan.client.http.HttpClientVo;
import org.xson.tangyuan.manager.TangYuanManager;
import org.xson.tangyuan.manager.conf.ResourceReloaderVo;
import org.xson.tangyuan.rpc.RpcPlaceHolderHandler;
import org.xson.tangyuan.service.Actuator;
import org.xson.tangyuan.service.ActuatorImpl;
import org.xson.tangyuan.util.PlaceholderResourceSupport;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.nsarg.XmlExtNsArg;

public class XmlTangYuanBuilder extends DefaultXmlComponentBuilder {

	private List<SystemAopVo> startingBeforeList = new ArrayList<SystemAopVo>();
	private List<SystemAopVo> startingAfterList  = new ArrayList<SystemAopVo>();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		this.globalContext = (XmlGlobalContext) xmlContext;
		this.init(resource, "tangyuan-component", false);
		this.configurationElement();
		this.clean();
	}

	@Override
	public void clean() {
		super.clean();
		startingBeforeList.clear();
		startingAfterList.clear();
		startingBeforeList = null;
		startingAfterList = null;
	}

	private void configurationElement() throws Throwable {
		// 添加系统变量
		XmlExtNsArg.getInstance().addExtNsArg(SystemProperty.extNsPrefix, SystemProperty.getInstance());

		// 解析占位属性
		buildPlaceholderNode(this.root.evalNodes("app-placeholder"));
		// 解析App配置文件
		buildAppPropertyNode(this.root.evalNodes("app-property"));
		// 解析配置项
		buildConfigNode(this.root.evalNodes("config-property"), TangYuanContainer.getInstance());
		// 解析HttpClient
		buildHttpClientNode(this.root.evalNodes("httpclient"));
		// 解析线程池
		buildThreadPoolNode(this.root.evalNodes("thread-pool"));
		// 配置系统拦截器
		buildSystemAopNode(this.root.evalNodes("system-aop"));
		executeSSAop(startingBeforeList, false);
		buildComponentNode(this.root.evalNodes("component"));
		executeSSAop(startingAfterList, false);
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

	/**
	 * 解析app-placeholder: 支持多个
	 */
	private void buildPlaceholderNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String       tagName   = "app-placeholder";
		List<String> resources = new ArrayList<String>();
		for (XmlNodeWrapper xNode : contexts) {
			String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
			resources.add(resource);
			log.info(lang("xml.tag.resource.load", tagName, resource));
		}
		if (resources.size() > 0) {
			// register ResourceReloader
			AppPlaceholder.init(resources);
			for (String resource : resources) {
				XmlGlobalContext.addReloaderVo(new ResourceReloaderVo(resource, AppPlaceholder.getInstance()));
			}
		}
	}

	/**
	 * 解析app-property: 支持多个
	 */
	private void buildAppPropertyNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String       tagName   = "app-property";
		List<String> resources = new ArrayList<String>();
		for (XmlNodeWrapper xNode : contexts) {
			String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
			resources.add(resource);
			log.info(lang("xml.tag.resource.load", tagName, resource));
		}
		if (resources.size() > 0) {
			// register reloader
			AppProperty.init(resources);
			for (String resource : resources) {
				XmlGlobalContext.addReloaderVo(new ResourceReloaderVo(resource, AppProperty.getInstance()));
			}
			// 添加外部参数使用前缀
			XmlExtNsArg.getInstance().addExtNsArg(AppProperty.extNsPrefix, (AppProperty) AppProperty.getInstance());
		}
	}

	private void buildThreadPoolNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "thread-pool";
		int    size    = contexts.size();
		if (size > 1) {
			throw new XmlParseException(lang("xml.tag.mostone", tagName));
		}
		if (size == 0) {
			Properties p = new Properties();
			TangYuanContainer.getInstance().startThreadPool(p);
			return;
		}

		XmlNodeWrapper xNode    = contexts.get(0);
		String         resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
		Properties     p        = getProperties(resource, true, true);
		log.info(lang("xml.tag.resource.load", tagName, resource));
		TangYuanContainer.getInstance().startThreadPool(p);
	}

	private void buildHttpClientNode(List<XmlNodeWrapper> contexts) throws Throwable {
		// <httpclient id="client1" resource="http.client.properties"/>
		String               tagName = "httpclient";
		Map<String, Integer> hcMap   = new HashMap<String, Integer>();
		List<HttpClientVo>   voList  = new ArrayList<HttpClientVo>();
		for (XmlNodeWrapper xNode : contexts) {
			String id       = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
			if (hcMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			Properties   p    = getProperties(resource, true, true);
			HttpClientVo hcVo = new HttpClientVo(id, p);

			voList.add(hcVo);
			hcMap.put(id, 1);

			log.info(lang("add.tag", tagName, id));
		}

		HttpClientManager.getInstance().init(voList);
	}

	/**
	 * build SystemAopHandler
	 */
	private void buildSystemAopNode(List<XmlNodeWrapper> contexts) throws Throwable {
		if (contexts.size() == 0) {
			return;
		}

		List<SystemAopVo> closingBeforeList = new ArrayList<SystemAopVo>();
		List<SystemAopVo> closingAfterList  = new ArrayList<SystemAopVo>();

		for (XmlNodeWrapper context : contexts) {
			String               className     = getStringFromAttr(context, "class", lang("xml.tag.attribute.empty", "class", "system-aop", this.resource));
			String               type          = getStringFromAttr(context, "pointcut", lang("xml.tag.attribute.empty", "pointcut", "system-aop", this.resource));
			SystemAopHandler     handler       = getInstanceForName(className, SystemAopHandler.class,
					lang("xml.class.impl.interface", className, SystemAopHandler.class.getName()));

			Map<String, String>  properties    = new HashMap<String, String>();
			List<XmlNodeWrapper> innerContexts = context.evalNodes("property");
			for (XmlNodeWrapper innerContext : innerContexts) {
				String name  = getStringFromAttr(innerContext, "name", lang("xml.tag.attribute.empty", "name", "system-aop.property", this.resource));
				String value = getStringFromAttr(innerContext, "value", lang("xml.tag.attribute.empty", "value", "system-aop.property", this.resource));
				properties.put(name, value);
			}
			PlaceholderResourceSupport.processMap(properties);

			// startup-before
			// startup-after
			// shutdown-before
			// shutdown-after

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
			// log.info("add system-aop class: " + className);
			log.info(lang("add.tag.class"), "system-aop", className);
		}

		TangYuanContainer.getInstance().setClosingList(closingBeforeList, closingAfterList);
	}

	private void buildComponentNode(List<XmlNodeWrapper> contexts) throws Throwable {
		Map<String, String> resourceMap     = new HashMap<String, String>();
		Map<String, String> typeMap         = new HashMap<String, String>();
		Map<String, String> typeResourceMap = new HashMap<String, String>();
		for (XmlNodeWrapper context : contexts) {
			String resource = StringUtils.trim(context.getStringAttribute("resource"));
			String type     = StringUtils.trim(context.getStringAttribute("type"));
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

		String                type                  = null;
		Aop                   aop                   = null;
		boolean               onlyProxy             = false;
		RpcPlaceHolderHandler rpcPlaceHolderHandler = null;

		// 1. 启动监控
		type = "manager".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent("manager").start(typeResourceMap.get(type));
		}

		// 2. 启动基础组件

		type = "validate".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}
		type = "rpc".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}
		type = "cache".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		// 3. 启动服务组件

		type = "sql".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}
		type = "java".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}
		type = "mongo".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
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
		}
		type = "aop".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		// 注册服务
		if (0 == TangYuanContainer.getInstance().getServicesKeySet().size()) {
			onlyProxy = true;
		}
		aop = this.globalContext.getAop();
		rpcPlaceHolderHandler = this.globalContext.getRpcPlaceHolderHandler();
		ActuatorImpl actuatorImpl = ActuatorImpl.create(onlyProxy, aop, rpcPlaceHolderHandler, TangYuanManager.getInstance());
		Actuator.init(actuatorImpl);

		// 4. 启动入口组件

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

	private void forName(String name) throws Throwable {
		// ClassUtils.forName(name);
		Class.forName(name);
	}

	private void preLoadComponent(String type) throws Throwable {
		boolean unsupported = false;

		try {
			if ("sql".equalsIgnoreCase(type)) {
				forName("org.xson.tangyuan.sql.SqlComponent");
			} else if ("java".equalsIgnoreCase(type)) {
				forName("org.xson.tangyuan.java.JavaComponent");
			} else if ("mongo".equalsIgnoreCase(type)) {
				forName("org.xson.tangyuan.mongo.MongoComponent");
			}

			else if ("hbase".equalsIgnoreCase(type)) {
				forName("org.xson.tangyuan.hbase.HBaseComponent");
			} else if ("hive".equalsIgnoreCase(type)) {
				forName("org.xson.tangyuan.hive.HiveComponent");
			}

			else if ("es".equalsIgnoreCase(type)) {
				forName("org.xson.tangyuan.es.EsComponent");
			} else if ("mq".equalsIgnoreCase(type)) {
				// Class.forName("org.xson.tangyuan.mq.MqComponent");
				forName("org.xson.tangyuan.mq.MqContainer");
				forName("org.xson.tangyuan.mq.MqServiceComponent");
				forName("org.xson.tangyuan.mq.MqListenerComponent");
			} else if ("timer".equalsIgnoreCase(type)) {
				forName("org.xson.tangyuan.timer.TimerComponent");
			} else if ("rpc".equalsIgnoreCase(type)) {
				forName("org.xson.tangyuan.rpc.RpcComponent");
				// ClassUtils.forName("org.xson.tangyuan.rpc.RpcContainer");
				// ClassUtils.forName("org.xson.tangyuan.rpc.RpcClientComponent");
				// ClassUtils.forName("org.xson.tangyuan.rpc.RpcServerComponent");
			} else if ("web".equalsIgnoreCase(type)) {
				forName("org.xson.tangyuan.web.WebComponent");
			} else if ("cache".equalsIgnoreCase(type)) {
				forName("org.xson.tangyuan.cache.CacheComponent");
			} else if ("validate".equalsIgnoreCase(type)) {
				forName("org.xson.tangyuan.validate.ValidateComponent");
			} else if ("aop".equalsIgnoreCase(type)) {
				forName("org.xson.tangyuan.aop.AopComponent");
			}

			// else if ("tools".equalsIgnoreCase(type)) {
			// Class.forName("org.xson.tangyuan.tools.ToolsComponent");
			// }

			else if ("manager".equalsIgnoreCase(type)) {
				forName("org.xson.tangyuan.manager.ManagerComponent");
			}

			else {
				unsupported = true;
			}
		} catch (Throwable e) {
			throw new XmlParseException("missing component dependent jar: " + type, e);
		}

		if (unsupported) {
			throw new XmlParseException("Unsupported component type: " + type);
		}
	}

	//	@SuppressWarnings({ "rawtypes", "unchecked" })
	//	private void buildPlaceholderNode(List<XmlNodeWrapper> contexts) throws Throwable {
	//		String              tagName        = "app-placeholder";
	//		Map<String, String> placeholderMap = new HashMap<String, String>();
	//		List<String>        resources      = new ArrayList<String>();
	//		if (contexts.size() > 1) {
	//			throw new XmlParseException(lang("xml.tag.mostone", tagName));
	//		}
	//		for (XmlNodeWrapper xNode : contexts) {
	//			String     resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
	//			Properties props    = getProperties(resource, false, true);
	//			placeholderMap.putAll((Map) props);
	//			resources.add(resource);
	//			log.info(lang("xml.tag.resource.load", tagName, resource));
	//		}
	//		if (resources.size() > 0) {
	//			// register ResourceReloader
	//			ResourceReloader reloader = AppPlaceholder.getInstance(placeholderMap);
	//			for (String resource : resources) {
	//				XmlGlobalContext.addReloaderVo(new ResourceReloaderVo(resource, reloader));
	//			}
	//		}
	//	}
	//	private void buildAppPropertyNode(List<XmlNodeWrapper> contexts) throws Throwable {
	//		String       tagName     = "app-property";
	//		XCO          appProperty = new XCO();
	//		List<String> resources   = new ArrayList<String>();
	//		if (contexts.size() > 1) {
	//			throw new XmlParseException(lang("xml.tag.mostone", tagName));
	//		}
	//		for (XmlNodeWrapper xNode : contexts) {
	//			String      resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
	//			InputStream in       = getInputStream(resource, true, true);
	//			new INIXLoader().load(in, appProperty);
	//			in.close();
	//			resources.add(resource);
	//			log.info(lang("xml.tag.resource.load", tagName, resource));
	//		}
	//		if (resources.size() > 0) {
	//			// register reloader
	//			ResourceReloader reloader = AppProperty.getInstance(appProperty);
	//			for (String resource : resources) {
	//				XmlGlobalContext.addReloaderVo(new ResourceReloaderVo(resource, reloader));
	//			}
	//			// 添加外部参数使用前缀
	//			//			XmlExtNsArg.getInstance().addExtArg("APP:", appProperty);
	//			XmlExtNsArg.getInstance().addExtNsArg(AppProperty.extNsPrefix, (AppProperty) reloader);
	//		}
	//	}
}
