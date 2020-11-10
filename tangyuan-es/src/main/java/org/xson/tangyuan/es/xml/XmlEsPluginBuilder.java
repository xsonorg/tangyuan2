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
		List<AbstractServiceNode> getList = buildEsServiceNode(this.root.evalNodes("get"), "get");
		List<AbstractServiceNode> postList = buildEsServiceNode(this.root.evalNodes("post"), "post");
		List<AbstractServiceNode> putList = buildEsServiceNode(this.root.evalNodes("put"), "put");
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
		// String tagName = "get";
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String dsKey = getStringFromAttr(xNode, "esKey");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");
			String _converter = getStringFromAttr(xNode, "converter");
			String desc = getStringFromAttr(xNode, "desc");
			String[] groups = getStringArrayFromAttr(xNode, "group");

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			EsResultConverter converter = getConverter(_converter);
			if (null == converter) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", _converter, "converter", tagName, resource));
			}

			CacheUseVo cacheUse = null;
			CacheCleanVo cacheClean = null;
			if (null != _cacheUse) {
				cacheUse = parseCacheUse(_cacheUse, getFullId(id));
			}
			if (null != _cacheClean) {
				cacheClean = parseCacheClean(_cacheClean, getFullId(id));
			}

			TangYuanNode urlNode = getNode(xNode.evalNodes("url"), tagName + ".url");
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

			String tagName = "foreach";

			String collection = getStringFromAttr(xNode, "collection", lang("xml.tag.attribute.empty", "collection", tagName, resource));
			String index = parseVariableKey(xNode, "index", tagName);
			String open = getStringFromAttr(xNode, "open");
			String close = getStringFromAttr(xNode, "close");
			String separator = getStringFromAttr(xNode, "separator");

			collection = parseVariableKey(xNode, "collection", tagName);

			List<TangYuanNode> contents = parseDynamicTags(xNode);
			int size = contents.size();
			TangYuanNode sqlNode = null;
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

			String tagName = "for";

			String index = getStringFromAttr(xNode, "index", lang("xml.tag.attribute.empty", "index", tagName, resource));
			String open = getStringFromAttr(xNode, "open");
			String close = getStringFromAttr(xNode, "close");
			String separator = getStringFromAttr(xNode, "separator");
			String _start = getStringFromAttr(xNode, "start");
			String _end = getStringFromAttr(xNode, "end", lang("xml.tag.attribute.empty", "end", tagName, resource));

			index = parseVariableKey(xNode, "index", tagName);

			Object start = null;
			Object end = null;

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
			int size = contents.size();
			TangYuanNode sqlNode = null;
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

}
