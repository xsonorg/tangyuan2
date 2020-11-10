package org.xson.tangyuan.web;

import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.Version;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.TangYuanState.ComponentState;
import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;
import org.xson.tangyuan.web.rest.RESTContainer;
import org.xson.tangyuan.web.xml.XmlWebComponentBuilder;
import org.xson.tangyuan.web.xml.XmlWebContext;

public class WebComponent implements TangYuanComponent {

	private static WebComponent	instance	= new WebComponent();

	private Log					log			= LogFactory.getLog(getClass());

	static {
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "web"));
	}

	private WebComponent() {
	}

	public static WebComponent getInstance() {
		return instance;
	}

	public ThreadLocal<RequestContext>	requestContextThreadLocal	= new ThreadLocal<RequestContext>();

	protected Map<String, ControllerVo>	controllerMap				= null;
	protected RESTContainer				restContainer				= null;

	private volatile ComponentState		state						= ComponentState.UNINITIALIZED;

	private int							errorCode					= -1;
	private String						errorMessage				= "系统错误";
	private String						errorRedirectPage			= "/404.html";
	private int							order						= 10;
	private boolean						cacheInAop					= true;

	/** 是否存在本地服务 */
	private boolean						existLocalService			= false;
	/** URL默认映射模式 */
	private boolean						urlAutoMappingMode			= false;
	/** 控制器直接向后台转发模式 */
	private boolean						forwardingMode				= false;
	/** REST API模式 */
	private boolean						restMode					= false;

	public void setControllerMap(Map<String, ControllerVo> controllerMap) {
		if (null == this.controllerMap) {
			this.controllerMap = controllerMap;
		}
	}

	public void setRestContainer(RESTContainer restContainer) {
		this.restContainer = restContainer;
	}

	protected ControllerVo getControllerVo(RequestTypeEnum requestType, String path) {

		if (this.restMode) {
			// REST模式下
			return this.restContainer.getControllerVo(requestType, path);
		}

		// if(this.forwardingMode){
		// }

		return this.controllerMap.get(path);
	}

	public void config(Map<String, String> properties) {

		if (properties.containsKey("errorCode".toUpperCase())) {
			this.errorCode = Integer.parseInt(properties.get("errorCode".toUpperCase()));
		}

		if (properties.containsKey("errorMessage".toUpperCase())) {
			this.errorMessage = properties.get("errorMessage".toUpperCase());
		}

		if (properties.containsKey("order".toUpperCase())) {
			this.order = Integer.parseInt(properties.get("order".toUpperCase()));
		}
		if (properties.containsKey("cacheInAop".toUpperCase())) {
			this.cacheInAop = Boolean.parseBoolean(properties.get("cacheInAop".toUpperCase()));
		}

		if (properties.containsKey("errorRedirectPage".toUpperCase())) {
			this.errorRedirectPage = properties.get("errorRedirectPage".toUpperCase());
		}

		// if (properties.containsKey("printResultLog".toUpperCase())) {
		// this.printResultLog = Boolean.parseBoolean(properties.get("printResultLog".toUpperCase()));
		// }

		if (properties.containsKey("urlAutoMappingMode".toUpperCase())) {
			this.urlAutoMappingMode = Boolean.parseBoolean(properties.get("urlAutoMappingMode".toUpperCase()));
		}

		if (properties.containsKey("forwardingMode".toUpperCase())) {
			this.forwardingMode = Boolean.parseBoolean(properties.get("forwardingMode".toUpperCase()));
		}

		if (properties.containsKey("restMode".toUpperCase())) {
			this.restMode = Boolean.parseBoolean(properties.get("restMode".toUpperCase()));
		}

		if (this.urlAutoMappingMode && this.restMode) {
			// 自动映射模式和REST模式冲突
			throw new TangYuanException("auto-mapping URL mode and REST mode conflict.");
		}

		// TODO 模式冲突

		if (this.forwardingMode) {
			log.info("Turn on tangyuan-web auto-forwarding mode.");
		}

		if (this.urlAutoMappingMode) {
			log.info("Turn on tangyuan-web auto-mapping URL mode.");
		}

		if (this.restMode) {
			log.info("Turn on tangyuan-web rest mode.");
		}

		// log.info("config setting successfully.");
		log.info(TangYuanLang.get("config.property.load"), "web-component");
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getErrorRedirectPage() {
		return errorRedirectPage;
	}

	public int getOrder() {
		return order;
	}

	public boolean isCacheInAop() {
		return cacheInAop;
	}

	public boolean isForwardingMode() {
		return forwardingMode;
	}

	public boolean isRestMode() {
		return restMode;
	}

	/** 是否映射服务名 */
	public boolean isMappingServiceName() {
		if (this.urlAutoMappingMode && this.existLocalService) {
			return true;
		}
		return false;
	}

	private void loadLang() {
		try {
			TangYuanLang.getInstance().load("tangyuan-lang-web");
		} catch (Throwable e) {
			log.error(e);
		}
	}

	public void start(String resource) throws Throwable {

		log.info(TangYuanLang.get("component.dividing.line"));
		log.info(TangYuanLang.get("component.starting"), "web", Version.getVersion());

		this.state = ComponentState.INITIALIZING;

		loadLang();

		// 是否存在本地服务 TODO
		this.existLocalService = TangYuanContainer.getInstance().getServicesKeySet().size() > 0;

		XmlWebContext componentContext = new XmlWebContext();
		componentContext.setXmlContext(TangYuanContainer.getInstance().getXmlGlobalContext());

		XmlWebComponentBuilder builder = new XmlWebComponentBuilder();
		builder.parse(componentContext, resource);
		componentContext.clean();

		this.state = ComponentState.RUNNING;

		log.info(TangYuanLang.get("component.starting.successfully"), "web");
	}

	@Override
	public void stop(long waitingTime, boolean asyn) {
		if (ComponentState.CLOSED != this.state) {
			this.state = ComponentState.CLOSED;
			log.info("web component stop successfully.");
		}
	}

	public boolean isRunning() {
		return ComponentState.RUNNING == this.state;
	}

}
