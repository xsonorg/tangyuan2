package org.xson.tangyuan.web.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.web.ControllerVo;
import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;
import org.xson.tangyuan.web.ResponseHandler;
import org.xson.tangyuan.web.WebComponent;
import org.xson.tangyuan.web.convert.DataConverterVo;
import org.xson.tangyuan.web.handler.ResponseConvertVo;
import org.xson.tangyuan.web.xml.vo.InterceptVo;
import org.xson.tangyuan.web.xml.vo.InterceptVo.InterceptType;
import org.xson.tangyuan.web.xml.vo.MethodObject;
import org.xson.tangyuan.xml.DefaultXmlPluginBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlWebPluginBuilder extends DefaultXmlPluginBuilder {

	// 引用标志
	protected String		refMark				= "@";
	protected String		urlSeparator		= "/";
	protected String		leftBrackets		= "{";
	protected String		rightBrackets		= "}";

	protected XmlWebContext	componentContext	= null;

	@Override
	public void setContext(String resource, XmlContext xmlContext) throws Throwable {
		this.componentContext = (XmlWebContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "web-controller", false);
		if (this.ns.length() > 0) {
			checkNs(this.ns);
		}
	}

	@Override
	public void clean() {
		super.clean();

		this.componentContext = null;
	}

	@Override
	public void parseRef() throws Throwable {
	}

	@Override
	public void parseService() throws Throwable {
		log.info(lang("xml.start.parsing.type", "plugin[controller]", this.resource));
		configurationElement();
	}

	public void configurationElement() throws Throwable {
		buildControllerNode(this.root.evalNodes("c"));
	}

	protected CacheUseVo parseCacheUse(String cacheUse, String url) {
		if (null == cacheUse) {
			return null;
		}
		return CacheUseVo.parseCacheUse(cacheUse, url);
	}

	private void buildControllerNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "c";
		for (XmlNodeWrapper xNode : contexts) {

			String url = getStringFromAttr(xNode, "url", lang("xml.tag.attribute.empty", "url", tagName, this.resource));
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

			// 基于安全考虑, 不能支持*,
			if (this.componentContext.getControllerMap().containsKey(url)) {
				throw new XmlParseException(lang("xml.tag.x.repeated", "url", url, tagName, this.resource));
			}
			DataConverter convert = getDataConverter(_convert, url, tagName);
			CacheUseVo cacheUse = parseCacheUse(_cacheUse, url);
			RequestTypeEnum requestType = getRequestType(_requestType, url);
			ResponseHandler responseHandler = getResponseHandler(url);

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

			ControllerVo cVo = new ControllerVo(url, requestType, transfer, validate, execMethod, getInterceptList(url, InterceptType.ASSEMBLY),
					getInterceptList(url, InterceptType.BEFORE), getInterceptList(url, InterceptType.AFTER), permission, cacheUse, convert,
					cacheInAop, responseHandler, desc, groups);

			this.componentContext.getControllerMap().put(url, cVo);
			log.info(lang("add.tag", tagName, url));
		}
	}

	/**
	 * URL合并
	 */
	protected String urlMerge(String domain, String path) {
		if (domain.endsWith(urlSeparator) && path.startsWith(urlSeparator)) {
			return domain + path.substring(1);
		} else if (domain.endsWith(urlSeparator) || path.startsWith(urlSeparator)) {
			return domain + path;
		} else {
			return domain + urlSeparator + path;
		}
	}

	// 解析转发URL, 忽略domain的处理
	protected String parseTransfer(String transfer, String url) {
		if (refMark.equals(transfer)) {
			return url;
		}
		// 第一种情况: {xxx}/@
		String regex = "\\{.*\\}/@";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(transfer);
		if (matcher.matches()) {
			int pos = transfer.indexOf(rightBrackets);
			String domainRef = transfer.substring(0, pos + 1);
			return urlMerge(domainRef, url);
		}
		// 第二种情况: {xxx}/axx/bxx
		// 第三种情况: 完全手写
		return transfer;
	}

	protected String parseValidate(String validate, String url) {
		if (refMark.equals(validate)) {
			String validateKey = url;
			if (validateKey.startsWith(urlSeparator)) {
				validateKey = validateKey.substring(1);
			}
			if (validateKey.endsWith(urlSeparator)) {
				validateKey = validateKey.substring(0, validateKey.length() - 1);
			}
			return validateKey;
		}
		return validate;
	}

	protected List<MethodObject> getInterceptList(String url, InterceptType type) {
		List<MethodObject> result = null;
		List<InterceptVo> matchList = new ArrayList<InterceptVo>();
		// 全局的
		List<InterceptVo> globalList = null;
		if (InterceptType.ASSEMBLY == type) {
			globalList = this.componentContext.getAssemblyList();
		} else if (InterceptType.BEFORE == type) {
			globalList = this.componentContext.getBeforeList();
		} else {
			globalList = this.componentContext.getAfterList();
		}
		for (InterceptVo baVo : globalList) {
			if (baVo.match(url)) {
				matchList.add(baVo);
			}
		}
		if (matchList.size() > 0) {
			Collections.sort(matchList);// sort
			result = new ArrayList<MethodObject>();
			for (InterceptVo baVo : matchList) {
				result.add(baVo.getMo());
			}
		}
		return result;
	}

	/** URL到本地服务名 */
	protected String urlToServiceName(String url) {
		if (null == url) {
			return null;
		}
		if (url.startsWith(urlSeparator)) {
			url = url.substring(1);
		}
		if (url.endsWith(urlSeparator)) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	protected DataConverter getDataConverter(String converter, String url, String tagName) {
		DataConverter instance = null;
		if (null != converter) {
			instance = this.componentContext.getConverterIdMap().get(converter);
			if (null == instance) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", converter, "converter", tagName, resource));
			}
			return instance;
		}

		List<DataConverterVo> dataConvertList = this.componentContext.getDataConvertList();
		for (DataConverterVo vo : dataConvertList) {
			if (vo.match(url)) {
				if (null != instance) {
					throw new XmlParseException(lang("web.converter.match.more", url));
				}
				instance = vo.getConverter();
			}
		}
		return instance;
	}

	protected ResponseHandler getResponseHandler(String url) {
		ResponseHandler handler = null;
		List<ResponseConvertVo> responseConvertList = this.componentContext.getResponseConvertList();
		for (ResponseConvertVo rcVo : responseConvertList) {
			if (rcVo.match(url)) {
				if (null != handler) {
					throw new XmlParseException(lang("web.response.match.more", url));
				}
				handler = rcVo.getResponseHandler();
			}
		}
		return handler;
	}

	private RequestTypeEnum getRequestType(String requestType, String url) {
		if (null == requestType || 0 == requestType.length()) {
			return null;
		}
		if (RequestTypeEnum.GET.toString().equalsIgnoreCase(requestType)) {
			return RequestTypeEnum.GET;
		}
		if (RequestTypeEnum.POST.toString().equalsIgnoreCase(requestType)) {
			return RequestTypeEnum.POST;
		}
		throw new XmlParseException("Unsupported type[" + requestType + "], in controller " + url);
	}

}
