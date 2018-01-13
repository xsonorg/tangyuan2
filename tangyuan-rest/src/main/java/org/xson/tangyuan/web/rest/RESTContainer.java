package org.xson.tangyuan.web.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;
import org.xson.tangyuan.web.util.RestUtil;
import org.xson.tangyuan.web.util.ServletUtils;
import org.xson.tangyuan.web.xml.XMLWebContext;
import org.xson.tangyuan.web.xml.vo.ControllerVo;
import org.xson.tangyuan.web.xml.vo.RESTControllerVo;
import org.xson.tangyuan.xml.XmlParseException;

public class RESTContainer {

	private Log							log				= LogFactory.getLog(getClass());

	/** 静态URI,包括GET, POST ... */
	private Map<String, ControllerVo>	staticMap		= new HashMap<String, ControllerVo>();

	private URINodeTree					getURITree		= new URINodeTree("/");
	private URINodeTree					postURITree		= new URINodeTree("/");
	private URINodeTree					putURITree		= new URINodeTree("/");
	private URINodeTree					deleteURITree	= new URINodeTree("/");
	private URINodeTree					headURITree		= new URINodeTree("/");
	private URINodeTree					optionsURITree	= new URINodeTree("/");

	public ControllerVo getControllerVo(RequestTypeEnum requestType, String path) {

		ControllerVo cVo = staticMap.get(RestUtil.getRestKey(requestType, path));
		if (null != cVo) {
			return cVo;
		}

		Object controller = null;

		// 确认url格式规范
		// String rPath = url;
		// if (rPath.length() > 1 && rPath.startsWith(RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR)) {
		// rPath = rPath.substring(1);
		// }
		// List<String> targetList = RestUtil.splitToStringList(rPath, RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR);

		List<String> targetList = ServletUtils.parseURIPathItem(path);

		if (RequestTypeEnum.GET == requestType) {
			controller = getURITree.get(targetList);
		} else if (RequestTypeEnum.POST == requestType) {
			controller = postURITree.get(targetList);
		} else if (RequestTypeEnum.PUT == requestType) {
			controller = putURITree.get(targetList);
		} else if (RequestTypeEnum.DELETE == requestType) {
			controller = deleteURITree.get(targetList);
		} else if (RequestTypeEnum.HEAD == requestType) {
			controller = headURITree.get(targetList);
		} else if (RequestTypeEnum.OPTIONS == requestType) {
			controller = optionsURITree.get(targetList);
		}

		return (ControllerVo) controller;
	}

	public void build(XMLWebContext context) {
		List<RESTControllerVo> restControllerList = context.getRestControllerList();
		if (0 == restControllerList.size()) {
			return;
		}

		long start = System.currentTimeMillis();

		// static
		List<String> detectedGetList = new ArrayList<String>();
		List<String> detectedPostList = new ArrayList<String>();
		List<String> detectedPutList = new ArrayList<String>();
		List<String> detectedDeleteList = new ArrayList<String>();
		List<String> detectedHeadList = new ArrayList<String>();
		List<String> detectedOptionsList = new ArrayList<String>();

		// dynamic
		List<String> detectedDynamicGetList = new ArrayList<String>();
		List<String> detectedDynamicPostList = new ArrayList<String>();
		List<String> detectedDynamicPutList = new ArrayList<String>();
		List<String> detectedDynamicDeleteList = new ArrayList<String>();
		List<String> detectedDynamicHeadList = new ArrayList<String>();
		List<String> detectedDynamicOptionsList = new ArrayList<String>();

		for (RESTControllerVo cVo : restControllerList) {
			RestURIVo uriVo = cVo.getRestURIVo();
			RequestTypeEnum requestType = cVo.getRequestType();
			String path = uriVo.getPath();
			if (uriVo.isStaticURI()) {
				staticMap.put(RestUtil.getRestKey(requestType, path), cVo);
				if (RequestTypeEnum.GET == requestType) {
					detectedGetList.add(path);
				} else if (RequestTypeEnum.POST == requestType) {
					detectedPostList.add(path);
				} else if (RequestTypeEnum.PUT == requestType) {
					detectedPutList.add(path);
				} else if (RequestTypeEnum.DELETE == requestType) {
					detectedDeleteList.add(path);
				} else if (RequestTypeEnum.HEAD == requestType) {
					detectedHeadList.add(path);
				} else if (RequestTypeEnum.OPTIONS == requestType) {
					detectedOptionsList.add(path);
				}
			} else {
				if (RequestTypeEnum.GET == requestType) {
					getURITree.build(uriVo, cVo);
					detectedDynamicGetList.add(uriVo.getPath());
				} else if (RequestTypeEnum.POST == requestType) {
					postURITree.build(uriVo, cVo);
					detectedDynamicPostList.add(uriVo.getPath());
				} else if (RequestTypeEnum.PUT == requestType) {
					putURITree.build(uriVo, cVo);
					detectedDynamicPutList.add(uriVo.getPath());
				} else if (RequestTypeEnum.DELETE == requestType) {
					deleteURITree.build(uriVo, cVo);
					detectedDynamicDeleteList.add(uriVo.getPath());
				} else if (RequestTypeEnum.HEAD == requestType) {
					headURITree.build(uriVo, cVo);
					detectedDynamicHeadList.add(uriVo.getPath());
				} else if (RequestTypeEnum.OPTIONS == requestType) {
					optionsURITree.build(uriVo, cVo);
					detectedDynamicOptionsList.add(uriVo.getPath());
				}
			}

			// add ControllerMap
			context.getControllerMap().put(RestUtil.getRestKey(requestType, path), cVo);
		}

		// 用固定的URI去匹配模糊的URI,检测不明确的控制器
		checkStaticUndefinedMatch(detectedGetList, getURITree, RequestTypeEnum.GET);
		checkStaticUndefinedMatch(detectedPostList, postURITree, RequestTypeEnum.POST);
		checkStaticUndefinedMatch(detectedPutList, putURITree, RequestTypeEnum.PUT);
		checkStaticUndefinedMatch(detectedDeleteList, deleteURITree, RequestTypeEnum.DELETE);
		checkStaticUndefinedMatch(detectedHeadList, headURITree, RequestTypeEnum.HEAD);
		checkStaticUndefinedMatch(detectedOptionsList, optionsURITree, RequestTypeEnum.OPTIONS);

		// 二次REST模糊检测，只检测动态的
		checkDynamicUndefinedMatch(detectedDynamicGetList, getURITree, RequestTypeEnum.GET);
		checkDynamicUndefinedMatch(detectedDynamicPostList, postURITree, RequestTypeEnum.POST);
		checkDynamicUndefinedMatch(detectedDynamicPutList, putURITree, RequestTypeEnum.PUT);
		checkDynamicUndefinedMatch(detectedDynamicDeleteList, deleteURITree, RequestTypeEnum.DELETE);
		checkDynamicUndefinedMatch(detectedDynamicHeadList, headURITree, RequestTypeEnum.HEAD);
		checkDynamicUndefinedMatch(detectedDynamicOptionsList, optionsURITree, RequestTypeEnum.OPTIONS);

		long end = System.currentTimeMillis();
		log.info("rest uri build successfully. [" + (end - start) + "s]");
	}

	private void checkStaticUndefinedMatch(List<String> detectedList, URINodeTree tree, RequestTypeEnum requestType) {
		Object controller = null;
		if (detectedList.size() > 0) {
			for (String target : detectedList) {
				controller = getURITree.get(ServletUtils.parseURIPathItem(target));
				if (null != controller) {
					throw new XmlParseException("Ambiguous controllers [" + requestType + " " + target + "] and [" + requestType + " "
							+ ((ControllerVo) controller).getUrl() + "].");
				}
			}
		}
	}

	private void checkDynamicUndefinedMatch(List<String> detectedList, URINodeTree tree, RequestTypeEnum requestType) {
		if (detectedList.size() > 0) {
			for (String target : detectedList) {
				getURITree.getAndCheck(ServletUtils.parseURIPathItem(target));
			}
		}
	}
}
