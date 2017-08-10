package org.xson.tangyuan.web.xml;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.modelmbean.XMLParseException;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.cache.CacheComponent;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.WebComponent;
import org.xson.tangyuan.web.xml.ControllerVo.DataConvertEnum;
import org.xson.tangyuan.web.xml.InterceptVo.InterceptType;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XMLPluginBuilder extends ControllerBuilder {

	private Log				log		= LogFactory.getLog(getClass());
	private XPathParser		parser	= null;
	private XmlNodeWrapper	root	= null;

	// private BuilderContext bc = null;
	// 引用标志
	// private String refMark = "@";
	// private String urlSeparator = "/";
	// private String leftBrackets = "{";
	// private String rightBrackets = "}";

	// public XMLPluginBuilder(BuilderContext bc) {
	// this.bc = bc;
	// }

	public XMLPluginBuilder(InputStream inputStream, BuilderContext bc) {
		this.bc = bc;
		this.parser = new XPathParser(inputStream);
		this.root = this.parser.evalNode("/web-controller");
	}

	// public void parseBeanNode() throws Throwable {
	// buildBeanNode(this.root.evalNodes("bean"));
	// }
	// public void parseInterceptNode() throws Throwable {
	// buildInterceptNode(this.root.evalNodes("assembly"), InterceptType.ASSEMBLY);
	// buildInterceptNode(this.root.evalNodes("before"), InterceptType.BEFORE);
	// buildInterceptNode(this.root.evalNodes("after"), InterceptType.AFTER);
	// }
	// public void parseControllerNodeAutoMapping() {
	// try {
	// buildControllerNodeAutoMapping();
	// } catch (Exception e) {
	// throw new RuntimeException(e);
	// }
	// }

	public void parseRef() throws Throwable {
		buildBeanNode(this.root.evalNodes("bean"));
		buildInterceptNode(this.root.evalNodes("assembly"), InterceptType.ASSEMBLY);
		buildInterceptNode(this.root.evalNodes("before"), InterceptType.BEFORE);
		buildInterceptNode(this.root.evalNodes("after"), InterceptType.AFTER);
	}

	public void parseControllerNode() throws Throwable {
		buildControllerNode(this.root.evalNodes("c"));
	}

	private void buildBeanNode(List<XmlNodeWrapper> contexts) throws Exception {
		// <bean id="" class="" />
		for (XmlNodeWrapper context : contexts) {
			String id = StringUtils.trim(context.getStringAttribute("id"));
			String clazz = StringUtils.trim(context.getStringAttribute("class"));
			if (this.bc.getBeanIdMap().containsKey(id)) {
				throw new XmlParseException("Duplicate Bean: " + id);
			}
			Object obj = this.bc.getBeanClassMap().get(clazz);
			if (null == obj) {
				obj = Class.forName(clazz).newInstance();
				this.bc.getBeanClassMap().put(clazz, obj);
			}
			this.bc.getBeanIdMap().put(id, obj);
			log.info("Add <bean> :" + clazz);
		}
	}

	private void buildInterceptNode(List<XmlNodeWrapper> contexts, InterceptType type) throws Exception {
		for (XmlNodeWrapper context : contexts) {

			String call = StringUtils.trim(context.getStringAttribute("call"));
			String _order = StringUtils.trim(context.getStringAttribute("order"));

			if (InterceptType.ASSEMBLY == type) {
				if (this.bc.getBeforeMap().containsKey(call)) {
					throw new RuntimeException("Duplicate Assembly: " + call);
				}
			} else if (InterceptType.BEFORE == type) {
				if (this.bc.getBeforeMap().containsKey(call)) {
					throw new RuntimeException("Duplicate Before: " + call);
				}
			} else {
				if (this.bc.getAfterMap().containsKey(call)) {
					throw new RuntimeException("Duplicate After: " + call);
				}
			}

			MethodObject mo = getMethodObject(call);

			int order = WebComponent.getInstance().getOrder();
			if (null != _order) {
				order = Integer.parseInt(_order);
			}

			List<String> includeList = new ArrayList<String>();
			List<String> excludeList = new ArrayList<String>();

			// <include></include>
			List<XmlNodeWrapper> includeNodes = context.evalNodes("include");
			for (XmlNodeWrapper include : includeNodes) {
				String body = StringUtils.trim(include.getStringBody());
				if (null != body) {
					includeList.add(body);
				}
			}
			if (includeList.size() == 0) {
				includeList = null;
			}

			// <exclude></exclude>
			List<XmlNodeWrapper> excludeNodes = context.evalNodes("exclude");
			for (XmlNodeWrapper exclude : excludeNodes) {
				String body = StringUtils.trim(exclude.getStringBody());
				if (null != body) {
					excludeList.add(body);
				}
			}
			if (excludeList.size() == 0) {
				excludeList = null;
			}

			if (null == includeList && null == excludeList) {
				throw new RuntimeException("Intercept missing <include|exclude>: " + call);
			}

			InterceptVo baVo = new InterceptVo(mo, order, includeList, excludeList);

			if (InterceptType.ASSEMBLY == type) {
				this.bc.getAssemblyList().add(baVo);
				this.bc.getAssemblyMap().put(call, call);
			} else if (InterceptType.BEFORE == type) {
				this.bc.getBeforeList().add(baVo);
				this.bc.getBeforeMap().put(call, call);
			} else {
				this.bc.getAfterList().add(baVo);
				this.bc.getAfterMap().put(call, call);
			}

		}
	}

	private void buildControllerNode(List<XmlNodeWrapper> contexts) throws Exception {
		for (XmlNodeWrapper context : contexts) {
			// 基于安全考虑, 不能支持*,
			String url = StringUtils.trim(context.getStringAttribute("url"));
			if (this.bc.getControllerMap().containsKey(url)) {
				throw new RuntimeException("Duplicate URL: " + url);
			}
			String transfer = StringUtils.trim(context.getStringAttribute("transfer"));
			// if (null != transfer) {
			// transfer = parseTransfer(transfer, url);
			// }

			String validate = StringUtils.trim(context.getStringAttribute("validate"));
			if (null != validate) {
				validate = parseValidate(validate, url);
			}

			String exec = StringUtils.trim(context.getStringAttribute("exec"));
			MethodObject execMethod = null;
			if (null != exec) {
				execMethod = getMethodObject(exec);
			}

			if (null == transfer && null == execMethod) {
				// throw new XmlParseException("transfer and exec can not be empty, url: " + url);
				transfer = url;
			}
			if (null != transfer) {
				transfer = parseTransfer(transfer, url);
				transfer = urlToServiceName(transfer);
			}

			// 权限
			String permission = StringUtils.trim(context.getStringAttribute("permission"));

			// 缓存
			// String _cacheUse = StringUtils.trim(context.getStringAttribute("cache"));
			String _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));

			CacheUseVo cacheUse = null;
			if (null != _cacheUse && _cacheUse.length() > 0) {
				cacheUse = parseCacheUse(_cacheUse, url);
			}

			// DataConvertEnum convert = DataConvertEnum.BODY;
			// String _convert = StringUtils.trim(context.getStringAttribute("convert"));
			// if (null != _convert) {
			// convert = getDataConvert(_convert);
			// }
			// if (DataConvertEnum.RULE == convert && null == validate) {
			// throw new XMLParseException("The controller is missing the validate ID.");
			// }

			DataConvertEnum convert = null;
			String _convert = StringUtils.trim(context.getStringAttribute("convert"));
			if (null != _convert) {
				convert = getDataConvert(_convert);
			}
			if (DataConvertEnum.KV_RULE_XCO == convert && null == validate) {
				throw new XMLParseException("Use data validation for data conversion, missing data validation ID. url: " + url);
			}

			// boolean convertByRule = false;
			// String _convertByRule = StringUtils.trim(context.getStringAttribute("convertByRule"));
			// if (null != _convertByRule) {
			// convertByRule = Boolean.parseBoolean(_convertByRule);
			// }
			// if (convertByRule && null == validate) {
			// throw new XMLParseException("Use data validation for data conversion, missing data validation ID. url: " + url);
			// }

			boolean cacheInAop = WebComponent.getInstance().isCacheInAop();
			String _cacheInAop = StringUtils.trim(context.getStringAttribute("cacheInAop"));
			if (null != _cacheInAop) {
				cacheInAop = Boolean.parseBoolean(_cacheInAop);
			}

			// 私有的assembly
			List<InterceptVo> assemblyList = new ArrayList<InterceptVo>();
			List<XmlNodeWrapper> assemblyNodes = context.evalNodes("assembly");
			for (XmlNodeWrapper node : assemblyNodes) {
				String call = StringUtils.trim(node.getStringAttribute("call"));
				String _order = StringUtils.trim(node.getStringAttribute("order"));
				MethodObject mo = getMethodObject(call);
				int order = WebComponent.getInstance().getOrder();
				if (null != _order) {
					order = Integer.parseInt(_order);
				}
				InterceptVo baVo = new InterceptVo(mo, order);
				assemblyList.add(baVo);
			}

			// 私有的before
			List<InterceptVo> beforeList = new ArrayList<InterceptVo>();
			List<XmlNodeWrapper> beforeNodes = context.evalNodes("before");
			for (XmlNodeWrapper node : beforeNodes) {
				String call = StringUtils.trim(node.getStringAttribute("call"));
				String _order = StringUtils.trim(node.getStringAttribute("order"));
				MethodObject mo = getMethodObject(call);
				int order = WebComponent.getInstance().getOrder();
				if (null != _order) {
					order = Integer.parseInt(_order);
				}
				InterceptVo baVo = new InterceptVo(mo, order);
				beforeList.add(baVo);
			}

			// 私有的after
			List<InterceptVo> afterList = new ArrayList<InterceptVo>();
			List<XmlNodeWrapper> afterNodes = context.evalNodes("after");
			for (XmlNodeWrapper node : afterNodes) {
				String call = StringUtils.trim(node.getStringAttribute("call"));
				String _order = StringUtils.trim(node.getStringAttribute("order"));
				MethodObject mo = getMethodObject(call);
				int order = WebComponent.getInstance().getOrder();
				if (null != _order) {
					order = Integer.parseInt(_order);
				}
				InterceptVo baVo = new InterceptVo(mo, order);
				afterList.add(baVo);
			}

			ControllerVo cVo = new ControllerVo(url, transfer, validate, execMethod, getInterceptList(url, assemblyList, InterceptType.ASSEMBLY),
					getInterceptList(url, beforeList, InterceptType.BEFORE), getInterceptList(url, afterList, InterceptType.AFTER), permission,
					cacheUse, convert, cacheInAop);

			this.bc.getControllerMap().put(cVo.getUrl(), cVo);
			log.info("Add <c> :" + cVo.getUrl());
		}
	}

	private MethodObject getMethodObject(String str) throws Exception {
		MethodObject mo = null;
		// a.b
		// if (str.startsWith(refMark)) {
		if (str.startsWith(leftBrackets)) {
			String[] array = str.split("\\.");
			if (array.length != 2) {
				throw new RuntimeException("Invalid bean reference: " + str);
			}
			// String beanId = array[0].substring(1);
			String beanId = array[0].substring(1, array[0].length() - 1);
			String methodName = array[1];

			Object bean = this.bc.getBeanIdMap().get(beanId);
			if (null == bean) {
				throw new XmlParseException("Reference bean does not exist: " + beanId);
			}
			String moKey = bean.getClass().getName() + "." + methodName;
			mo = this.bc.getMoMap().get(moKey);
			if (null == mo) {
				Method method = bean.getClass().getMethod(methodName, RequestContext.class);
				// 如果方法不存在或者参数类型不匹配，这里会抛出异常
				method.setAccessible(true);
				mo = new MethodObject(method, bean);
				this.bc.getMoMap().put(moKey, mo);
			}
		} else {
			mo = this.bc.getMoMap().get(str);
			if (null != mo) {
				return mo;
			}
			int pos = str.lastIndexOf(".");
			String className = str.substring(1, pos);
			String methodName = str.substring(pos + 1, str.length());

			Object instance = this.bc.getBeanClassMap().get(className);
			Class<?> clazz = null;
			if (null == instance) {
				clazz = Class.forName(className);
				instance = clazz.newInstance();
				this.bc.getBeanClassMap().put(className, instance);
			} else {
				clazz = instance.getClass();
			}
			Method method = clazz.getMethod(methodName, RequestContext.class);
			// 如果方法不存在或者参数类型不匹配，这里会抛出异常
			method.setAccessible(true);
			mo = new MethodObject(method, instance);
			this.bc.getMoMap().put(str, mo);
		}
		return mo;
	}

	/**
	 * URL合并
	 */
	private String urlMerge(String domain, String path) {
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
	private String parseTransfer(String transfer, String url) {
		if (refMark.equals(transfer)) {
			return url;
		}
		// 第一种情况: {xxx}/@
		String regex = "\\{.*\\}/@";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(transfer);
		if (matcher.matches()) {
			int pos = transfer.indexOf(rightBrackets);
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

	private String parseValidate(String validate, String url) {
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

	// private List<MethodObject> getInterceptList(String url, List<InterceptVo> list, InterceptType type) {
	// List<MethodObject> result = null;
	// List<InterceptVo> matchList = new ArrayList<InterceptVo>();
	// // 全局的
	// List<InterceptVo> globalList = null;
	//
	// if (InterceptType.ASSEMBLY == type) {
	// globalList = this.bc.getAssemblyList();
	// } else if (InterceptType.BEFORE == type) {
	// globalList = this.bc.getBeforeList();
	// } else {
	// globalList = this.bc.getAfterList();
	// }
	//
	// for (InterceptVo baVo : globalList) {
	// if (baVo.match(url)) {
	// matchList.add(baVo);
	// }
	// }
	// if (list.size() > 0) {
	// matchList.addAll(list);
	// }
	//
	// if (matchList.size() > 0) {
	// Collections.sort(matchList);// sort
	// result = new ArrayList<MethodObject>();
	// for (InterceptVo baVo : matchList) {
	// result.add(baVo.getMo());
	// }
	// }
	//
	// return result;
	// }

	/**
	 * 解析: ID:xxx; key:xxx; time:1000; ignore:a,b<br />
	 * 最短cache="@"
	 * 
	 */
	private CacheUseVo parseCacheUse(String cacheUse, String url) {
		CacheUseVo cacheUseVo = null;
		String[] array = cacheUse.split(";");
		if (array.length > 0) {
			// 特殊情况
			if (cacheUse.equals(refMark)) {
				TangYuanCache cache = CacheComponent.getInstance().getCache(null);
				if (null == cache) {
					throw new XmlParseException("Non-existent cache: " + cacheUse);
				}
				cacheUseVo = new CacheUseVo(cache, url, null, url);
			}

			Map<String, String> map = new HashMap<String, String>();
			for (int i = 0; i < array.length; i++) {
				String[] item = array[i].split(":");
				map.put(item[0].trim().toUpperCase(), item[1].trim());
			}
			TangYuanCache cache = CacheComponent.getInstance().getCache(map.get("id".toUpperCase()));
			if (null == cache) {
				throw new XmlParseException("Non-existent cache: " + cacheUse);
			}
			String key = map.get("key".toUpperCase());
			if (null == key) {
				throw new XmlParseException("Missing cache.key: " + cacheUse);
			}
			Long expiry = null;
			if (map.containsKey("expiry".toUpperCase())) {
				expiry = Long.parseLong(map.get("expiry".toUpperCase()));
			}
			cacheUseVo = new CacheUseVo(cache, key, expiry, url);
		}
		return cacheUseVo;
	}

	/** URL到本地服务名 */
	private String urlToServiceName(String url) {
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

	// private String serviceNameToUrl(String serviceName) {
	// if (!serviceName.startsWith(urlSeparator)) {
	// serviceName = urlSeparator + serviceName;
	// }
	// return serviceName.replaceAll("\\.", "/");
	// }

	// private DataConvertEnum getDataConvert(String type) {
	// if ("BODY".equalsIgnoreCase(type)) {
	// return DataConvertEnum.BODY;
	// } else if ("KV".equalsIgnoreCase(type)) {
	// return DataConvertEnum.KV;
	// } else if ("RULE".equalsIgnoreCase(type)) {
	// return DataConvertEnum.RULE;
	// }
	// return null;
	// }

	private DataConvertEnum getDataConvert(String type) {
		if ("KV_XCO".equalsIgnoreCase(type)) {
			return DataConvertEnum.KV_XCO;
		} else if ("KV_RULE_XCO".equalsIgnoreCase(type)) {
			return DataConvertEnum.KV_RULE_XCO;
		}
		return null;
	}

}
