package org.xson.tangyuan.hive.xml;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.hive.HiveComponent;
import org.xson.tangyuan.hive.transaction.XTransactionDefinition;
import org.xson.tangyuan.hive.xml.node.HiveForEachNode;
import org.xson.tangyuan.hive.xml.node.HiveForNode;
import org.xson.tangyuan.hive.xml.node.HiveTextNode;
import org.xson.tangyuan.hive.xml.node.HqlNode;
import org.xson.tangyuan.hive.xml.node.InternalHqlNode;
import org.xson.tangyuan.hive.xml.node.InternalSelectOneNode;
import org.xson.tangyuan.hive.xml.node.InternalSelectSetNode;
import org.xson.tangyuan.hive.xml.node.InternalSelectVarNode;
import org.xson.tangyuan.hive.xml.node.SelectOneNode;
import org.xson.tangyuan.hive.xml.node.SelectSetNode;
import org.xson.tangyuan.hive.xml.node.SelectVarNode;
import org.xson.tangyuan.hive.xml.node.ServiceNode;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.xml.DefaultXmlPluginBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.SegmentNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class XmlHivePluginBuilder extends DefaultXmlPluginBuilder {

	private XmlHiveContext componentContext    = null;
	private String         idWithSqlService    = null;
	private String         dsKeyWithSqlService = null;
	private Class<?>       serviceResultType   = null;

	@Override
	public void setContext(String resource, XmlContext xmlContext) throws Throwable {
		this.componentContext = (XmlHiveContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "hiveservices", false);
		if (this.ns.length() > 0) {
			checkNs(this.ns);
		}
		initNodeHandler();
	}

	@Override
	public void clean() {
		super.clean();

		this.idWithSqlService = null;
		this.dsKeyWithSqlService = null;
		this.serviceResultType = null;
		this.componentContext = null;
	}

	@Override
	public void parseRef() {
		log.info(lang("xml.start.parsing.type", "plugin[ref]", this.resource));
		// 解析sql节点
		buildSqlNode(this.root.evalNodes("sql"));
		// 解析segment节点, 增加段定义和引用
		buildSegmentNode(this.root.evalNodes("segment"));
	}

	@Override
	public void parseService() {
		log.info(lang("xml.start.parsing.type", "plugin[service]", this.resource));
		configurationElement();
	}

	private void initNodeHandler() {
		nodeHandlers.put("foreach", new ForEachHandler());
		nodeHandlers.put("for", new ForHandler());
		nodeHandlers.put("if", new IfHandler());
		nodeHandlers.put("else", new ElseHandler());
		nodeHandlers.put("elseif", new ElseIfHandler());
		nodeHandlers.put("include", new IncludeHandler());
		nodeHandlers.put("exception", new ThrowHandler());
		nodeHandlers.put("return", new ReturnHandler());
		nodeHandlers.put("setvar", new SetVarHandler());
		nodeHandlers.put("log", new LogHandler());
		nodeHandlers.put("selectSet", new SelectSetHandler());
		nodeHandlers.put("selectOne", new SelectOneHandler());
		nodeHandlers.put("selectVar", new SelectVarHandler());
		nodeHandlers.put("hql", new HqlHandler());
		nodeHandlers.put("call", new CallHandler());
	}

	private String checkReferencedDsKey(String dsKey, String tagName, String id) {
		if (null == dsKey) {
			String defaultDsKey = HiveComponent.getInstance().getDataSourceManager().getDefaultDsKey();
			if (null == defaultDsKey) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", "null", id, "dsKey", tagName, this.resource));
			}
			return defaultDsKey;
		}
		if (!HiveComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
			throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", dsKey, id, "dsKey", tagName, this.resource));
		}
		return dsKey;
	}

	private void checkInnerDsKey(String dsKey, String tagName) {
		if (!HiveComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
			throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", dsKey, "dsKey", tagName, this.resource));
		}
	}

	@Override
	protected TangYuanNode getTextNode(String data) {
		return new HiveTextNode(data);
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

	/** 扫描REF */
	private void buildSqlNode(List<XmlNodeWrapper> contexts) {
		String tagName = "sql";
		for (XmlNodeWrapper xNode : contexts) {
			String id     = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String fullId = getFullId(id);
			if (this.integralRefMap.containsKey(fullId)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			TangYuanNode sqlNode = parseNode(xNode, false);
			if (null != sqlNode) {
				this.integralRefMap.put(fullId, sqlNode);
				log.info(lang("add.tag.service", tagName, fullId));
			}
		}
	}

	/** 扫描Segment */
	private void buildSegmentNode(List<XmlNodeWrapper> contexts) {
		String tagName = "segment";
		for (XmlNodeWrapper xNode : contexts) {
			String id     = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String fullId = getFullId(id);
			if (this.integralRefMap.containsKey(fullId)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			TangYuanNode sqlNode = new SegmentNode(xNode);
			this.integralRefMap.put(fullId, sqlNode);
			log.info(lang("add.tag.service", tagName, fullId));
		}
	}

	private void configurationElement() {
		List<AbstractServiceNode> selectSetList  = buildSelectSetNode(this.root.evalNodes("selectSet"));
		List<AbstractServiceNode> selectOneList  = buildSelectOneNode(this.root.evalNodes("selectOne"));
		List<AbstractServiceNode> selectVarList  = buildSelectVarNode(this.root.evalNodes("selectVar"));
		List<AbstractServiceNode> hqlList        = buildHqlNode(this.root.evalNodes("hql"));
		List<AbstractServiceNode> sqlServiceList = buildSqlServiceNode(this.root.evalNodes("sql-service"));

		registerService(selectSetList, "selectSet");
		registerService(selectOneList, "selectOne");
		registerService(selectVarList, "selectVar");
		registerService(hqlList, "hql");
		registerService(sqlServiceList, "sql-service");
	}

	private List<AbstractServiceNode> buildSelectSetNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "selectSet";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String       id         = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       resultType = getStringFromAttr(xNode, "resultType");
			String       resultMap  = getStringFromAttr(xNode, "resultMap");
			Integer      fetchSize  = getIntegerFromAttr(xNode, "fetchSize");
			String       dsKey      = getStringFromAttr(xNode, "dsKey");
			//			String       txRef      = getStringFromAttr(xNode, "txRef");
			String       _cacheUse  = getStringFromAttr(xNode, "cacheUse");
			// 新增,每个服务节点都需要包含的
			String       desc       = getStringFromAttr(xNode, "desc");
			String[]     groups     = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode    = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			XTransactionDefinition txDef         = null;

			SelectResult           selectResult  = parseSelectResult(resultType, resultMap, tagName, this.componentContext);
			CacheUseVo             cacheUse      = parseCacheUse(_cacheUse, id);

			SelectSetNode          selectSetNode = new SelectSetNode(id, this.ns, getFullId(id), selectResult.resultType, selectResult.resultMap, dsKey, fetchSize, txDef, sqlNode,
					cacheUse, desc, groups);
			list.add(selectSetNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildSelectOneNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "selectOne";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String       id         = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       resultType = getStringFromAttr(xNode, "resultType");
			String       resultMap  = getStringFromAttr(xNode, "resultMap");
			String       dsKey      = getStringFromAttr(xNode, "dsKey");
			//			String       txRef      = getStringFromAttr(xNode, "txRef");
			String       _cacheUse  = getStringFromAttr(xNode, "cacheUse");
			// 新增,每个服务节点都需要包含的
			String       desc       = getStringFromAttr(xNode, "desc");
			String[]     groups     = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode    = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);
			XTransactionDefinition txDef         = null;

			SelectResult           selectResult  = parseSelectResult(resultType, resultMap, tagName, this.componentContext);
			CacheUseVo             cacheUse      = parseCacheUse(_cacheUse, id);

			SelectOneNode          selectOneNode = new SelectOneNode(id, this.ns, getFullId(id), selectResult.resultType, selectResult.resultMap, dsKey, txDef, sqlNode, cacheUse,
					desc, groups);

			list.add(selectOneNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildSelectVarNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "selectVar";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String       id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       dsKey     = getStringFromAttr(xNode, "dsKey");
			//			String       txRef     = getStringFromAttr(xNode, "txRef");
			String       resultMap = getStringFromAttr(xNode, "resultMap");
			String       _cacheUse = getStringFromAttr(xNode, "cacheUse");
			// 新增,每个服务节点都需要包含的
			String       desc      = getStringFromAttr(xNode, "desc");
			String[]     groups    = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode   = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);
			XTransactionDefinition txDef         = null;

			SelectResult           selectResult  = parseSelectResult(null, resultMap, tagName, this.componentContext);
			CacheUseVo             cacheUse      = parseCacheUse(_cacheUse, id);
			SelectVarNode          selectVarNode = new SelectVarNode(id, this.ns, getFullId(id), dsKey, txDef, sqlNode, selectResult.resultMap, cacheUse, desc, groups);
			list.add(selectVarNode);
		}

		return list;
	}

	private List<AbstractServiceNode> buildHqlNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "hql";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String       id          = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       dsKey       = getStringFromAttr(xNode, "dsKey");
			//			String       txRef       = getStringFromAttr(xNode, "txRef");
			String       _cacheClean = getStringFromAttr(xNode, "cacheClean");
			boolean      asyncHql    = getBoolFromAttr(xNode, "asyncHql", false);

			// 新增,每个服务节点都需要包含的
			String       desc        = getStringFromAttr(xNode, "desc");
			String[]     groups      = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode     = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);
			XTransactionDefinition txDef      = null;
			CacheCleanVo           cacheClean = parseCacheClean(_cacheClean, id);

			HqlNode                hqlNode    = new HqlNode(id, ns, getFullId(id), dsKey, txDef, sqlNode, cacheClean, asyncHql, desc, groups);
			list.add(hqlNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildSqlServiceNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "sql-service";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String   id          = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String   dsKey       = getStringFromAttr(xNode, "dsKey");
			//			String   txRef       = getStringFromAttr(xNode, "txRef");
			String   _cacheUse   = getStringFromAttr(xNode, "cacheUse");
			String   _cacheClean = getStringFromAttr(xNode, "cacheClean");
			// 新增,每个服务节点都需要包含的
			String   desc        = getStringFromAttr(xNode, "desc");
			String[] groups      = getStringArrayFromAttr(xNode, "group");

			checkServiceRepeated(id, tagName);
			if (null != dsKey) {
				dsKey = checkReferencedDsKey(dsKey, tagName, id);
			}

			XTransactionDefinition txDef      = null;
			CacheUseVo             cacheUse   = parseCacheUse(_cacheUse, id);
			CacheCleanVo           cacheClean = parseCacheClean(_cacheClean, id);

			this.idWithSqlService = id;
			this.dsKeyWithSqlService = dsKey;
			this.serviceResultType = TangYuanContainer.getInstance().getDefaultResultType();

			TangYuanNode sqlNode = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			ServiceNode serviceNode = new ServiceNode(id, this.ns, getFullId(id), dsKey, txDef, sqlNode, cacheUse, cacheClean, this.serviceResultType, desc, groups);
			list.add(serviceNode);
		}
		return list;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private class SelectSetHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "selectSet";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				//				return;
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			Integer fetchSize = getIntegerFromAttr(xNode, "fetchSize");
			String  dsKey     = getStringFromAttr(xNode, "dsKey");
			String  resultMap = getStringFromAttr(xNode, "resultMap");
			String  _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String  resultKey = parseVariableKey(xNode, "resultKey", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			SelectResult          selectResult  = parseSelectResult(null, resultMap, tagName, componentContext);
			//			CacheUseVo            cacheUse      = parseCacheUse(_cacheUse, "");
			CacheUseVo            cacheUse      = parseCacheUse(_cacheUse, idWithSqlService);
			InternalSelectSetNode selectSetNode = new InternalSelectSetNode(dsKey, resultKey, sqlNode, serviceResultType, selectResult.resultMap, fetchSize, cacheUse);
			targetContents.add(selectSetNode);
		}
	}

	private class SelectOneHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "selectOne";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey     = getStringFromAttr(xNode, "dsKey");
			String resultMap = getStringFromAttr(xNode, "resultMap");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String resultKey = parseVariableKey(xNode, "resultKey", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			SelectResult          selectResult  = parseSelectResult(null, resultMap, tagName, componentContext);
			CacheUseVo            cacheUse      = parseCacheUse(_cacheUse, idWithSqlService);
			InternalSelectOneNode selectOneNode = new InternalSelectOneNode(dsKey, resultKey, sqlNode, serviceResultType, selectResult.resultMap, cacheUse);
			targetContents.add(selectOneNode);
		}
	}

	private class SelectVarHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "selectVar";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey     = getStringFromAttr(xNode, "dsKey");
			String resultMap = getStringFromAttr(xNode, "resultMap");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String resultKey = parseVariableKey(xNode, "resultKey", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			SelectResult          selectResult  = parseSelectResult(null, resultMap, tagName, componentContext);
			CacheUseVo            cacheUse      = parseCacheUse(_cacheUse, idWithSqlService);
			InternalSelectVarNode selectVarNode = new InternalSelectVarNode(dsKey, resultKey, sqlNode, selectResult.resultMap, cacheUse);
			targetContents.add(selectVarNode);
		}
	}

	private class HqlHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "selectVar";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String  dsKey       = getStringFromAttr(xNode, "dsKey");
			//			String  resultMap   = getStringFromAttr(xNode, "resultMap");
			String  _cacheClean = getStringFromAttr(xNode, "cacheClean");
			boolean asyncHql    = getBoolFromAttr(xNode, "asyncHql", false);
			String  resultKey   = parseVariableKey(xNode, "resultKey", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}

			CacheCleanVo    cacheClean = parseCacheClean(_cacheClean, idWithSqlService);

			InternalHqlNode hqlNode    = new InternalHqlNode(dsKey, resultKey, sqlNode, cacheClean, asyncHql);
			targetContents.add(hqlNode);
		}
	}

	protected class ForEachHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {

			String tagName    = "foreach";

			String collection = getStringFromAttr(xNode, "collection", lang("xml.tag.attribute.empty", "collection", tagName, resource));
			String index      = parseVariableKey(xNode, "index", tagName);
			String open       = getStringFromAttr(xNode, "open");
			String close      = getStringFromAttr(xNode, "close");
			String separator  = getStringFromAttr(xNode, "separator");

			collection = parseVariableKey(xNode, "collection", tagName);

			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
			}

			ForEachNode forEachNode = new HiveForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator);
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

			Object start = 0;
			Object end   = null;

			if (null != _start) {
				if (checkVar(_start)) {
					start = parseVariableUseGA(_start);
				} else {
					start = Integer.parseInt(_start);
				}
			}

			if (checkVar(_end)) {
				end = parseVariableUseGA(_end);
			} else {
				end = Integer.parseInt(_end);
			}

			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
			}

			HiveForNode forNode = new HiveForNode(sqlNode, index, start, end, open, close, separator);
			targetContents.add(forNode);
		}
	}

	/////////////////////////////////////////////////////////

	//	private Map<String, NodeHandler> nodeHandlers = new HashMap<String, NodeHandler>() {
	//		private static final long serialVersionUID = 1L;
	//
	//		{
	//			put("foreach", new ForEachHandler());
	//			put("if", new IfHandler());
	//			put("else", new ElseHandler());
	//			put("elseif", new ElseIfHandler());
	//			put("include", new IncludeHandler());
	//			put("exception", new ThrowHandler());
	//			put("return", new ReturnHandler());
	//			put("setvar", new SetVarHandler());
	//			put("log", new LogHandler());
	//			put("selectSet", new SelectSetHandler());
	//			put("selectOne", new SelectOneHandler());
	//			put("selectVar", new SelectVarHandler());
	//			put("hql", new HqlHandler());
	//			put("call", new CallHandler());
	//		}
	//	};

	//	private List<AbstractServiceNode> buildSelectOneNodes(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			TangYuanNode sqlNode = parseNode(context, false);
	//			if (null != sqlNode) {
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml v
	//				existingService(id);
	//
	//				String       _resultType  = StringUtils.trim(context.getStringAttribute("resultType"));
	//				String       _resultMap   = StringUtils.trim(context.getStringAttribute("resultMap"));
	//				SelectResult selectResult = parseSelectResult(_resultType, _resultMap);
	//
	//				String       dsKey        = StringUtils.trim(context.getStringAttribute("dsKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				// String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
	//				// XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
	//				// if (null == txDef) {
	//				// throw new XmlParseException("service txRef is invalid: " + id);
	//				// }
	//				XTransactionDefinition txDef     = null;
	//
	//				String                 _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//				CacheUseVo             cacheUse  = null;
	//				if (null != _cacheUse && _cacheUse.length() > 0) {
	//					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//				}
	//
	//				SelectOneNode selectOneNode = new SelectOneNode(id, ns, getFullId(id), selectResult.resultType, selectResult.resultMap, dsKey, txDef, sqlNode, cacheUse);
	//				list.add(selectOneNode);
	//			}
	//		}
	//		return list;
	//	}
	//	private List<AbstractServiceNode> buildSelectVarNodes(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			TangYuanNode sqlNode = parseNode(context, false);
	//			if (null != sqlNode) {
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml v
	//				existingService(id);
	//
	//				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				// String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
	//				// XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
	//				// if (null == txDef) {
	//				// throw new XmlParseException("service txRef is invalid: " + id);
	//				// }
	//				XTransactionDefinition txDef     = null;
	//
	//				String                 _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//				CacheUseVo             cacheUse  = null;
	//				if (null != _cacheUse && _cacheUse.length() > 0) {
	//					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//				}
	//
	//				SelectVarNode selectVarNode = new SelectVarNode(id, ns, getFullId(id), dsKey, txDef, sqlNode, cacheUse);
	//				list.add(selectVarNode);
	//			}
	//		}
	//		return list;
	//	}
	//	private List<AbstractServiceNode> buildHqlNodes(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			TangYuanNode sqlNode = parseNode(context, false);
	//			if (null != sqlNode) {
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml v
	//				existingService(id);
	//
	//				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				// String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
	//				// XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
	//				// if (null == txDef) {
	//				// throw new XmlParseException("service txRef is invalid: " + id);
	//				// }
	//				XTransactionDefinition txDef       = null;
	//
	//				String                 _cacheClean = StringUtils.trim(context.getStringAttribute("cacheClean"));
	//				CacheCleanVo           cacheClean  = null;
	//				if (null != _cacheClean && _cacheClean.length() > 0) {
	//					cacheClean = parseCacheClean(_cacheClean, getFullId(id));
	//				}
	//
	//				boolean asyncHql = getBooleanValueFromXmlNode(context, "asyncHql", true, false, "");
	//
	//				HqlNode hqlNode  = new HqlNode(id, ns, getFullId(id), dsKey, txDef, sqlNode, cacheClean, asyncHql);
	//				list.add(hqlNode);
	//			}
	//		}
	//		return list;
	//	}

	//	private String checkDsKey(String dsKey, String service) {
	//		if (null == dsKey) {
	//			dsKey = HiveComponent.getInstance().getDataSourceManager().getDefaultDsKey();
	//			if (null == dsKey) {
	//				throw new XmlParseException("service[" + service + "] uses an invalid dsKey: " + dsKey);
	//			}
	//		} else {
	//			if (!HiveComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
	//				throw new XmlParseException("service[" + service + "] uses an invalid dsKey: " + dsKey);
	//			}
	//		}
	//		return dsKey;
	//	}
	//
	//	private void checkInnerDsKey(String dsKey, String method) {
	//		if (!HiveComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
	//			throw new XmlParseException("service[" + method + "] uses an invalid dsKey: " + dsKey);
	//		}
	//	}
	//	private void buildRefNode(List<XmlNodeWrapper> contexts) {
	//		for (XmlNodeWrapper context : contexts) {
	//			String id     = StringUtils.trim(context.getStringAttribute("id")); // xml V
	//			String fullId = getFullId(id);
	//			if (null == this.sqlContext.getXmlContext().getIntegralRefMap().get(fullId)) {
	//				TangYuanNode sqlNode = parseNode(context, false);
	//				if (null != sqlNode) {
	//					this.sqlContext.getXmlContext().getIntegralRefMap().put(fullId, sqlNode);
	//					log.info("add <sql> node: " + fullId);
	//				}
	//			} else {
	//				throw new XmlParseException("Duplicate <sql> nodes: " + id);
	//			}
	//		}
	//	}
	//
	//	// 扫描段
	//	private void buildSegmentNode(List<XmlNodeWrapper> contexts) {
	//		for (XmlNodeWrapper context : contexts) {
	//			String id     = StringUtils.trim(context.getStringAttribute("id")); // xml V
	//			String fullId = getFullId(id);
	//			if (null == this.sqlContext.getXmlContext().getIntegralRefMap().get(fullId)) {
	//				TangYuanNode sqlNode = new SegmentNode(context);
	//				if (null != sqlNode) {
	//					this.sqlContext.getXmlContext().getIntegralRefMap().put(fullId, sqlNode);
	//					log.info("add <segment> node: " + fullId);
	//				}
	//			} else {
	//				throw new XmlParseException("Duplicate <segment> nodes: " + id);
	//			}
	//		}
	//	}

	//	private void configurationElement(XmlNodeWrapper context) {
	//		List<AbstractServiceNode> selectSetList  = buildSelectSetNodes(context.evalNodes("selectSet"));
	//		List<AbstractServiceNode> selectOneList  = buildSelectOneNodes(context.evalNodes("selectOne"));
	//		List<AbstractServiceNode> selectVarList  = buildSelectVarNodes(context.evalNodes("selectVar"));
	//		List<AbstractServiceNode> hqlList        = buildHqlNodes(context.evalNodes("hql"));
	//		List<AbstractServiceNode> sqlServiceList = buildSqlServiceNodes(context.evalNodes("hive-service"));
	//
	//		registerService(selectSetList, "selectSet");
	//		registerService(selectOneList, "selectOne");
	//		registerService(selectVarList, "selectVar");
	//		registerService(hqlList, "hql");
	//		registerService(sqlServiceList, "hive-service");
	//	}
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

	//	private List<TangYuanNode> parseDynamicTags(XmlNodeWrapper node) {
	//		List<TangYuanNode> contents = new ArrayList<TangYuanNode>();
	//		NodeList           children = node.getNode().getChildNodes();
	//		for (int i = 0; i < children.getLength(); i++) {
	//			XmlNodeWrapper child = node.newXMlNode(children.item(i));
	//			if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
	//				String data = child.getStringBody("");
	//				if (isEmpty(data)) {
	//					continue;
	//				}
	//				// 使用新的sqlText节点
	//				contents.add(new SqlTextNode(data));
	//				// log.info("-----------data:" + data);
	//			} else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) {
	//				String      nodeName = child.getNode().getNodeName();
	//				// log.info("-----------name:" + nodeName);
	//				NodeHandler handler  = nodeHandlers.get(nodeName);
	//				if (handler == null) {
	//					throw new XmlParseException("Unknown element <" + nodeName + "> in SQL statement.");
	//				}
	//				handler.handleNode(child, contents);
	//			}
	//		}
	//		return contents;
	//	}

	//	private SelectResult parseSelectResult(String _resultType, String _resultMap) {
	//		Class<?>  resultType = null;
	//		MappingVo resultMap  = null;
	//		if (null == _resultType && null == _resultMap) {// 都没有值的情况下
	//			resultType = TangYuanContainer.getInstance().getDefaultResultType();// 这里是简单服务,直接只用系统默认即可
	//		} else if (null != _resultType && null != _resultMap) {// 都存在值的情况下
	//			// resultType处理
	//			if ("map".equalsIgnoreCase(_resultType)) {
	//				resultType = Map.class;
	//			} else if ("xco".equalsIgnoreCase(_resultType)) {
	//				resultType = XCO.class;
	//			} else {
	//				resultType = ClassUtils.forName(_resultType);
	//			}
	//			// resultMap处理
	//			resultMap = this.sqlContext.getMappingVoMap().get(_resultMap);
	//			if (null == resultMap) {
	//				throw new XmlParseException("Non-existent resultMap: " + _resultMap);
	//			}
	//			// 检测是否冲突
	//			if (null != resultMap.getBeanClass() && resultType != resultMap.getBeanClass()) {
	//				throw new XmlParseException("resultMap[" + resultMap.getBeanClass() + "] and resultType[" + resultType + "]类型冲突");
	//			}
	//		} else if (null == _resultType && null != _resultMap) {
	//			resultMap = this.sqlContext.getMappingVoMap().get(_resultMap);
	//			if (null == resultMap) {
	//				throw new XmlParseException("Non-existent resultMap: " + _resultMap);
	//			}
	//			if (null == resultMap.getBeanClass()) {
	//				resultType = TangYuanContainer.getInstance().getDefaultResultType();
	//			}
	//			// 具体的类型看resultMap.type
	//		} else if (null != _resultType && null == _resultMap) {
	//			if ("map".equalsIgnoreCase(_resultType)) {
	//				resultType = Map.class;
	//			} else if ("xco".equalsIgnoreCase(_resultType)) {
	//				resultType = XCO.class;
	//			} else {
	//				resultType = ClassUtils.forName(_resultType);
	//				// 默认Bean Result Mapping
	//			}
	//		}
	//		return new SelectResult(resultType, resultMap);
	//	}

	//	private List<AbstractServiceNode> buildSelectSetNodes(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			TangYuanNode sqlNode = parseNode(context, false);
	//			if (null != sqlNode) {
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml v
	//				existingService(id);
	//
	//				String       _resultType  = StringUtils.trim(context.getStringAttribute("resultType"));
	//				String       _resultMap   = StringUtils.trim(context.getStringAttribute("resultMap"));
	//				SelectResult selectResult = parseSelectResult(_resultType, _resultMap);
	//
	//				String       _fetchSize   = StringUtils.trim(context.getStringAttribute("fetchSize")); // xml v
	//				Integer      fetchSize    = null;
	//				if (null != _fetchSize) {
	//					fetchSize = Integer.valueOf(_fetchSize);
	//				}
	//
	//				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				// String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
	//				// XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
	//				// if (null == txDef) {
	//				// throw new XmlParseException("service txRef is invalid: " + id);
	//				// }
	//				XTransactionDefinition txDef     = null;
	//
	//				String                 _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//				CacheUseVo             cacheUse  = null;
	//				if (null != _cacheUse && _cacheUse.length() > 0) {
	//					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//				}
	//
	//				SelectSetNode selectSetNode = new SelectSetNode(id, ns, getFullId(id), selectResult.resultType, selectResult.resultMap, dsKey, fetchSize, txDef, sqlNode, cacheUse);
	//				list.add(selectSetNode);
	//			}
	//		}
	//		return list;
	//	}

	//	private List<AbstractServiceNode> buildSqlServiceNodes(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			String id = StringUtils.trim(context.getStringAttribute("id")); // xml v
	//			existingService(id);
	//
	//			String                 txRef = StringUtils.trim(context.getStringAttribute("txRef"));
	//			// XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
	//			// if (null == txDef) {
	//			// throw new XmlParseException("service txRef is invalid: " + id);
	//			// }
	//			XTransactionDefinition txDef = null;
	//
	//			String                 dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
	//			if (null != dsKey && !HiveComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
	//				throw new XmlParseException("service[" + id + "] uses an invalid dsKey: " + dsKey);
	//			}
	//
	//			this.dsKeyWithSqlService = dsKey;
	//
	//			String _resultType = StringUtils.trim(context.getStringAttribute("resultType"));
	//			this.serviceResultType = null;
	//			if ("map".equalsIgnoreCase(_resultType)) {
	//				this.serviceResultType = Map.class;
	//			} else if ("xco".equalsIgnoreCase(_resultType)) {
	//				this.serviceResultType = XCO.class;
	//			} else {
	//				this.serviceResultType = TangYuanContainer.getInstance().getDefaultResultType();
	//			}
	//
	//			String     _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//			CacheUseVo cacheUse  = null;
	//			if (null != _cacheUse && _cacheUse.length() > 0) {
	//				cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//			}
	//			String       _cacheClean = StringUtils.trim(context.getStringAttribute("cacheClean"));
	//			CacheCleanVo cacheClean  = null;
	//			if (null != _cacheClean && _cacheClean.length() > 0) {
	//				cacheClean = parseCacheClean(_cacheClean, getFullId(id));
	//			}
	//
	//			TangYuanNode sqlNode = parseNode(context, true);
	//			if (null != sqlNode) {
	//				ServiceNode serviceNode = new ServiceNode(id, txRef, getFullId(id), dsKey, txDef, sqlNode, cacheUse, cacheClean, this.serviceResultType);
	//				list.add(serviceNode);
	//			}
	//		}
	//		return list;
	//	}
	//	private class SetVarHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			// <setvar key="{x}" value="100" type="Integer" />
	//			String key    = StringUtils.trim(nodeToHandle.getStringAttribute("key")); // xml v
	//			String _value = StringUtils.trim(nodeToHandle.getStringAttribute("value")); // xml v
	//			String type   = StringUtils.trim(nodeToHandle.getStringAttribute("type")); // xml v
	//			if (!checkVar(key)) {
	//				throw new XmlParseException("<setvar> node key is not legal, should be {xxx}.");
	//			}
	//			key = getRealVal(key);
	//			Object  value    = null;
	//			boolean constant = true;
	//			if (checkVar(_value)) {
	//				constant = false;
	//				value = new NormalParser().parse(getRealVal(_value));
	//			} else {
	//				value = getSetVarValue(_value, type);
	//			}
	//			SetVarNode setVarNode = new SetVarNode(key, value, constant);
	//			targetContents.add(setVarNode);
	//		}
	//	}

	//	private class LogHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			String message = StringUtils.trim(nodeToHandle.getStringAttribute("message")); // xml v
	//			String _level  = StringUtils.trim(nodeToHandle.getStringAttribute("level")); // xml c
	//			int    level   = 3;
	//			if (null != _level) {
	//				level = getLogLevel(_level);
	//			}
	//			LogNode logNode = new LogNode(level, message);
	//			targetContents.add(logNode);
	//		}
	//	}
	//
	//	private class ReturnHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			// Variable result = null;
	//			Object result  = null;
	//			String _result = StringUtils.trim(nodeToHandle.getStringAttribute("value"));
	//			if (null != _result) {
	//				if (checkVar(_result)) {
	//					result = new NormalParser().parse(getRealVal(_result));
	//				} else {
	//					result = parseValue(_result);
	//				}
	//			}
	//
	//			List<XmlNodeWrapper> properties = nodeToHandle.evalNodes("property");
	//			List<PropertyItem>   resultList = buildPropertyItem(properties, "return");
	//
	//			if (null != result && null != resultList) {
	//				throw new XmlParseException("<return> node in the result | property can only choose a way.");
	//			}
	//
	//			ReturnNode returnNode = new ReturnNode(result, resultList, serviceResultType);
	//			targetContents.add(returnNode);
	//		}
	//	}

	//	private class ThrowHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			String test    = StringUtils.trim(nodeToHandle.getStringAttribute("test")); // xml v
	//			String code    = StringUtils.trim(nodeToHandle.getStringAttribute("code")); // xml v
	//			String message = StringUtils.trim(nodeToHandle.getStringAttribute("message"));
	//			String i18n    = StringUtils.trim(nodeToHandle.getStringAttribute("i18n"));
	//			if (null == test || null == code) {
	//				throw new XmlParseException("In the Exception node, the test, code attribute can not be empty.");
	//			}
	//			targetContents.add(new ExceptionNode(new LogicalExprParser().parse(test), Integer.parseInt(code), message, i18n));
	//		}
	//	}

	//	private class CallHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			String serviceId = StringUtils.trim(nodeToHandle.getStringAttribute("service"));
	//			if (null == serviceId) {
	//				throw new XmlParseException("The service attribute in the call node can not be empty");
	//			}
	//
	//			// fix: 新增变量调用功能
	//			Object service = serviceId;
	//			if (checkVar(serviceId)) {
	//				// service = new NormalParser().parse(serviceId);
	//				service = new NormalParser().parse(getRealVal(serviceId));
	//			}
	//
	//			String   resultKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("resultKey")));
	//			String   _mode     = StringUtils.trim(nodeToHandle.getStringAttribute("mode"));// xml v
	//
	//			CallMode mode      = null;// 增加新的默认模式
	//			if (null != _mode) {
	//				mode = getCallMode(_mode);
	//			}
	//
	//			// String exResultKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("exResultKey")));
	//
	//			String               codeKey    = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("codeKey")));
	//			String               messageKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("messageKey")));
	//
	//			List<XmlNodeWrapper> properties = nodeToHandle.evalNodes("property");
	//			List<PropertyItem>   itemList   = buildPropertyItem(properties, "call");
	//
	//			// service id可以放在运行期间检查
	//			// targetContents.add(new CallNode(service, resultKey, mode, itemList, exResultKey));
	//			targetContents.add(new CallNode(service, resultKey, mode, itemList, codeKey, messageKey));
	//		}
	//	}

	//	private class SelectSetHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
	//			if (null != sqlNode) {
	//				String dsKey = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
	//				if (null == dsKey) {
	//					dsKey = dsKeyWithSqlService;
	//				} else {
	//					checkInnerDsKey(dsKey, "SelectSet");
	//				}
	//
	//				String  resultKey  = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("resultKey")));
	//				Integer fetchSize  = null;
	//				String  _fetchSize = StringUtils.trim(nodeToHandle.getStringAttribute("fetchSize"));
	//				if (null != _fetchSize) {
	//					fetchSize = Integer.valueOf(_fetchSize);
	//				}
	//
	//				String     _cacheUse = StringUtils.trim(nodeToHandle.getStringAttribute("cacheUse"));
	//				CacheUseVo cacheUse  = null;
	//				if (null != _cacheUse && _cacheUse.length() > 0) {
	//					cacheUse = parseCacheUse(_cacheUse, "");
	//				}
	//
	//				InternalSelectSetNode selectSetNode = new InternalSelectSetNode(dsKey, resultKey, sqlNode, serviceResultType, fetchSize, cacheUse);
	//				targetContents.add(selectSetNode);
	//			}
	//		}
	//	}
	//
	//	private class SelectOneHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
	//			if (null != sqlNode) {
	//				String dsKey = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
	//				if (null == dsKey) {
	//					dsKey = dsKeyWithSqlService;
	//				} else {
	//					checkInnerDsKey(dsKey, "SelectOne");
	//				}
	//				String     resultKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("resultKey")));
	//
	//				String     _cacheUse = StringUtils.trim(nodeToHandle.getStringAttribute("cacheUse"));
	//				CacheUseVo cacheUse  = null;
	//				if (null != _cacheUse && _cacheUse.length() > 0) {
	//					cacheUse = parseCacheUse(_cacheUse, "");
	//				}
	//
	//				InternalSelectOneNode selectOneNode = new InternalSelectOneNode(dsKey, resultKey, sqlNode, serviceResultType, cacheUse);
	//				targetContents.add(selectOneNode);
	//			}
	//		}
	//	}
	//
	//	private class SelectVarHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
	//			if (null != sqlNode) {
	//				String dsKey = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
	//				if (null == dsKey) {
	//					dsKey = dsKeyWithSqlService;
	//				} else {
	//					checkInnerDsKey(dsKey, "SelectVar");
	//				}
	//				String     resultKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("resultKey")));
	//
	//				String     _cacheUse = StringUtils.trim(nodeToHandle.getStringAttribute("cacheUse"));
	//				CacheUseVo cacheUse  = null;
	//				if (null != _cacheUse && _cacheUse.length() > 0) {
	//					cacheUse = parseCacheUse(_cacheUse, "");
	//				}
	//
	//				InternalSelectVarNode selectVarNode = new InternalSelectVarNode(dsKey, resultKey, sqlNode, cacheUse);
	//				targetContents.add(selectVarNode);
	//			}
	//		}
	//	}

	//	protected interface NodeHandler {
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
	//
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
	//
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

	//	private class IncludeHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			String       refKey  = nodeToHandle.getStringAttribute("ref"); // xml V
	//			TangYuanNode refNode = sqlContext.getXmlContext().getIntegralRefMap().get(refKey);
	//			if (null == refNode) {
	//				throw new XmlParseException("The referenced node is null: " + refKey);
	//			}
	//
	//			// 增加段的引用
	//			if (refNode instanceof SegmentNode) {
	//				XmlNodeWrapper innerNode = ((SegmentNode) refNode).getNode();
	//				refNode = parseNode(innerNode, true);
	//				if (null == refNode) {
	//					log.warn("The referenced segment is empty, ref: " + refKey);
	//					return;
	//				}
	//			}
	//
	//			targetContents.add(refNode);
	//		}
	//	}

	//	private class ForEachHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//
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
	//			if (null == sqlNode && null == open && null == close && null == separator) {
	//				open = "(";
	//				close = ")";
	//				separator = ",";
	//			}
	//			if (null == sqlNode) {
	//				if (null == index) {
	//					index = "i";
	//				}
	//				sqlNode = new SqlTextNode("#{" + collection + "[" + index + "]}");
	//			}
	//
	//			// 增加Start && End选项
	//			String _start = StringUtils.trim(nodeToHandle.getStringAttribute("start"));
	//			String _end   = StringUtils.trim(nodeToHandle.getStringAttribute("end"));
	//			String _pLen  = StringUtils.trim(nodeToHandle.getStringAttribute("len"));
	//
	//			Object start  = null;
	//			Object end    = null;
	//			Object pLen   = null;
	//
	//			if (null != _start) {
	//				if (checkVar(_start)) {
	//					start = new GAParserWarper().parse(getRealVal(_start));
	//				} else {
	//					start = Integer.parseInt(_start);
	//				}
	//			}
	//			if (null != _end) {
	//				if (checkVar(_end)) {
	//					end = new GAParserWarper().parse(getRealVal(_end));
	//				} else {
	//					end = Integer.parseInt(_end);
	//				}
	//			}
	//			if (null != _pLen) {
	//				if (checkVar(_pLen)) {
	//					pLen = new GAParserWarper().parse(getRealVal(_pLen));
	//				} else {
	//					pLen = Integer.parseInt(_pLen);
	//				}
	//			}
	//
	//			Boolean     ignoreIOOB  = getBooleanValueFromXmlNode(nodeToHandle, "ignoreIOOB", true, false, "in the <foreach> node, ");
	//			int         indexMode   = ForEachNode.getAndCheckIndexMode(start, end, pLen);
	//
	//			// ForEachNode forEachNode = new SqlForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator);
	//			ForEachNode forEachNode = new HiveForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator, start, end, pLen, ignoreIOOB, indexMode);
	//			targetContents.add(forEachNode);
	//		}
	//	}

	//	private class HqlHandler implements NodeHandler {
	//	public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//		TangYuanNode sqlNode = parseNode(nodeToHandle, true);
	//		if (null != sqlNode) {
	//			String dsKey = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
	//			if (null == dsKey) {
	//				dsKey = dsKeyWithSqlService;
	//			} else {
	//				checkInnerDsKey(dsKey, "hql");
	//			}
	//			String resultKey = StringUtils.trim(nodeToHandle.getStringAttribute("resultKey"));
	//			if (null != resultKey) {
	//				if (!checkVar(resultKey)) {
	//					throw new XmlParseException("<hql> resultKey is not legal, should be {xxx}.");
	//				}
	//				resultKey = getRealVal(resultKey);
	//			}
	//
	//			String       _cacheClean = StringUtils.trim(nodeToHandle.getStringAttribute("cacheClean"));
	//			CacheCleanVo cacheClean  = null;
	//			if (null != _cacheClean && _cacheClean.length() > 0) {
	//				cacheClean = parseCacheClean(_cacheClean, "");
	//			}
	//
	//			boolean         asyncHql = getBooleanValueFromXmlNode(nodeToHandle, "asyncHql", true, false, "");
	//
	//			InternalHqlNode hqlNode  = new InternalHqlNode(dsKey, resultKey, sqlNode, cacheClean, asyncHql);
	//			targetContents.add(hqlNode);
	//		}
	//	}
	//}
}
