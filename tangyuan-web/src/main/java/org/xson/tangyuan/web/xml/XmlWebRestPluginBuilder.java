package org.xson.tangyuan.web.xml;

import java.util.List;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;
import org.xson.tangyuan.web.ResponseHandler;
import org.xson.tangyuan.web.WebComponent;
import org.xson.tangyuan.web.rest.RESTControllerVo;
import org.xson.tangyuan.web.rest.RestURIParser;
import org.xson.tangyuan.web.rest.RestURIVo;
import org.xson.tangyuan.web.util.WebUtil;
import org.xson.tangyuan.web.xml.vo.InterceptVo.InterceptType;
import org.xson.tangyuan.web.xml.vo.MethodObject;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

/**
 * Rest Plugin builder
 */
public class XmlWebRestPluginBuilder extends XmlWebPluginBuilder {

	private RestURIParser restURIParser = new RestURIParser();

	@Override
	public void clean() {
		super.clean();
		this.restURIParser = null;
	}

	public void configurationElement() throws Throwable {
		buildRestControllerNode(this.root.evalNodes("c"));// rest
	}
	// private void buildControllerNode(List<XmlNodeWrapper> contexts) throws Throwable {
	// String tagName = "c";
	// for (XmlNodeWrapper xNode : contexts) {
	//
	// String url = getStringFromAttr(xNode, "url", lang("xml.tag.attribute.empty", "url", tagName, this.resource));
	// String _requestType = getStringFromAttr(xNode, "type");
	// String transfer = getStringFromAttr(xNode, "transfer");
	// String validate = getStringFromAttr(xNode, "validate");
	// String exec = getStringFromAttr(xNode, "exec");
	// String permission = getStringFromAttr(xNode, "permission");
	// String _cacheUse = getStringFromAttr(xNode, "cacheUse");
	// boolean cacheInAop = getBoolFromAttr(xNode, "cacheInAop", WebComponent.getInstance().isCacheInAop());
	// String _convert = getStringFromAttr(xNode, "converter");
	//
	// // 基于安全考虑, 不能支持*,
	// if (this.componentContext.getControllerMap().containsKey(url)) {
	// throw new XmlParseException(lang("xml.tag.x.repeated", "url", url, tagName, this.resource));
	// }
	// DataConverter convert = getDataConverter(_convert, tagName);
	// CacheUseVo cacheUse = parseCacheUse(_cacheUse, url);
	// RequestTypeEnum requestType = getRequestType(_requestType, url);
	//
	// if (null != validate) {
	// validate = parseValidate(validate, url);
	// }
	//
	// MethodObject execMethod = null;
	// if (null != exec) {
	// execMethod = this.componentContext.getMethodObject(exec);
	// }
	// if (null == transfer && null == execMethod) {
	// transfer = url;
	// }
	// if (null != transfer) {
	// transfer = parseTransfer(transfer, url);
	// transfer = urlToServiceName(transfer);
	// }
	//
	// List<InterceptVo> assemblyList = buildInnerInterceptNode(xNode.evalNodes("assembly"), tagName + ".assembly");
	// List<InterceptVo> beforeList = buildInnerInterceptNode(xNode.evalNodes("before"), tagName + ".before");
	// List<InterceptVo> afterList = buildInnerInterceptNode(xNode.evalNodes("after"), tagName + ".after");
	//
	// ResponseHandler responseHandler = getResponseHandler(url);
	//
	// ControllerVo cVo = new ControllerVo(url, requestType, transfer, validate, execMethod, getInterceptList(url, assemblyList,
	// InterceptType.ASSEMBLY),
	// getInterceptList(url, beforeList, InterceptType.BEFORE), getInterceptList(url, afterList, InterceptType.AFTER), permission, cacheUse, convert,
	// cacheInAop,
	// responseHandler);
	//
	// // log.info("Add <c> :" + cVo.getUrl());
	//
	// this.componentContext.getControllerMap().put(url, cVo);
	// log.info(lang("add.tag", tagName, url));
	// }
	// }

	private void buildRestControllerNode(List<XmlNodeWrapper> contexts) throws Throwable {

		String tagName = "c";

		for (XmlNodeWrapper xNode : contexts) {

			String originalUrl = getStringFromAttr(xNode, "url", lang("xml.tag.attribute.empty", "url", tagName, this.resource));
			String _requestType = getStringFromAttr(xNode, "type");
			String transfer = getStringFromAttr(xNode, "transfer");
			String validate = getStringFromAttr(xNode, "validate");
			String exec = getStringFromAttr(xNode, "exec");
			String permission = getStringFromAttr(xNode, "permission");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			boolean cacheInAop = getBoolFromAttr(xNode, "cacheInAop", WebComponent.getInstance().isCacheInAop());
			String _convert = getStringFromAttr(xNode, "converter");
			String desc = getStringFromAttr(xNode, "desc");
			String[] groups = getStringArrayFromAttr(xNode, "group");

			// 解析URI
			RestURIVo restURIVo = restURIParser.parseURI(originalUrl);
			String url = restURIVo.getPath();

			DataConverter convert = getDataConverter(_convert, originalUrl, tagName);
			CacheUseVo cacheUse = parseCacheUse(_cacheUse, url);
			RequestTypeEnum requestType = getRestRequestType(_requestType, originalUrl);

			if (this.componentContext.existRestURI(WebUtil.getRestKey(requestType, url))) {
				throw new XmlParseException(lang("xml.tag.x.repeated", "url", url, tagName, this.resource));
			}

			if (null != validate) {
				validate = parseValidate(validate, url);
			}

			MethodObject execMethod = null;
			if (null != exec) {
				execMethod = this.componentContext.getMethodObject(exec);
			}
			if (null == transfer && null == execMethod) {
				transfer = url;
			}
			if (null != transfer) {
				transfer = parseTransfer(transfer, url);
				transfer = urlToServiceName(transfer);
			}

			// List<InterceptVo> assemblyList = buildInnerInterceptNode(xNode.evalNodes("assembly"), tagName + ".assembly");
			// List<InterceptVo> beforeList = buildInnerInterceptNode(xNode.evalNodes("before"), tagName + ".before");
			// List<InterceptVo> afterList = buildInnerInterceptNode(xNode.evalNodes("after"), tagName + ".after");

			ResponseHandler responseHandler = getResponseHandler(url);

			///////////////////////////////////////////////////////

			// String fullUrl = StringUtils.trim(context.getStringAttribute("url"));
			// String _requestType = StringUtils.trim(context.getStringAttribute("type"));
			// TangYuanAssert.stringEmpty(fullUrl, "In tag <c>, attribute 'url' can not be empty.");
			// TangYuanAssert.stringEmpty(_requestType, "Invalid attribute 'type' in controller " + fullUrl);
			// RequestTypeEnum requestType = getRestRequestType(_requestType, fullUrl);

			// 解析URI
			// RestURIVo restURIVo = restURIParser.parseURI(fullUrl);
			// String url = restURIVo.getPath();

			// 加上type进行URL判断
			// if (this.context.existRestURI(RestUtil.getRestKey(requestType, url))) {
			// throw new XmlParseException("Duplicate URL: " + url);
			// }

			// String transfer = StringUtils.trim(context.getStringAttribute("transfer"));
			// String validate = StringUtils.trim(context.getStringAttribute("validate"));
			// if (null != validate) {
			// validate = parseValidate(validate, url);// 这里如果出现变量{x},则是使用错误
			// }
			//
			// String exec = StringUtils.trim(context.getStringAttribute("exec"));
			// MethodObject execMethod = null;
			// if (null != exec) {
			// execMethod = getMethodObject(exec);
			// }
			//
			// if (null == transfer && null == execMethod) {
			// throw new XmlParseException("Attribute 'transfer' and attribute 'exec' can not be null at the same time in controller " + url);
			// }
			// if (null != transfer) {
			// transfer = parseTransfer(transfer, url);
			// transfer = urlToServiceName(transfer);
			// }

			// // 权限
			// String permission = StringUtils.trim(context.getStringAttribute("permission"));
			// // 缓存
			// String _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
			// CacheUseVo cacheUse = null;
			// if (null != _cacheUse && _cacheUse.length() > 0) {
			// // cacheUse = parseCacheUse(_cacheUse, fullUrl);// 如果URI中包含变量需要注意, 不要出现'{}'
			// cacheUse = parseCacheUse(_cacheUse, parseRestCacheKey(fullUrl));// 现在可以了
			// }

			// 数据转换
			// String _convert = StringUtils.trim(context.getStringAttribute("converter"));
			// DataConverter convert = getDataConverter(_convert);

			// boolean cacheInAop = WebComponent.getInstance().isCacheInAop();
			// String _cacheInAop = StringUtils.trim(context.getStringAttribute("cacheInAop"));
			// if (null != _cacheInAop) {
			// cacheInAop = Boolean.parseBoolean(_cacheInAop);
			// }

			// // 私有的assembly
			// List<InterceptVo> assemblyList = new ArrayList<InterceptVo>();
			// buildInnerInterceptNode(context.evalNodes("assembly"), assemblyList);
			//
			// // 私有的before
			// List<InterceptVo> beforeList = new ArrayList<InterceptVo>();
			// buildInnerInterceptNode(context.evalNodes("before"), beforeList);
			//
			// // 私有的after
			// List<InterceptVo> afterList = new ArrayList<InterceptVo>();
			// buildInnerInterceptNode(context.evalNodes("after"), afterList);
			//
			// ResponseHandler responseHandler = getResponseHandler(url);

			// RESTControllerVo cVo = new RESTControllerVo(originalUrl, url, requestType, transfer, validate, execMethod,
			// getInterceptList(url, assemblyList, InterceptType.ASSEMBLY), getInterceptList(url, beforeList, InterceptType.BEFORE),
			// getInterceptList(url, afterList, InterceptType.AFTER), permission, cacheUse, convert, cacheInAop, responseHandler, restURIVo);

			RESTControllerVo cVo = new RESTControllerVo(originalUrl, url, requestType, transfer, validate, execMethod,
					getInterceptList(url, InterceptType.ASSEMBLY), getInterceptList(url, InterceptType.BEFORE),
					getInterceptList(url, InterceptType.AFTER), permission, cacheUse, convert, cacheInAop, responseHandler, restURIVo, desc, groups);

			this.componentContext.addRestController(cVo);
			log.info("Add <c> :" + originalUrl + "\t" + requestType);
		}
	}

	private RequestTypeEnum getRestRequestType(String requestType, String url) {
		if (RequestTypeEnum.GET.toString().equalsIgnoreCase(requestType)) {
			return RequestTypeEnum.GET;
		}
		if (RequestTypeEnum.POST.toString().equalsIgnoreCase(requestType)) {
			return RequestTypeEnum.POST;
		}
		if (RequestTypeEnum.PUT.toString().equalsIgnoreCase(requestType)) {
			return RequestTypeEnum.PUT;
		}
		if (RequestTypeEnum.DELETE.toString().equalsIgnoreCase(requestType)) {
			return RequestTypeEnum.DELETE;
		}
		throw new XmlParseException("Invalid attribute type[" + requestType + "], in controller " + url);
	}

	protected String parseRestCacheKey(String uri) {
		if (uri.indexOf("{}") > -1) {
			String regex = "([\\?\\&])([^#=\\{]+)(=\\{\\})";
			return uri.replaceAll(regex, "$1$2={$2}");
		}
		return uri;
	}

}
