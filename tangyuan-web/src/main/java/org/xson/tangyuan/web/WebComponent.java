package org.xson.tangyuan.web;

import java.io.InputStream;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.web.xml.ControllerVo;
import org.xson.tangyuan.web.xml.XMLConfigBuilder;

public class WebComponent implements TangYuanComponent {

	private static WebComponent	instance	= new WebComponent();

	private Log					log			= LogFactory.getLog(getClass());

	static {
		// web 70 10
		// TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "web", 70, 10));
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "web"));
	}

	private WebComponent() {
	}

	public static WebComponent getInstance() {
		return instance;
	}

	public ThreadLocal<RequestContext>	requestContextThreadLocal	= new ThreadLocal<RequestContext>();
	protected Map<String, ControllerVo>	controllerMap				= null;

	private volatile boolean			initialization				= false;

	private int							errorCode					= -1;
	private String						errorMessage				= "系统错误";
	private String						errorRedirectPage			= "/404.html";
	private int							order						= 10;
	/** 远程服务模式|本地服务模式 */
	private boolean						localServiceMode			= false;
	/** URL默认映射模式 */
	private boolean						urlAutoMappingMode			= false;
	private boolean						cacheInAop					= true;
	/** 转发模式 */
	private boolean						forwardingMode				= false;
	/** K/V形式的参数的是否自动转换 */
	private boolean						kvAutoConvert				= false;

	// 控制器返回结果日志
	private boolean						printResultLog				= false;

	public void setControllerMap(Map<String, ControllerVo> controllerMap) {
		if (null == this.controllerMap) {
			this.controllerMap = controllerMap;
		}
	}

	protected ControllerVo getControllerVo(String url) {
		return controllerMap.get(url);
		// ControllerVo cVo = controllerMap.get(url);
		// if (null == cVo && forwardingMode) {
		// // TODO
		// }
		// return cVo;
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

		if (properties.containsKey("urlAutoMappingMode".toUpperCase())) {
			this.urlAutoMappingMode = Boolean.parseBoolean(properties.get("urlAutoMappingMode".toUpperCase()));
		}

		if (this.urlAutoMappingMode) {
			log.info("Turn on tangyuan-web auto-mapping URL mode.");
		}

		if (properties.containsKey("forwardingMode".toUpperCase())) {
			this.forwardingMode = Boolean.parseBoolean(properties.get("forwardingMode".toUpperCase()));
		}

		if (this.forwardingMode) {
			log.info("Turn on tangyuan-web auto-forwarding mode.");
		}

		if (properties.containsKey("kvAutoConvert".toUpperCase())) {
			this.kvAutoConvert = Boolean.parseBoolean(properties.get("kvAutoConvert".toUpperCase()));
		}
		if (this.kvAutoConvert) {
			log.info("Turn on tangyuan-web k/v parameter auto-convert mode.");
		}

		if (properties.containsKey("printResultLog".toUpperCase())) {
			this.printResultLog = Boolean.parseBoolean(properties.get("printResultLog".toUpperCase()));
		}

		log.info("config setting success, version: " + Version.getVersion());
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

	public boolean isRemoteServiceMode() {
		return !localServiceMode;
	}

	public boolean isKvAutoConvert() {
		return kvAutoConvert;
	}

	public boolean isPrintResultLog() {
		return printResultLog;
	}

	/** 是否映射服务名 */
	public boolean isMappingServiceName() {
		if (urlAutoMappingMode && localServiceMode) {
			return true;
		}
		return false;
	}

	@Override
	public void start(String resource) throws Throwable {
		if (!initialization) {
			log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			log.info("web component starting, version: " + Version.getVersion());
			// 是否存在本地服务
			localServiceMode = TangYuanContainer.getInstance().getServicesKeySet().size() > 0;
			log.info("*** Start parsing: " + resource);
			InputStream inputStream = Resources.getResourceAsStream(resource);
			XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
			builder.parseNode();
			initialization = true;
			log.info("web component successfully.");
		}
	}

	@Override
	public void stop(boolean wait) {
		if (initialization) {
			initialization = false;
			log.info("web component stop successfully.");
		}
	}

	public boolean isclosing() {
		return !initialization;
	}
}
