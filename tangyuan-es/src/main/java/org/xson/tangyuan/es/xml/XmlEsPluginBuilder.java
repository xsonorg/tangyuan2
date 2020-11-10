package org.xson.tangyuan.es.xml;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.es.EsComponent;
import org.xson.tangyuan.es.EsResultConverter;
import org.xson.tangyuan.es.converters.XCOConverter;
import org.xson.tangyuan.es.xml.node.EsDeleteNode;
import org.xson.tangyuan.es.xml.node.EsForEachNode;
import org.xson.tangyuan.es.xml.node.EsForNode;
import org.xson.tangyuan.es.xml.node.EsGetNode;
import org.xson.tangyuan.es.xml.node.EsPostNode;
import org.xson.tangyuan.es.xml.node.EsPutNode;
import org.xson.tangyuan.es.xml.node.EsTextNode;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.xml.DefaultXmlPluginBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.MixedNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class XmlEsPluginBuilder extends DefaultXmlPluginBuilder {

	private XmlEsContext componentContext = null;

	@Override
	public void setContext(String resource, XmlContext xmlContext) throws Throwable {
		this.componentContext = (XmlEsContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "esservices", false);
		if (this.ns.length() > 0) {
			checkNs(this.ns);
		}
		initNodeHandler();
	}

	@Override
	public void clean() {
		super.clean();
		this.componentContext = null;
	}

	private void initNodeHandler() {
		nodeHandlers.put("foreach", new ForEachHandler());
		nodeHandlers.put("for", new ForHandler());
		nodeHandlers.put("if", new IfHandler());
		nodeHandlers.put("else", new ElseHandler());
		nodeHandlers.put("elseif", new ElseIfHandler());
	}

	public void parseRef() {
	}

	public void parseService() {
		log.info(lang("xml.start.parsing.type", "plugin[service]", this.resource));
		configurationElement();
	}

	private void configurationElement() {
		List<AbstractServiceNode> getList    = buildEsServiceNode(this.root.evalNodes("get"), "get");
		List<AbstractServiceNode> postList   = buildEsServiceNode(this.root.evalNodes("post"), "post");
		List<AbstractServiceNode> putList    = buildEsServiceNode(this.root.evalNodes("put"), "put");
		List<AbstractServiceNode> deleteList = buildEsServiceNode(this.root.evalNodes("delete"), "delete");

		registerService(getList, "get");
		registerService(postList, "post");
		registerService(putList, "put");
		registerService(deleteList, "delete");
	}

	@Override
	protected TangYuanNode getTextNode(String data) {
		return new EsTextNode(data);
	}

	private CacheUseVo parseCacheUse(String cacheUse, String id) {
		if (null == cacheUse) {
			return null;
		}
		return CacheUseVo.parseCacheUse(cacheUse, getFullId(id));
	}

	private CacheCleanVo parseCacheClean(String cacheClean, String id) {
		if (null == cacheClean) {
			return null;
		}
		return CacheCleanVo.parseCacheClean(cacheClean, getFullId(id));
	}

	private String checkReferencedDsKey(String dsKey, String tagName, String id) {
		if (null == dsKey) {
			String defaultDsKey = EsComponent.getInstance().getEsSourceManager().getDefaultEsKey();
			if (null == defaultDsKey) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", "null", id, "dsKey", tagName, this.resource));
			}
			return defaultDsKey;
		}
		if (!EsComponent.getInstance().getEsSourceManager().isValidEsKey(dsKey)) {
			throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", dsKey, id, "dsKey", tagName, this.resource));
		}
		return dsKey;
	}

	private List<AbstractServiceNode> buildEsServiceNode(List<XmlNodeWrapper> contexts, String tagName) {
		//		String                    tagName = "get";
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String   id          = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String   dsKey       = getStringFromAttr(xNode, "esKey");
			String   _cacheUse   = getStringFromAttr(xNode, "cacheUse");
			String   _cacheClean = getStringFromAttr(xNode, "cacheClean");
			String   _converter  = getStringFromAttr(xNode, "converter");
			String   desc        = getStringFromAttr(xNode, "desc");
			String[] groups      = getStringArrayFromAttr(xNode, "group");

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			EsResultConverter converter = getConverter(_converter);
			if (null == converter) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", _converter, "converter", tagName, resource));
			}

			CacheUseVo   cacheUse   = null;
			CacheCleanVo cacheClean = null;
			if (null != _cacheUse) {
				cacheUse = parseCacheUse(_cacheUse, getFullId(id));
			}
			if (null != _cacheClean) {
				cacheClean = parseCacheClean(_cacheClean, getFullId(id));
			}

			TangYuanNode urlNode  = getNode(xNode.evalNodes("url"), tagName + ".url");
			TangYuanNode bodyNode = getNode(xNode.evalNodes("body"), tagName + ".body");

			if (null == urlNode) {
				throw new XmlParseException(lang("xml.tag.miss", tagName + ".url", resource));
			}

			AbstractServiceNode serviceNode = null;

			if ("get".endsWith(tagName)) {
				serviceNode = new EsGetNode(id, ns, getFullId(id), dsKey, urlNode, converter, cacheUse, desc, groups);
			} else if ("post".endsWith(tagName)) {
				if (null == bodyNode) {
					throw new XmlParseException(lang("xml.tag.miss", tagName + ".body", resource));
				}
				serviceNode = new EsPostNode(id, ns, getFullId(id), dsKey, urlNode, bodyNode, converter, cacheUse, cacheClean, desc, groups);
			} else if ("put".endsWith(tagName)) {
				if (null == bodyNode) {
					throw new XmlParseException(lang("xml.tag.miss", tagName + ".body", resource));
				}
				serviceNode = new EsPutNode(id, ns, getFullId(id), dsKey, urlNode, bodyNode, converter, cacheClean, desc, groups);
			} else if ("delete".endsWith(tagName)) {
				serviceNode = new EsDeleteNode(id, ns, getFullId(id), dsKey, urlNode, converter, cacheClean, desc, groups);
			}
			list.add(serviceNode);
		}
		return list;
	}

	private TangYuanNode getNode(List<XmlNodeWrapper> contexts, String tagName) {
		int size = contexts.size();
		if (1 == size) {
			return parseNode(contexts.get(0), false);
		} else if (0 == size) {
			return null;
		}
		throw new XmlParseException(lang("xml.tag.mostone", tagName));
	}

	private EsResultConverter getConverter(String converter) {
		if (null == converter) {
			return this.componentContext.getConverter(XCOConverter.key);
		}
		return this.componentContext.getConverter(converter);
	}

	// NodeHandler

	protected class ForEachHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {

			String tagName    = "foreach";

			String collection = getStringFromAttr(xNode, "collection", lang("xml.tag.attribute.empty", "collection", tagName, resource));
			String index      = parseVariableKey(xNode, "index", tagName);
			String open       = getStringFromAttr(xNode, "open");
			String close      = getStringFromAttr(xNode, "close");
			String separator  = getStringFromAttr(xNode, "separator");

			collection = parseVariableKey(xNode, "collection", tagName);

			List<TangYuanNode> contents = parseDynamicTags(xNode);
			int                size     = contents.size();
			TangYuanNode       sqlNode  = null;
			if (1 == size) {
				sqlNode = contents.get(0);
			} else if (size > 1) {
				sqlNode = new MixedNode(contents);
			} else {
				throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
			}

			ForEachNode forEachNode = new EsForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator);
			targetContents.add(forEachNode);
		}
	}

	protected class ForHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {

			String tagName   = "for";

			String index     = getStringFromAttr(xNode, "index", lang("xml.tag.attribute.empty", "index", tagName, resource));
			String open      = getStringFromAttr(xNode, "open");
			String close     = getStringFromAttr(xNode, "close");
			String separator = getStringFromAttr(xNode, "separator");
			String _start    = getStringFromAttr(xNode, "start");
			String _end      = getStringFromAttr(xNode, "end", lang("xml.tag.attribute.empty", "end", tagName, resource));

			index = parseVariableKey(xNode, "index", tagName);

			Object start = null;
			Object end   = null;

			if (null != _start) {
				if (checkVar(_start)) {
					start = parseVariableUseGA(_start);
				} else {
					start = Integer.parseInt(_start);
				}
			} else {
				start = 0;
			}
			if (checkVar(_end)) {
				end = parseVariableUseGA(_end);
			} else {
				end = Integer.parseInt(_end);
			}

			List<TangYuanNode> contents = parseDynamicTags(xNode);
			int                size     = contents.size();
			TangYuanNode       sqlNode  = null;
			if (1 == size) {
				sqlNode = contents.get(0);
			} else if (size > 1) {
				sqlNode = new MixedNode(contents);
			} else {
				throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
			}

			EsForNode forNode = new EsForNode(sqlNode, index, start, end, open, close, separator);
			targetContents.add(forNode);
		}
	}

	//	private TangYuanNode getURLNode(XmlNodeWrapper context, String xName) {
	//		List<XmlNodeWrapper> contexts = context.evalNodes("url");
	//		if (contexts.size() != 1) {
	//			throw new XmlParseException("In the <" + xName + "> node, the <url> node is missing.");
	//		}
	//		return parseNode(contexts.get(0), false);
	//	}
	//	private TangYuanNode getBodyNode(XmlNodeWrapper context, String xName) {
	//		List<XmlNodeWrapper> contexts = context.evalNodes("body");
	//		if (contexts.size() != 1) {
	//			throw new XmlParseException("In the <" + xName + "> node, the <body> node is missing.");
	//		}
	//		return parseNode(contexts.get(0), false);
	//	}

	//	private List<AbstractServiceNode> buildGetNode(List<XmlNodeWrapper> contexts) {
	//		String                    tagName = "get";
	//		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper xNode : contexts) {
	//
	//			String   id          = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
	//			String   dsKey       = getStringFromAttr(xNode, "esKey");
	//			String   _cacheUse   = getStringFromAttr(xNode, "cacheUse");
	//			String   _cacheClean = getStringFromAttr(xNode, "cacheClean");
	//			String   _converter  = getStringFromAttr(xNode, "converter");
	//			String   desc        = getStringFromAttr(xNode, "desc");
	//			String[] groups      = getStringArrayFromAttr(xNode, "group");
	//
	//			checkServiceRepeated(id, tagName);
	//			dsKey = checkReferencedDsKey(dsKey, tagName, id);
	//
	//			ResultConverter converter = getConverter(_converter);
	//			if (null == converter) {
	//				throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", _converter, "converter", tagName, resource));
	//			}
	//
	//			CacheUseVo   cacheUse   = null;
	//			CacheCleanVo cacheClean = null;
	//			if (null != _cacheUse) {
	//				cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//			}
	//			if (null != _cacheClean) {
	//				cacheClean = parseCacheClean(_cacheClean, getFullId(id));
	//			}
	//
	//			TangYuanNode urlNode  = getNode(xNode.evalNodes("url"), tagName + ".url");
	//			TangYuanNode bodyNode = getNode(xNode.evalNodes("body"), tagName + ".body");
	//
	//			if (null == urlNode) {
	//				throw new XmlParseException(lang("xml.tag.miss", tagName + ".url", resource));
	//			}
	//
	//			AbstractServiceNode serviceNode = null;
	//
	//			if ("get".endsWith(tagName)) {
	//				serviceNode = new EsGetNode(id, ns, getFullId(id), dsKey, urlNode, converter, cacheUse, desc, groups);
	//			} else if ("post".endsWith(tagName)) {
	//				if (null == bodyNode) {
	//					throw new XmlParseException(lang("xml.tag.miss", tagName + ".body", resource));
	//				}
	//				serviceNode = new EsPostNode(id, ns, getFullId(id), dsKey, urlNode, bodyNode, converter, cacheClean, desc, groups);
	//			} else if ("put".endsWith(tagName)) {
	//				if (null == bodyNode) {
	//					throw new XmlParseException(lang("xml.tag.miss", tagName + ".body", resource));
	//				}
	//				serviceNode = new EsPutNode(id, ns, getFullId(id), dsKey, urlNode, bodyNode, converter, cacheClean, desc, groups);
	//			} else if ("delete".endsWith(tagName)) {
	//				serviceNode = new EsDeleteNode(id, ns, getFullId(id), dsKey, urlNode, converter, cacheClean, desc, groups);
	//			}
	//			list.add(serviceNode);
	//		}
	//		return list;
	//	}

	//			if (null != sqlNode) {
	//				//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
	//				//				existingService(id);
	//				//				String dsKey = StringUtils.trim(context.getStringAttribute("esKey"));
	//				//				dsKey = checkDsKey(dsKey, id);
	//				//				String     _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//				//				CacheUseVo cacheUse  = null;
	//				//				if (null != _cacheUse && _cacheUse.length() > 0) {
	//				//					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//				//				}
	//				//				String          _converter    = StringUtils.trim(context.getStringAttribute("converter"));
	//				//				ResultConverter converter     = getConverter(_converter);
	//				EsGetNode selectSetNode = new EsGetNode(id, ns, getFullId(id), dsKey, sqlNode, cacheUse, converter);
	//				list.add(selectSetNode);
	//			}
	//	private List<AbstractServiceNode> buildGetNode(List<XmlNodeWrapper> contexts) {
	//		String                    tagName = "get";
	//		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			// TangYuanNode sqlNode = parseNode(context, false);
	//			TangYuanNode sqlNode = getURLNode(context, "get");
	//			if (null != sqlNode) {
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
	//				existingService(id);
	//
	//				String dsKey = StringUtils.trim(context.getStringAttribute("esKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				String     _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//				CacheUseVo cacheUse  = null;
	//				if (null != _cacheUse && _cacheUse.length() > 0) {
	//					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//				}
	//
	//				String          _converter    = StringUtils.trim(context.getStringAttribute("converter"));
	//				ResultConverter converter     = getConverter(_converter);
	//
	//				EsGetNode       selectSetNode = new EsGetNode(id, ns, getFullId(id), dsKey, sqlNode, cacheUse, converter);
	//				list.add(selectSetNode);
	//			}
	//		}
	//		return list;
	//	}

	//	private List<AbstractServiceNode> buildPostNode(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			TangYuanNode sqlNode  = getURLNode(context, "post");
	//			TangYuanNode bodyNode = getBodyNode(context, "post");
	//			if (null != sqlNode) {
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
	//				existingService(id);
	//
	//				String dsKey = StringUtils.trim(context.getStringAttribute("esKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				String     _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//				CacheUseVo cacheUse  = null;
	//				if (null != _cacheUse && _cacheUse.length() > 0) {
	//					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//				}
	//
	//				String          _converter    = StringUtils.trim(context.getStringAttribute("converter"));
	//				ResultConverter converter     = getConverter(_converter);
	//
	//				EsPostNode      selectSetNode = new EsPostNode(id, ns, getFullId(id), dsKey, sqlNode, bodyNode, cacheUse, converter);
	//				list.add(selectSetNode);
	//			}
	//		}
	//		return list;
	//	}
	//
	//	private List<AbstractServiceNode> buildPutNode(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			TangYuanNode sqlNode  = getURLNode(context, "put");
	//			TangYuanNode bodyNode = getBodyNode(context, "put");
	//			if (null != sqlNode) {
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
	//				existingService(id);
	//
	//				String dsKey = StringUtils.trim(context.getStringAttribute("esKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				String          _converter    = StringUtils.trim(context.getStringAttribute("converter"));
	//				ResultConverter converter     = getConverter(_converter);
	//
	//				EsPutNode       selectSetNode = new EsPutNode(id, ns, getFullId(id), dsKey, sqlNode, bodyNode, converter);
	//				list.add(selectSetNode);
	//			}
	//		}
	//		return list;
	//	}
	//
	//	private List<AbstractServiceNode> buildDeleteNode(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			TangYuanNode sqlNode = getURLNode(context, "delete");
	//			if (null != sqlNode) {
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
	//				existingService(id);
	//
	//				String dsKey = StringUtils.trim(context.getStringAttribute("esKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				String          _converter    = StringUtils.trim(context.getStringAttribute("converter"));
	//				ResultConverter converter     = getConverter(_converter);
	//
	//				EsDeleteNode    selectSetNode = new EsDeleteNode(id, ns, getFullId(id), dsKey, sqlNode, converter);
	//				list.add(selectSetNode);
	//			}
	//		}
	//		return list;
	//	}

	//	private class ForEachHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			String collection = StringUtils.trim(nodeToHandle.getStringAttribute("collection"));
	//			if (!checkVar(collection)) {
	//				throw new XmlParseException("<forEach> collection is not legal, Should be {xxx}");
	//			}
	//			collection = getRealVal(collection);
	//
	//			String index = StringUtils.trim(nodeToHandle.getStringAttribute("index"));
	//			if (null != index) {
	//				if (!checkVar(index)) {
	//					throw new XmlParseException("<forEach> index is not legal, Should be {xxx}");
	//				}
	//				index = getRealVal(index);
	//			}
	//
	//			String             open      = StringUtils.trim(nodeToHandle.getStringAttribute("open"));
	//			String             close     = StringUtils.trim(nodeToHandle.getStringAttribute("close"));
	//			String             separator = StringUtils.trim(nodeToHandle.getStringAttribute("separator"));
	//
	//			List<TangYuanNode> contents  = parseDynamicTags(nodeToHandle);
	//			int                size      = contents.size();
	//			TangYuanNode       sqlNode   = null;
	//			if (1 == size) {
	//				sqlNode = contents.get(0);
	//			} else if (size > 1) {
	//				sqlNode = new MixedNode(contents);
	//			}
	//
	//			// if (null == sqlNode && null == open && null == close && null == separator) {
	//			// open = "(";
	//			// close = ")";
	//			// separator = ",";
	//			// }
	//			// if (null == sqlNode) {
	//			// if (null == index) {
	//			// index = "i";
	//			// }
	//			// sqlNode = new EsTextNode("#{" + collection + "[" + index + "]}");
	//			// }
	//
	//			if (null == sqlNode) {
	//				throw new XmlParseException("<forEach> node missing child nodes.");
	//			}
	//
	//			ForEachNode forEachNode = new EsForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator);
	//			targetContents.add(forEachNode);
	//		}
	//	}

	//	private Map<String, NodeHandler> nodeHandlers = new HashMap<String, NodeHandler>() {
	//		private static final long serialVersionUID = 1L;
	//
	//		{
	//			put("foreach", new ForEachHandler());
	//			put("if", new IfHandler());
	//			put("else", new ElseHandler());
	//			put("elseif", new ElseIfHandler());
	//		}
	//	};

	//	/**
	//	 * 解析: ID:xxx; key:xxx; expiry:10(秒)
	//	 */
	//	private CacheUseVo parseCacheUse(String cacheUse, String service) {
	//		CacheUseVo cacheUseVo = null;
	//		String[]   array      = cacheUse.split(";");
	//		if (array.length > 0) {
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
	//			cacheUseVo = new CacheUseVo(cache, key, expiry, service);
	//		}
	//		return cacheUseVo;
	//	}

	//	private interface NodeHandler {
	//		void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents);
	//	}

	//	private class IfHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			String test = nodeToHandle.getStringAttribute("test");
	//			if (null == test) {
	//				throw new XmlParseException("<if> node test == null");
	//			}
	//			List<TangYuanNode> contents = parseDynamicTags(nodeToHandle);
	//			int                size     = contents.size();
	//			IfNode             ifNode   = null;
	//			if (1 == size) {
	//				ifNode = new IfNode(contents.get(0), new LogicalExprParser().parse(test));
	//			} else if (size > 1) {
	//				ifNode = new IfNode(new MixedNode(contents), new LogicalExprParser().parse(test));
	//			} else { // size == 0
	//				throw new XmlParseException("<if> node contents == null");
	//			}
	//			targetContents.add(ifNode);
	//		}
	//	}

	//	private class ElseIfHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			if (0 == targetContents.size()) {
	//				throw new XmlParseException("<elseIf> node is not legal.");
	//			}
	//			TangYuanNode previousNode = targetContents.get(targetContents.size() - 1);
	//			if (!(previousNode instanceof IfNode)) {
	//				throw new XmlParseException("The node before the <elseIf> node must be an <if> node.");
	//			}
	//			String test = nodeToHandle.getStringAttribute("test");
	//			if (null == test) {
	//				throw new XmlParseException("<elseIf> node test == null");
	//			}
	//
	//			List<TangYuanNode> contents = parseDynamicTags(nodeToHandle);
	//			int                size     = contents.size();
	//
	//			IfNode             ifNode   = null;
	//			if (1 == size) {
	//				ifNode = new IfNode(contents.get(0), new LogicalExprParser().parse(test));
	//			} else if (size > 1) {
	//				ifNode = new IfNode(new MixedNode(contents), new LogicalExprParser().parse(test));
	//			} else {
	//				throw new XmlParseException("<elseIf> node contents == null");
	//			}
	//			((IfNode) previousNode).addElseIfNode(ifNode);
	//		}
	//	}

	//	private class ElseHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			if (0 == targetContents.size()) {
	//				throw new XmlParseException("<else> node is not legal.");
	//			}
	//			TangYuanNode previousNode = targetContents.get(targetContents.size() - 1);
	//			if (!(previousNode instanceof IfNode)) {
	//				throw new XmlParseException("<else> node is not legal.");
	//			}
	//			List<TangYuanNode> contents = parseDynamicTags(nodeToHandle);
	//			int                size     = contents.size();
	//			IfNode             ifNode   = null;
	//			if (1 == size) {
	//				ifNode = new IfNode(contents.get(0), null);
	//			} else if (size > 1) {
	//				ifNode = new IfNode(new MixedNode(contents), null);
	//			} else {
	//				throw new XmlParseException("<else> node contents == null");
	//			}
	//			((IfNode) previousNode).addElseNode(ifNode);
	//		}
	//	}

	//	private Log				log		= LogFactory.getLog(getClass());
	//	private XmlNodeWrapper	root	= null;
	//	private XmlEsContext context = null;
	//	@Override
	//	public Log getLog() {
	//		return this.log;
	//	}
	//	@Override
	//	public void setContext(XmlNodeWrapper root, XmlContext context) {
	//		this.context = (XmlEsContext) context;
	//		this.root = root;
	//		this.ns = this.root.getStringAttribute("ns", "");
	//		if (this.ns.length() > 0) {
	//			this.context.getXmlContext().checkNs(this.ns);
	//		}
	//	}
	//	protected String getFullId(String id) {
	//	return TangYuanUtil.getQualifiedName(this.ns, id, null, TangYuanContainer.getInstance().getNsSeparator());
	//}
	//	private TangYuanNode parseNode(XmlNodeWrapper context, boolean internal) {
	//		List<TangYuanNode> contents = parseDynamicTags(context);
	//		int                size     = contents.size();
	//		TangYuanNode       sqlNode  = null;
	//		if (size == 1) {
	//			sqlNode = contents.get(0);
	//		} else if (size > 1) {
	//			sqlNode = new MixedNode(contents);
	//		} else {
	//			log.warn("节点内容为空, 将被忽略:" + context.getName());
	//		}
	//		return sqlNode;
	//	}

	//	protected List<TangYuanNode> parseDynamicTags(XmlNodeWrapper node) {
	//		List<TangYuanNode> contents = new ArrayList<TangYuanNode>();
	//		NodeList           children = node.getNode().getChildNodes();
	//		for (int i = 0; i < children.getLength(); i++) {
	//			XmlNodeWrapper child = node.newXMlNode(children.item(i));
	//			if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
	//				String data = child.getStringBody("");
	//				if (StringUtils.isEmptySafe(data)) {
	//					continue;
	//				}
	//				contents.add(new EsTextNode(data));
	//			} else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) {
	//				String      nodeName = child.getNode().getNodeName();
	//				NodeHandler handler  = nodeHandlers.get(nodeName);
	//				if (handler == null) {
	//					throw new XmlParseException("Unknown element <" + nodeName + "> in SQL statement.");
	//				}
	//				handler.handleNode(child, contents);
	//			}
	//		}
	//		return contents;
	//	}

	//	private String checkDsKey(String dsKey, String service) {
	//		if (null == dsKey) {
	//			dsKey = EsSourceManager.getDefaultEsKey();
	//			if (null == dsKey) {
	//				throw new XmlParseException("service[" + service + "] uses an invalid esKey: " + dsKey);
	//			}
	//		} else {
	//			if (!EsSourceManager.isValidEsKey(dsKey)) {
	//				throw new XmlParseException("service[" + service + "] uses an invalid esKey: " + dsKey);
	//			}
	//		}
	//		return dsKey;
	//	}
	//	private void existingService(String id) {
	//		if (null == id || 0 == id.length()) {
	//			throw new XmlParseException("Service ID can not be empty.");
	//		}
	//		String fullId = getFullId(id);
	//		if (null != this.context.getXmlContext().getIntegralServiceMap().get(fullId)) {
	//			throw new XmlParseException("Duplicate service nodes: " + fullId);
	//		}
	//		if (null != this.context.getXmlContext().getIntegralRefMap().get(fullId)) {
	//			throw new XmlParseException("Duplicate service nodes: " + fullId);
	//		}
	//		this.context.getXmlContext().getIntegralServiceMap().put(fullId, 1);
	//	}
}
