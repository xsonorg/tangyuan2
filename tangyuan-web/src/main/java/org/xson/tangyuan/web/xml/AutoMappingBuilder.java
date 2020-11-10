package org.xson.tangyuan.web.xml;

public class AutoMappingBuilder extends ControllerBuilder {

	// private Log log = LogFactory.getLog(getClass());
	//
	// public AutoMappingBuilder(XMLWebContext context) {
	// this.context = context;
	// }
	//
	// /** 解析控制器,自动映射模式 */
	// public void build() {
	// Set<String> serviceKeys = TangYuanContainer.getInstance().getServicesKeySet();
	// for (String key : serviceKeys) {
	// String url = serviceNameToUrl(key);
	// if (this.context.getControllerMap().containsKey(url)) {
	// throw new XmlParseException("Duplicate URL: " + url);
	// }
	//
	// RequestTypeEnum requestType = null;
	// String transfer = key;
	// String validate = null;
	// MethodObject execMethod = null;
	// String permission = null;
	// CacheUseVo cacheUse = null;
	// DataConverter convert = null;
	// boolean cacheInAop = WebComponent.getInstance().isCacheInAop();
	//
	// List<InterceptVo> assemblyList = new ArrayList<InterceptVo>();
	// List<InterceptVo> beforeList = new ArrayList<InterceptVo>();
	// List<InterceptVo> afterList = new ArrayList<InterceptVo>();
	//
	// ControllerVo cVo = new ControllerVo(url, requestType, transfer, validate, execMethod, getInterceptList(url, assemblyList,
	// InterceptType.ASSEMBLY),
	// getInterceptList(url, beforeList, InterceptType.BEFORE), getInterceptList(url, afterList, InterceptType.AFTER), permission, cacheUse, convert,
	// cacheInAop,
	// null);
	//
	// this.context.getControllerMap().put(cVo.getUrl(), cVo);
	// log.info("Add auto <c> :" + cVo.getUrl());
	// }
	// }
}
