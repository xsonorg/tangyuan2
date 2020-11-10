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
import org.xson.tangyuan.web.handler.ResponseConvertVo;
import org.xson.tangyuan.web.xml.vo.InterceptVo;
import org.xson.tangyuan.web.xml.vo.InterceptVo.InterceptType;
import org.xson.tangyuan.web.xml.vo.MethodObject;
import org.xson.tangyuan.xml.DefaultXmlPluginBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlWebPluginBuilder2 extends DefaultXmlPluginBuilder {

	// 引用标志
	protected String        refMark          = "@";
	protected String        urlSeparator     = "/";
	protected String        leftBrackets     = "{";
	protected String        rightBrackets    = "}";

	protected XmlWebContext componentContext = null;
	//	private RestURIParser   restURIParser    = new RestURIParser();

	@Override
	public void setContext(String resource, XmlContext xmlContext) throws Throwable {
		this.componentContext = (XmlWebContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "web-controller", false);
		if (this.ns.length() > 0) {
			checkNs(this.ns);// TODO
		}
	}

	@Override
	public void clean() {
		super.clean();

		this.componentContext = null;
		//		this.restURIParser = null;
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
		//		if (WebComponent.getInstance().isRestMode()) {
		//			buildRestControllerNode(this.root.evalNodes("c"));// rest
		//		} else {
		//			buildControllerNode(this.root.evalNodes("c"));
		//		}
		buildControllerNode(this.root.evalNodes("c"));
	}

	//	private void buildRestControllerNode(List<XmlNodeWrapper> contexts) throws Throwable {
	//		for (XmlNodeWrapper context : contexts) {
	//
	//			String fullUrl      = StringUtils.trim(context.getStringAttribute("url"));
	//			String _requestType = StringUtils.trim(context.getStringAttribute("type"));
	//			TangYuanAssert.stringEmpty(fullUrl, "In tag <c>, attribute 'url' can not be empty.");
	//			TangYuanAssert.stringEmpty(_requestType, "Invalid attribute 'type' in controller " + fullUrl);
	//			RequestTypeEnum requestType = getRestRequestType(_requestType, fullUrl);
	//
	//			// 解析URI
	//			RestURIVo       restURIVo   = restURIParser.parseURI(fullUrl);
	//			String          url         = restURIVo.getPath();
	//
	//			// 加上type进行URL判断
	//			if (this.context.existRestURI(RestUtil.getRestKey(requestType, url))) {
	//				throw new XmlParseException("Duplicate URL: " + url);
	//			}
	//
	//			String transfer = StringUtils.trim(context.getStringAttribute("transfer"));
	//			String validate = StringUtils.trim(context.getStringAttribute("validate"));
	//			if (null != validate) {
	//				validate = parseValidate(validate, url);// 这里如果出现变量{x},则是使用错误
	//			}
	//
	//			String       exec       = StringUtils.trim(context.getStringAttribute("exec"));
	//			MethodObject execMethod = null;
	//			if (null != exec) {
	//				execMethod = getMethodObject(exec);
	//			}
	//
	//			if (null == transfer && null == execMethod) {
	//				throw new XmlParseException("Attribute 'transfer' and attribute 'exec' can not be null at the same time in controller " + url);
	//			}
	//			if (null != transfer) {
	//				transfer = parseTransfer(transfer, url);
	//				transfer = urlToServiceName(transfer);
	//			}
	//
	//			// 权限
	//			String     permission = StringUtils.trim(context.getStringAttribute("permission"));
	//			// 缓存
	//			String     _cacheUse  = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//			CacheUseVo cacheUse   = null;
	//			if (null != _cacheUse && _cacheUse.length() > 0) {
	//				// cacheUse = parseCacheUse(_cacheUse, fullUrl);// 如果URI中包含变量需要注意, 不要出现'{}'
	//				cacheUse = parseCacheUse(_cacheUse, parseRestCacheKey(fullUrl));// 现在可以了
	//			}
	//
	//			// 数据转换
	//			String        _convert    = StringUtils.trim(context.getStringAttribute("converter"));
	//			DataConverter convert     = getDataConverter(_convert);
	//
	//			boolean       cacheInAop  = WebComponent.getInstance().isCacheInAop();
	//			String        _cacheInAop = StringUtils.trim(context.getStringAttribute("cacheInAop"));
	//			if (null != _cacheInAop) {
	//				cacheInAop = Boolean.parseBoolean(_cacheInAop);
	//			}
	//
	//			// 私有的assembly
	//			List<InterceptVo> assemblyList = new ArrayList<InterceptVo>();
	//			buildInnerInterceptNode(context.evalNodes("assembly"), assemblyList);
	//
	//			// 私有的before
	//			List<InterceptVo> beforeList = new ArrayList<InterceptVo>();
	//			buildInnerInterceptNode(context.evalNodes("before"), beforeList);
	//
	//			// 私有的after
	//			List<InterceptVo> afterList = new ArrayList<InterceptVo>();
	//			buildInnerInterceptNode(context.evalNodes("after"), afterList);
	//
	//			ResponseHandler  responseHandler = getResponseHandler(url);
	//
	//			RESTControllerVo cVo             = new RESTControllerVo(fullUrl, url, requestType, transfer, validate, execMethod,
	//					getInterceptList(url, assemblyList, InterceptType.ASSEMBLY), getInterceptList(url, beforeList, InterceptType.BEFORE),
	//					getInterceptList(url, afterList, InterceptType.AFTER), permission, cacheUse, convert, cacheInAop, responseHandler, restURIVo);
	//
	//			this.context.addRestController(cVo);
	//			// log.info("Add <c> :" + requestType + " " + fullUrl);
	//			log.info("Add <c> :" + fullUrl + "\t" + requestType);
	//		}
	//	}

	protected List<InterceptVo> buildInnerInterceptNode(List<XmlNodeWrapper> contexts, String tagName) throws Throwable {
		List<InterceptVo> interceptlist = new ArrayList<InterceptVo>();
		for (XmlNodeWrapper xNode : contexts) {
			String       call  = getStringFromAttr(xNode, "call", lang("xml.tag.attribute.empty", "call", tagName, this.resource));
			int          order = getIntFromAttr(xNode, "order", WebComponent.getInstance().getOrder());
			MethodObject mo    = this.componentContext.getMethodObject(call);
			InterceptVo  baVo  = new InterceptVo(mo, order);
			interceptlist.add(baVo);
		}
		return interceptlist;
	}

	//	private void buildInnerInterceptNode(List<XmlNodeWrapper> nodes, List<InterceptVo> interceptlist) throws Throwable {
	//		for (XmlNodeWrapper node : nodes) {
	//			String       call   = StringUtils.trim(node.getStringAttribute("call"));
	//			String       _order = StringUtils.trim(node.getStringAttribute("order"));
	//			MethodObject mo     = getMethodObject(call);
	//			int          order  = WebComponent.getInstance().getOrder();
	//			if (null != _order) {
	//				order = Integer.parseInt(_order);
	//			}
	//			InterceptVo baVo = new InterceptVo(mo, order);
	//			interceptlist.add(baVo);
	//		}
	//	}

	protected CacheUseVo parseCacheUse(String cacheUse, String url) {
		if (null == cacheUse) {
			return null;
		}
		return CacheUseVo.parseCacheUse(cacheUse, url);
	}

	private void buildControllerNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "c";
		for (XmlNodeWrapper xNode : contexts) {

			String  url          = getStringFromAttr(xNode, "url", lang("xml.tag.attribute.empty", "url", tagName, this.resource));
			String  _requestType = getStringFromAttr(xNode, "type");
			String  transfer     = getStringFromAttr(xNode, "transfer");
			String  validate     = getStringFromAttr(xNode, "validate");
			String  exec         = getStringFromAttr(xNode, "exec");
			String  permission   = getStringFromAttr(xNode, "permission");
			String  _cacheUse    = getStringFromAttr(xNode, "cacheUse");
			boolean cacheInAop   = getBoolFromAttr(xNode, "cacheInAop", WebComponent.getInstance().isCacheInAop());
			String  _convert     = getStringFromAttr(xNode, "converter");

			// 基于安全考虑, 不能支持*,
			if (this.componentContext.getControllerMap().containsKey(url)) {
				throw new XmlParseException(lang("xml.tag.x.repeated", "url", url, tagName, this.resource));
			}
			DataConverter   convert     = getDataConverter(_convert, tagName);
			CacheUseVo      cacheUse    = parseCacheUse(_cacheUse, url);
			RequestTypeEnum requestType = getRequestType(_requestType, url);

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

			List<InterceptVo> assemblyList    = buildInnerInterceptNode(xNode.evalNodes("assembly"), tagName + ".assembly");
			List<InterceptVo> beforeList      = buildInnerInterceptNode(xNode.evalNodes("before"), tagName + ".before");
			List<InterceptVo> afterList       = buildInnerInterceptNode(xNode.evalNodes("after"), tagName + ".after");

			ResponseHandler   responseHandler = getResponseHandler(url);

			//			ControllerVo      cVo             = new ControllerVo(url, requestType, transfer, validate, execMethod, getInterceptList(url, assemblyList, InterceptType.ASSEMBLY),
			//					getInterceptList(url, beforeList, InterceptType.BEFORE), getInterceptList(url, afterList, InterceptType.AFTER), permission, cacheUse, convert, cacheInAop,
			//					responseHandler);

			ControllerVo      cVo             = null;

			//			log.info("Add <c> :" + cVo.getUrl());

			this.componentContext.getControllerMap().put(url, cVo);
			log.info(lang("add.tag", tagName, url));
		}
	}

	// 基于安全考虑, 不能支持*,
	//			String          url          = StringUtils.trim(context.getStringAttribute("url"));
	//			if (this.componentContext.getControllerMap().containsKey(url)) {
	//				throw new RuntimeException("Duplicate URL: " + url);
	//			}
	//			String          _requestType = StringUtils.trim(context.getStringAttribute("type"));
	//			String          transfer     = StringUtils.trim(context.getStringAttribute("transfer"));
	//			String          validate     = StringUtils.trim(context.getStringAttribute("validate"));
	//			String       exec       = StringUtils.trim(context.getStringAttribute("exec"));
	// 权限
	//			String     permission = StringUtils.trim(context.getStringAttribute("permission"));
	// 缓存
	//			String     _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//			CacheUseVo cacheUse = null;
	//			if (null != _cacheUse && _cacheUse.length() > 0) {
	//				cacheUse = parseCacheUse(_cacheUse, url);
	//			}
	//			String            _convert     = StringUtils.trim(context.getStringAttribute("converter"));
	//			DataConverter     convert      = getDataConverter(_convert);
	//			boolean       cacheInAop  = WebComponent.getInstance().isCacheInAop();
	//			String        _cacheInAop = StringUtils.trim(context.getStringAttribute("cacheInAop"));
	//			if (null != _cacheInAop) {
	//				cacheInAop = Boolean.parseBoolean(_cacheInAop);
	//			}
	//			// 私有的assembly
	//			List<InterceptVo> assemblyList = new ArrayList<InterceptVo>();
	//			buildInnerInterceptNode(xNode.evalNodes("assembly"), assemblyList);
	//
	//			// 私有的before
	//			List<InterceptVo> beforeList = new ArrayList<InterceptVo>();
	//			buildInnerInterceptNode(xNode.evalNodes("before"), beforeList);
	//
	//			// 私有的after
	//			List<InterceptVo> afterList = new ArrayList<InterceptVo>();
	//			buildInnerInterceptNode(xNode.evalNodes("after"), afterList);

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

	// private String parseTransfer(String transfer, String url) {
	// if (refMark.equals(transfer)) {
	// return url;
	// }
	// // 第一种情况: {xxx}/@
	// String regex = "\\{.*\\}/@";
	// Pattern pattern = Pattern.compile(regex);
	// Matcher matcher = pattern.matcher(transfer);
	// if (matcher.matches()) {
	// int pos = transfer.indexOf(rightBrackets);
	// String domainRef = transfer.substring(1, pos);
	// String domain = this.bc.getDomainMap().get(domainRef);
	// if (null == domain) {
	// throw new XmlParseException("Domain reference error: " + transfer);
	// }
	// return urlMerge(domain, url);
	// }
	// // 第二种情况: {xxx}/axx/bxx
	// if (transfer.startsWith(leftBrackets)) {
	// int pos = transfer.indexOf(rightBrackets);
	// String domainRef = transfer.substring(1, pos);
	// String domain = this.bc.getDomainMap().get(domainRef);
	// if (null == domain) {
	// throw new XmlParseException("Domain reference error: " + transfer);
	// }
	// String path = transfer.substring(pos + 1);
	// return urlMerge(domain, path);
	// }
	// // 第三种情况: 完全手写
	// return transfer;
	// }

	// 解析转发URL, 忽略domain的处理
	protected String parseTransfer(String transfer, String url) {
		if (refMark.equals(transfer)) {
			return url;
		}
		// 第一种情况: {xxx}/@
		String  regex   = "\\{.*\\}/@";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(transfer);
		if (matcher.matches()) {
			int    pos       = transfer.indexOf(rightBrackets);
			// String domainRef = transfer.substring(1, pos);
			// String domain = this.bc.getDomainMap().get(domainRef);
			// if (null == domain) {
			// throw new XmlParseException("Domain reference error: " + transfer);
			// }
			// return urlMerge(domain, url);
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

	protected List<MethodObject> getInterceptList(String url, List<InterceptVo> list, InterceptType type) {
		List<MethodObject> result     = null;
		List<InterceptVo>  matchList  = new ArrayList<InterceptVo>();
		// 全局的
		List<InterceptVo>  globalList = null;

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
		if (list.size() > 0) {
			matchList.addAll(list);
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

	//	/**
	//	 * 解析: ID:xxx; key:xxx; expiry:1000<br />
	//	 * 最短cache="@"
	//	 */
	//	private CacheUseVo parseCacheUse(String cacheUse, String url) {
	//		CacheUseVo cacheUseVo = null;
	//		String[]   array      = cacheUse.split(";");
	//		if (array.length > 0) {
	//			// 特殊情况
	//			if (cacheUse.equals(refMark)) {
	//				TangYuanCache cache = CacheComponent.getInstance().getCache(null);
	//				if (null == cache) {
	//					throw new XmlParseException("Non-existent cache: " + cacheUse);
	//				}
	//				cacheUseVo = new CacheUseVo(cache, url, null, url);
	//			}
	//
	//			Map<String, String> map = new HashMap<String, String>();
	//			for (int i = 0; i < array.length; i++) {
	//				String[] item = array[i].split(":");
	//				map.put(item[0].trim().toUpperCase(), item[1].trim());
	//			}
	//			TangYuanCache cache = CacheComponent.getInstance().getCache(map.get("id".toUpperCase()));
	//			if (null == cache) {
	//				throw new XmlParseException("Non-existent cache: " + cacheUse);
	//			}
	//			String key = map.get("key".toUpperCase());
	//			if (null == key) {
	//				throw new XmlParseException("Missing cache.key: " + cacheUse);
	//			}
	//			Long expiry = null;
	//			if (map.containsKey("expiry".toUpperCase())) {
	//				expiry = Long.parseLong(map.get("expiry".toUpperCase()));
	//			}
	//			cacheUseVo = new CacheUseVo(cache, key, expiry, url);
	//		}
	//		return cacheUseVo;
	//	}

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
		// return url.replaceAll("/", ".");
		return url;
	}

	protected DataConverter getDataConverter(String converter, String tagName) {
		if (null == converter) {
			return null;
		}
		DataConverter instance = this.componentContext.getConverterIdMap().get(converter);
		if (null == instance) {
			throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", converter, "converter", tagName, resource));
		}
		return instance;
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

	//	private RequestTypeEnum getRestRequestType(String requestType, String url) {
	//		if (RequestTypeEnum.GET.toString().equalsIgnoreCase(requestType)) {
	//			return RequestTypeEnum.GET;
	//		}
	//		if (RequestTypeEnum.POST.toString().equalsIgnoreCase(requestType)) {
	//			return RequestTypeEnum.POST;
	//		}
	//		if (RequestTypeEnum.PUT.toString().equalsIgnoreCase(requestType)) {
	//			return RequestTypeEnum.PUT;
	//		}
	//		if (RequestTypeEnum.DELETE.toString().equalsIgnoreCase(requestType)) {
	//			return RequestTypeEnum.DELETE;
	//		}
	//		// if (RequestTypeEnum.HEAD.toString().equalsIgnoreCase(requestType)) {
	//		// return RequestTypeEnum.HEAD;
	//		// }
	//		// if (RequestTypeEnum.OPTIONS.toString().equalsIgnoreCase(requestType)) {
	//		// return RequestTypeEnum.OPTIONS;
	//		// }
	//		throw new XmlParseException("Invalid attribute type[" + requestType + "], in controller " + url);
	//	}

	//	private String parseRestCacheKey(String uri) {
	//		if (uri.indexOf("{}") > -1) {
	//			String regex = "([\\?\\&])([^#=\\{]+)(=\\{\\})";
	//			return uri.replaceAll(regex, "$1$2={$2}");
	//		}
	//		return uri;
	//	}

	protected ResponseHandler getResponseHandler(String url) {
		ResponseHandler         handler             = null;
		List<ResponseConvertVo> responseConvertList = this.componentContext.getResponseConvertList();
		for (ResponseConvertVo rcVo : responseConvertList) {
			if (rcVo.match(url)) {
				if (null != handler) {
					// TODO
					throw new XmlParseException("only one response converter can be bound to each controller: " + url);
				}
				handler = rcVo.getResponseHandler();
			}
		}
		return handler;
	}

	//	public static void main(String[] args) {
	//		// 正则需要转义字符：'$', '(', ')', '*', '+', '.', '[', ']', '?', '\\', '^', '{', '}', '|'
	//		String str   = "/aaa?axx={x}&bxx={}&cxx={ccc}";
	//		// String regex = "[\\?\\&][^#=\\{]+=\\{\\}";
	//		String regex = "([\\?\\&])([^#=\\{]+)(=\\{\\})";
	//		// Pattern pattern = Pattern.compile(regex);
	//		System.out.println(str.replaceAll(regex, "$1$2$3"));
	//		System.out.println(str.replaceAll(regex, "$1$2={$2}"));
	//	}

	/////////////////////

	//	private Log            log           = LogFactory.getLog(getClass());
	//	private XPathParser    parser        = null;
	//	private XmlNodeWrapper root          = null;
	//	public XMLPluginBuilder(InputStream inputStream, XMLWebContext context) {
	//		this.context = context;
	//		this.parser = new XPathParser(inputStream);
	//		this.root = this.parser.evalNode("/web-controller");
	//	}

	//	public void parseRef() throws Throwable {
	//		buildBeanNode(this.root.evalNodes("bean"));
	//		buildConverterNode(this.root.evalNodes("converter"));
	//		buildInterceptNode(this.root.evalNodes("assembly"), InterceptType.ASSEMBLY);
	//		buildInterceptNode(this.root.evalNodes("before"), InterceptType.BEFORE);
	//		buildInterceptNode(this.root.evalNodes("after"), InterceptType.AFTER);
	//		// 增加响应转换器
	//		buildResponseConvertNode(this.root.evalNodes("response"));
	//	}
	//	public void parseRef2() throws Throwable {
	//		buildConverterGroupNode(this.root.evalNodes("converter-group"));
	//	}

	//	private void buildBeanNode(List<XmlNodeWrapper> contexts) throws Throwable {
	//		// <bean id="" class="" />
	//		for (XmlNodeWrapper context : contexts) {
	//			String id    = StringUtils.trim(context.getStringAttribute("id"));
	//			String clazz = StringUtils.trim(context.getStringAttribute("class"));
	//
	//			TangYuanAssert.stringEmpty(id, "'id' attribute can not be null in node bean.");
	//			TangYuanAssert.stringEmpty(clazz, "'class' attribute can not be null in node bean.");
	//
	//			if (this.context.getBeanIdMap().containsKey(id)) {
	//				throw new XmlParseException("Duplicate Bean: " + id);
	//			}
	//			Object obj = this.context.getBeanClassMap().get(clazz);
	//			if (null == obj) {
	//				obj = TangYuanUtil.newInstance(ClassUtils.forName(clazz));
	//				this.context.getBeanClassMap().put(clazz, obj);
	//			}
	//			this.context.getBeanIdMap().put(id, obj);
	//			log.info("Add <bean> :" + clazz);
	//		}
	//	}

	//	private void buildConverterNode(List<XmlNodeWrapper> contexts) throws Throwable {
	//		for (XmlNodeWrapper context : contexts) {
	//			String id    = StringUtils.trim(context.getStringAttribute("id"));
	//			String clazz = StringUtils.trim(context.getStringAttribute("class"));
	//
	//			TangYuanAssert.stringEmpty(id, "'id' attribute can not be null in node converter.");
	//			TangYuanAssert.stringEmpty(clazz, "'class' attribute can not be null in node converter.");
	//
	//			if (this.context.getConverterIdMap().containsKey(id)) {
	//				throw new XmlParseException("Duplicate Converter: " + id);
	//			}
	//			DataConverter obj = this.context.getConverterClassMap().get(clazz);
	//			if (null == obj) {
	//				Class<?> converterClass = ClassUtils.forName(clazz);
	//				if (!DataConverter.class.isAssignableFrom(converterClass)) {
	//					throw new XmlParseException("Converter class not implement the DataConverter interface: " + clazz);
	//				}
	//				obj = (DataConverter) TangYuanUtil.newInstance(converterClass);
	//				this.context.getConverterClassMap().put(clazz, obj);
	//			}
	//			this.context.getConverterIdMap().put(id, obj);
	//			log.info("Add <converter> :" + clazz);
	//		}
	//	}

	//	private void buildConverterGroupNode(List<XmlNodeWrapper> contexts) throws Throwable {
	//		for (XmlNodeWrapper context : contexts) {
	//			String id = StringUtils.trim(context.getStringAttribute("id"));
	//			if (this.context.getConverterIdMap().containsKey(id)) {
	//				throw new XmlParseException("Duplicate converter: " + id);
	//			}
	//			List<XmlNodeWrapper> converterRefList = context.evalNodes("converter");
	//			TangYuanAssert.collectionEmpty(converterRefList, "Internal converter node can not be empty: " + id);
	//			List<DataConverter> converters = new ArrayList<DataConverter>();
	//			for (XmlNodeWrapper converterNode : converterRefList) {
	//				String        ref       = StringUtils.trim(converterNode.getStringAttribute("ref"));
	//				DataConverter converter = this.context.getConverterIdMap().get(ref);
	//				TangYuanAssert.objectEmpty(converter, "The referenced converter does not exist: " + ref);
	//				converters.add(converter);
	//			}
	//			this.context.getConverterIdMap().put(id, new MixedDataConverter(converters));
	//			log.info("Add <converter-group> :" + id);
	//		}
	//	}

	//	private void buildInterceptNode(List<XmlNodeWrapper> contexts, InterceptType type) throws Throwable {
	//		for (XmlNodeWrapper context : contexts) {
	//
	//			String call   = StringUtils.trim(context.getStringAttribute("call"));
	//			String _order = StringUtils.trim(context.getStringAttribute("order"));
	//
	//			if (InterceptType.ASSEMBLY == type) {
	//				if (this.context.getBeforeMap().containsKey(call)) {
	//					throw new XmlParseException("Duplicate Assembly: " + call);
	//				}
	//			} else if (InterceptType.BEFORE == type) {
	//				if (this.context.getBeforeMap().containsKey(call)) {
	//					throw new XmlParseException("Duplicate Before: " + call);
	//				}
	//			} else {
	//				if (this.context.getAfterMap().containsKey(call)) {
	//					throw new XmlParseException("Duplicate After: " + call);
	//				}
	//			}
	//
	//			MethodObject mo    = getMethodObject(call);
	//
	//			int          order = WebComponent.getInstance().getOrder();
	//			if (null != _order) {
	//				order = Integer.parseInt(_order);
	//			}
	//
	//			List<String>         includeList  = new ArrayList<String>();
	//			List<String>         excludeList  = new ArrayList<String>();
	//
	//			// <include></include>
	//			List<XmlNodeWrapper> includeNodes = context.evalNodes("include");
	//			for (XmlNodeWrapper include : includeNodes) {
	//				String body = StringUtils.trim(include.getStringBody());
	//				if (null != body) {
	//					includeList.add(body);
	//				}
	//			}
	//			if (includeList.size() == 0) {
	//				includeList = null;
	//			}
	//
	//			// <exclude></exclude>
	//			List<XmlNodeWrapper> excludeNodes = context.evalNodes("exclude");
	//			for (XmlNodeWrapper exclude : excludeNodes) {
	//				String body = StringUtils.trim(exclude.getStringBody());
	//				if (null != body) {
	//					excludeList.add(body);
	//				}
	//			}
	//			if (excludeList.size() == 0) {
	//				excludeList = null;
	//			}
	//
	//			if (null == includeList && null == excludeList) {
	//				throw new XmlParseException("Intercept missing <include|exclude>: " + call);
	//			}
	//
	//			InterceptVo baVo = new InterceptVo(mo, order, includeList, excludeList);
	//
	//			if (InterceptType.ASSEMBLY == type) {
	//				this.context.getAssemblyList().add(baVo);
	//				this.context.getAssemblyMap().put(call, call);
	//				log.info("Add <assembly> :" + call);
	//			} else if (InterceptType.BEFORE == type) {
	//				this.context.getBeforeList().add(baVo);
	//				this.context.getBeforeMap().put(call, call);
	//				log.info("Add <before> :" + call);
	//			} else {
	//				this.context.getAfterList().add(baVo);
	//				this.context.getAfterMap().put(call, call);
	//				log.info("Add <after> :" + call);
	//			}
	//		}
	//	}

	//	private void buildResponseConvertNode(List<XmlNodeWrapper> contexts) throws Throwable {
	//		for (XmlNodeWrapper context : contexts) {
	//
	//			String bean = StringUtils.trim(context.getStringAttribute("bean"));
	//			TangYuanAssert.stringEmpty(bean, "'bean' attribute can not be null in <response>.");
	//
	//			Object beanInstance = this.context.getBeanIdMap().get(bean);
	//			TangYuanAssert.objectEmpty(beanInstance, "reference bean does not exist: " + bean);
	//
	//			if (!(beanInstance instanceof ResponseHandler)) {
	//				throw new XmlParseException("instances of use of the bean[" + bean + "] attribute must implement the 'ResponseHandler' interface.");
	//			}
	//			ResponseHandler      handler      = (ResponseHandler) beanInstance;
	//
	//			List<String>         includeList  = new ArrayList<String>();
	//			List<String>         excludeList  = new ArrayList<String>();
	//
	//			// <include></include>
	//			List<XmlNodeWrapper> includeNodes = context.evalNodes("include");
	//			for (XmlNodeWrapper include : includeNodes) {
	//				String body = StringUtils.trim(include.getStringBody());
	//				if (null != body) {
	//					includeList.add(body);
	//				}
	//			}
	//			if (includeList.size() == 0) {
	//				includeList = null;
	//			}
	//
	//			// <exclude></exclude>
	//			List<XmlNodeWrapper> excludeNodes = context.evalNodes("exclude");
	//			for (XmlNodeWrapper exclude : excludeNodes) {
	//				String body = StringUtils.trim(exclude.getStringBody());
	//				if (null != body) {
	//					excludeList.add(body);
	//				}
	//			}
	//			if (excludeList.size() == 0) {
	//				excludeList = null;
	//			}
	//
	//			if (null == includeList && null == excludeList) {
	//				throw new XmlParseException("<response> node missing <include|exclude>: " + bean);
	//			}
	//
	//			ResponseConvertVo rcVo = new ResponseConvertVo(handler, includeList, excludeList);
	//			this.context.addResponseConvert(rcVo);
	//
	//			// log
	//			log.info("Add <response> :" + bean);
	//		}
	//	}

	//	private MethodObject getMethodObject(String str) throws Exception {
	//		MethodObject mo = null;
	//		// a.b
	//		if (str.startsWith(leftBrackets)) {
	//			String[] array = str.split("\\.");
	//			if (array.length != 2) {
	//				throw new XmlParseException("Invalid bean reference: " + str);
	//			}
	//			// String beanId = array[0].substring(1);
	//			String beanId     = array[0].substring(1, array[0].length() - 1);
	//			String methodName = array[1];
	//
	//			Object bean       = this.context.getBeanIdMap().get(beanId);
	//			if (null == bean) {
	//				throw new XmlParseException("Reference bean does not exist: " + beanId);
	//			}
	//			String moKey = bean.getClass().getName() + "." + methodName;
	//			mo = this.context.getMoMap().get(moKey);
	//			if (null == mo) {
	//				Method method = bean.getClass().getMethod(methodName, RequestContext.class);
	//				// 如果方法不存在或者参数类型不匹配，这里会抛出异常
	//				method.setAccessible(true);
	//				mo = new MethodObject(method, bean);
	//				this.context.getMoMap().put(moKey, mo);
	//			}
	//		} else {
	//			mo = this.context.getMoMap().get(str);
	//			if (null != mo) {
	//				return mo;
	//			}
	//			int      pos        = str.lastIndexOf(".");
	//			String   className  = str.substring(1, pos);
	//			String   methodName = str.substring(pos + 1, str.length());
	//
	//			Object   instance   = this.context.getBeanClassMap().get(className);
	//			Class<?> clazz      = null;
	//			if (null == instance) {
	//				clazz = Class.forName(className);
	//				instance = clazz.newInstance();
	//				this.context.getBeanClassMap().put(className, instance);
	//			} else {
	//				clazz = instance.getClass();
	//			}
	//			Method method = clazz.getMethod(methodName, RequestContext.class);
	//			// 如果方法不存在或者参数类型不匹配，这里会抛出异常
	//			method.setAccessible(true);
	//			mo = new MethodObject(method, instance);
	//			this.context.getMoMap().put(str, mo);
	//		}
	//		return mo;
	//	}

	//	private void buildControllerNode(List<XmlNodeWrapper> contexts) throws Throwable {
	//		for (XmlNodeWrapper context : contexts) {
	//			// 基于安全考虑, 不能支持*,
	//			String url = StringUtils.trim(context.getStringAttribute("url"));
	//			if (this.context.getControllerMap().containsKey(url)) {
	//				throw new RuntimeException("Duplicate URL: " + url);
	//			}
	//			String          _requestType = StringUtils.trim(context.getStringAttribute("type"));
	//			RequestTypeEnum requestType  = getRequestType(_requestType, url);
	//
	//			String          transfer     = StringUtils.trim(context.getStringAttribute("transfer"));
	//			String          validate     = StringUtils.trim(context.getStringAttribute("validate"));
	//			if (null != validate) {
	//				validate = parseValidate(validate, url);
	//			}
	//
	//			String       exec       = StringUtils.trim(context.getStringAttribute("exec"));
	//			MethodObject execMethod = null;
	//			if (null != exec) {
	//				execMethod = getMethodObject(exec);
	//			}
	//
	//			if (null == transfer && null == execMethod) {
	//				transfer = url;
	//			}
	//			if (null != transfer) {
	//				transfer = parseTransfer(transfer, url);
	//				transfer = urlToServiceName(transfer);
	//			}
	//
	//			// 权限
	//			String     permission = StringUtils.trim(context.getStringAttribute("permission"));
	//			// 缓存
	//			String     _cacheUse  = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//
	//			CacheUseVo cacheUse   = null;
	//			if (null != _cacheUse && _cacheUse.length() > 0) {
	//				cacheUse = parseCacheUse(_cacheUse, url);
	//			}
	//
	//			String        _convert    = StringUtils.trim(context.getStringAttribute("converter"));
	//			DataConverter convert     = getDataConverter(_convert);
	//
	//			boolean       cacheInAop  = WebComponent.getInstance().isCacheInAop();
	//			String        _cacheInAop = StringUtils.trim(context.getStringAttribute("cacheInAop"));
	//			if (null != _cacheInAop) {
	//				cacheInAop = Boolean.parseBoolean(_cacheInAop);
	//			}
	//
	//			// 私有的assembly
	//			List<InterceptVo> assemblyList = new ArrayList<InterceptVo>();
	//			buildInnerInterceptNode(context.evalNodes("assembly"), assemblyList);
	//
	//			// 私有的before
	//			List<InterceptVo> beforeList = new ArrayList<InterceptVo>();
	//			buildInnerInterceptNode(context.evalNodes("before"), beforeList);
	//
	//			// 私有的after
	//			List<InterceptVo> afterList = new ArrayList<InterceptVo>();
	//			buildInnerInterceptNode(context.evalNodes("after"), afterList);
	//
	//			ResponseHandler responseHandler = getResponseHandler(url);
	//
	//			ControllerVo    cVo             = new ControllerVo(url, requestType, transfer, validate, execMethod, getInterceptList(url, assemblyList, InterceptType.ASSEMBLY),
	//					getInterceptList(url, beforeList, InterceptType.BEFORE), getInterceptList(url, afterList, InterceptType.AFTER), permission, cacheUse, convert, cacheInAop,
	//					responseHandler);
	//
	//			this.context.getControllerMap().put(cVo.getUrl(), cVo);
	//			log.info("Add <c> :" + cVo.getUrl());
	//		}
	//	}
}
