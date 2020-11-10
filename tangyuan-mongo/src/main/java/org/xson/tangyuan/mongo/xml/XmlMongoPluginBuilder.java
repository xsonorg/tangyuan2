package org.xson.tangyuan.mongo.xml;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.mongo.MongoComponent;
import org.xson.tangyuan.mongo.xml.node.InternalMongoCommandNode;
import org.xson.tangyuan.mongo.xml.node.InternalMongoDeleteNode;
import org.xson.tangyuan.mongo.xml.node.InternalMongoInsertNode;
import org.xson.tangyuan.mongo.xml.node.InternalMongoSelectOneNode;
import org.xson.tangyuan.mongo.xml.node.InternalMongoSelectSetNode;
import org.xson.tangyuan.mongo.xml.node.InternalMongoSelectVarNode;
import org.xson.tangyuan.mongo.xml.node.InternalMongoUpdateNode;
import org.xson.tangyuan.mongo.xml.node.MongoCommandNode;
import org.xson.tangyuan.mongo.xml.node.MongoDeleteNode;
import org.xson.tangyuan.mongo.xml.node.MongoForEachNode;
import org.xson.tangyuan.mongo.xml.node.MongoForNode;
import org.xson.tangyuan.mongo.xml.node.MongoInsertNode;
import org.xson.tangyuan.mongo.xml.node.MongoSelectOneNode;
import org.xson.tangyuan.mongo.xml.node.MongoSelectSetNode;
import org.xson.tangyuan.mongo.xml.node.MongoSelectVarNode;
import org.xson.tangyuan.mongo.xml.node.MongoServiceNode;
import org.xson.tangyuan.mongo.xml.node.MongoTextNode;
import org.xson.tangyuan.mongo.xml.node.MongoUpdateNode;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.xml.DefaultXmlPluginBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.ForNode;
import org.xson.tangyuan.xml.node.SegmentNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class XmlMongoPluginBuilder extends DefaultXmlPluginBuilder {

	private XmlMongoContext componentContext    = null;
	private String          dsKeyWithSqlService = null;
	private String          idWithSqlService    = null;
	private Class<?>        serviceResultType   = null;

	@Override
	public void setContext(String resource, XmlContext xmlContext) throws Throwable {
		this.componentContext = (XmlMongoContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "mongoservices", false);
		if (this.ns.length() > 0) {
			checkNs(this.ns);
		}
		initNodeHandler();
	}

	@Override
	public void clean() {
		super.clean();

		this.dsKeyWithSqlService = null;
		this.idWithSqlService = null;
		this.serviceResultType = null;
		this.componentContext = null;
	}

	public void parseRef() {
		log.info(lang("xml.start.parsing.type", "plugin[ref]", this.resource));
		buildSqlNode(this.root.evalNodes("sql"));
		// 增加段定义和引用
		buildSegmentNode(this.root.evalNodes("segment"));
	}

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
		nodeHandlers.put("update", new UpdateHandler());
		nodeHandlers.put("delete", new DeleteHandler());
		nodeHandlers.put("insert", new InsertHandler());
		nodeHandlers.put("call", new CallHandler());
		nodeHandlers.put("command", new CommandHandler());
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
			String defaultDsKey = this.componentContext.getDefaultDataSource();
			if (null == defaultDsKey) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", "null", id, "dsKey", tagName, this.resource));
			}
			return defaultDsKey;
		}
		if (!MongoComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
			throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", dsKey, id, "dsKey", tagName, this.resource));
		}
		return dsKey;
	}

	//	private void checkInnerDsKey(String dsKey, String method) {
	//		if (!MongoComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
	//			throw new XmlParseException("service[" + method + "] uses an invalid dsKey: " + dsKey);
	//		}
	//	}

	private void checkInnerDsKey(String dsKey, String tagName) {
		if (!MongoComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
			throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", dsKey, "dsKey", tagName, this.resource));
		}
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
		List<AbstractServiceNode> selectSetList    = buildSelectSetNode(this.root.evalNodes("selectSet"));
		List<AbstractServiceNode> selectOneList    = buildSelectOneNode(this.root.evalNodes("selectOne"));
		List<AbstractServiceNode> selectVarList    = buildSelectVarNode(this.root.evalNodes("selectVar"));
		List<AbstractServiceNode> insertList       = buildInsertNode(this.root.evalNodes("insert"));
		List<AbstractServiceNode> updateList       = buildUpdateNode(this.root.evalNodes("update"));
		List<AbstractServiceNode> deleteList       = buildDeleteNode(this.root.evalNodes("delete"));
		// List<AbstractServiceNode> commandList = buildCommandNodes(context.evalNodes("mongo-command"));
		List<AbstractServiceNode> commandList      = buildCommandNode(this.root.evalNodes("command"));
		List<AbstractServiceNode> mongoServiceList = buildMongoServiceNode(this.root.evalNodes("mongo-service"));

		registerService(selectSetList, "selectSet");
		registerService(selectOneList, "selectOne");
		registerService(selectVarList, "selectVar");
		registerService(insertList, "insert");
		registerService(updateList, "update");
		registerService(deleteList, "delete");
		// registerService(commandList, "mongo-command");
		registerService(commandList, "command");
		registerService(mongoServiceList, "mongo-service");
	}

	@Override
	protected TangYuanNode getTextNode(String data) {
		return new MongoTextNode(data);
	}

	private List<AbstractServiceNode> buildSelectSetNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "selectSet";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String       id         = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       resultType = getStringFromAttr(xNode, "resultType");
			String       resultMap  = getStringFromAttr(xNode, "resultMap");
			String       dsKey      = getStringFromAttr(xNode, "dsKey");
			String       _cacheUse  = getStringFromAttr(xNode, "cacheUse");

			// 新增,每个服务节点都需要包含的
			String       desc       = getStringFromAttr(xNode, "desc");
			String[]     groups     = getStringArrayFromAttr(xNode, "group");

			Integer      fetchSize  = null;
			//			Integer      fetchSize  = getIntegerFromAttr(xNode, "fetchSize");
			//			String       txRef      = getStringFromAttr(xNode, "txRef");

			TangYuanNode sqlNode    = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			SelectResult       selectResult  = parseSelectResult(resultType, resultMap, tagName, this.componentContext);
			CacheUseVo         cacheUse      = parseCacheUse(_cacheUse, id);

			MongoSelectSetNode selectSetNode = new MongoSelectSetNode(id, this.ns, getFullId(id), selectResult.resultType, selectResult.resultMap, dsKey, fetchSize, sqlNode,
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

			SelectResult       selectResult  = parseSelectResult(resultType, resultMap, tagName, this.componentContext);
			CacheUseVo         cacheUse      = parseCacheUse(_cacheUse, id);

			MongoSelectOneNode selectOneNode = new MongoSelectOneNode(id, this.ns, getFullId(id), selectResult.resultType, selectResult.resultMap, dsKey, sqlNode, cacheUse, desc,
					groups);

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

			CacheUseVo         cacheUse      = parseCacheUse(_cacheUse, id);

			MongoSelectVarNode selectVarNode = new MongoSelectVarNode(id, this.ns, getFullId(id), dsKey, sqlNode, cacheUse, desc, groups);
			list.add(selectVarNode);
		}

		return list;
	}

	private List<AbstractServiceNode> buildInsertNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "insert";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String       id          = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       dsKey       = getStringFromAttr(xNode, "dsKey");
			String       _cacheClean = getStringFromAttr(xNode, "cacheClean");

			//			String       rowCount     = parseVariableKey(xNode, "rowCount", tagName);
			//			String       incrementKey = parseVariableKey(xNode, "incrementKey", tagName);

			// 新增,每个服务节点都需要包含的
			String       desc        = getStringFromAttr(xNode, "desc");
			String[]     groups      = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode     = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			CacheCleanVo    cacheClean = parseCacheClean(_cacheClean, id);

			MongoInsertNode insertNode = new MongoInsertNode(id, this.ns, getFullId(id), null, null, dsKey, sqlNode, cacheClean, desc, groups);
			list.add(insertNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildUpdateNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "update";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String       id          = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       dsKey       = getStringFromAttr(xNode, "dsKey");
			String       _cacheClean = getStringFromAttr(xNode, "cacheClean");
			// 新增,每个服务节点都需要包含的
			String       desc        = getStringFromAttr(xNode, "desc");
			String[]     groups      = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode     = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			CacheCleanVo    cacheClean = parseCacheClean(_cacheClean, id);

			MongoUpdateNode updateNode = new MongoUpdateNode(id, this.ns, getFullId(id), dsKey, sqlNode, cacheClean, desc, groups);
			list.add(updateNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildDeleteNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "delete";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String       id          = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       dsKey       = getStringFromAttr(xNode, "dsKey");
			String       _cacheClean = getStringFromAttr(xNode, "cacheClean");
			// 新增,每个服务节点都需要包含的
			String       desc        = getStringFromAttr(xNode, "desc");
			String[]     groups      = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode     = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			CacheCleanVo    cacheClean = parseCacheClean(_cacheClean, id);

			MongoDeleteNode deleteNode = new MongoDeleteNode(id, this.ns, getFullId(id), dsKey, sqlNode, cacheClean, desc, groups);
			list.add(deleteNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildCommandNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "command";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String       id          = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       resultType  = getStringFromAttr(xNode, "resultType");
			String       resultMap   = getStringFromAttr(xNode, "resultMap");
			String       dsKey       = getStringFromAttr(xNode, "dsKey");
			String       _cacheUse   = getStringFromAttr(xNode, "cacheUse");
			String       _cacheClean = getStringFromAttr(xNode, "cacheClean");
			// 新增,每个服务节点都需要包含的
			String       desc        = getStringFromAttr(xNode, "desc");
			String[]     groups      = getStringArrayFromAttr(xNode, "group");

			//			String       rowCount     = parseVariableKey(xNode, "rowCount", tagName);
			//			String       incrementKey = parseVariableKey(xNode, "incrementKey", tagName);
			//			Integer      fetchSize   = getIntegerFromAttr(xNode, "fetchSize");

			TangYuanNode sqlNode     = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			SelectResult     selectResult = parseSelectResult(resultType, resultMap, tagName, this.componentContext);

			CacheUseVo       cacheUse     = parseCacheUse(_cacheUse, id);
			CacheCleanVo     cacheClean   = parseCacheClean(_cacheClean, id);

			MongoCommandNode commandNode  = new MongoCommandNode(id, this.ns, getFullId(id), dsKey, sqlNode, cacheUse, cacheClean, selectResult.resultType, selectResult.resultMap,
					null, null, desc, groups);

			list.add(commandNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildMongoServiceNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "mongo-service";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String   id          = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String   dsKey       = getStringFromAttr(xNode, "dsKey");
			String   _cacheUse   = getStringFromAttr(xNode, "cacheUse");
			String   _cacheClean = getStringFromAttr(xNode, "cacheClean");
			// 新增,每个服务节点都需要包含的
			String   desc        = getStringFromAttr(xNode, "desc");
			String[] groups      = getStringArrayFromAttr(xNode, "group");

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			CacheUseVo   cacheUse   = parseCacheUse(_cacheUse, id);
			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, id);

			this.idWithSqlService = id;
			this.dsKeyWithSqlService = dsKey;
			this.serviceResultType = TangYuanContainer.getInstance().getDefaultResultType();

			TangYuanNode sqlNode = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			MongoServiceNode serviceNode = new MongoServiceNode(id, this.ns, getFullId(id), dsKey, sqlNode, cacheUse, cacheClean, this.serviceResultType, desc, groups);
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
				// throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String  dsKey     = getStringFromAttr(xNode, "dsKey");
			String  resultMap = getStringFromAttr(xNode, "resultMap");
			String  _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String  resultKey = parseVariableKey(xNode, "resultKey", tagName);

			//			Integer fetchSize = getIntegerFromAttr(xNode, "fetchSize");
			Integer fetchSize = null;

			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			SelectResult               selectResult  = parseSelectResult(null, resultMap, tagName, componentContext);
			CacheUseVo                 cacheUse      = parseCacheUse(_cacheUse, idWithSqlService);
			InternalMongoSelectSetNode selectSetNode = new InternalMongoSelectSetNode(dsKey, resultKey, sqlNode, serviceResultType, selectResult.resultMap, fetchSize, cacheUse);
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
			SelectResult               selectResult  = parseSelectResult(null, resultMap, tagName, componentContext);
			CacheUseVo                 cacheUse      = parseCacheUse(_cacheUse, idWithSqlService);

			InternalMongoSelectOneNode selectOneNode = new InternalMongoSelectOneNode(dsKey, resultKey, sqlNode, serviceResultType, selectResult.resultMap, cacheUse);
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
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String resultKey = parseVariableKey(xNode, "resultKey", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			CacheUseVo                 cacheUse      = parseCacheUse(_cacheUse, idWithSqlService);
			InternalMongoSelectVarNode selectVarNode = new InternalMongoSelectVarNode(dsKey, resultKey, sqlNode, cacheUse);
			targetContents.add(selectVarNode);
		}
	}

	private class DeleteHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "delete";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey       = getStringFromAttr(xNode, "dsKey");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");
			String resultKey   = parseVariableKey(xNode, "rowCount", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			CacheCleanVo            cacheClean = parseCacheClean(_cacheClean, idWithSqlService);
			InternalMongoDeleteNode deleteNode = new InternalMongoDeleteNode(dsKey, resultKey, sqlNode, cacheClean);
			targetContents.add(deleteNode);
		}
	}

	private class UpdateHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "update";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey       = getStringFromAttr(xNode, "dsKey");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");
			String resultKey   = parseVariableKey(xNode, "rowCount", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			CacheCleanVo            cacheClean = parseCacheClean(_cacheClean, idWithSqlService);
			InternalMongoUpdateNode updateNode = new InternalMongoUpdateNode(dsKey, resultKey, sqlNode, cacheClean);
			targetContents.add(updateNode);
		}
	}

	private class InsertHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "insert";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey        = getStringFromAttr(xNode, "dsKey");
			String _cacheClean  = getStringFromAttr(xNode, "cacheClean");

			// String resultKey    = parseVariableKey(xNode, "resultKey", tagName);
			// String rowCount     = parseVariableKey(xNode, "rowCount", tagName);
			String incrementKey = parseVariableKey(xNode, "incrementKey", tagName);

			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			CacheCleanVo            cacheClean = parseCacheClean(_cacheClean, idWithSqlService);
			InternalMongoInsertNode insertNode = new InternalMongoInsertNode(dsKey, null, incrementKey, sqlNode, cacheClean);
			targetContents.add(insertNode);
		}
	}

	private class CommandHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "command";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}

			String dsKey       = getStringFromAttr(xNode, "dsKey");
			String resultMap   = getStringFromAttr(xNode, "resultMap");
			String _cacheUse   = getStringFromAttr(xNode, "cacheUse");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");

			// insert, update, delete
			String resultKey   = parseVariableKey(xNode, "resultKey", tagName);
			// insert 专用
			//			String rowCount     = parseVariableKey(xNode, "rowCount", tagName);
			//			String incrementKey = parseVariableKey(xNode, "incrementKey", tagName);

			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			SelectResult             selectResult = parseSelectResult(null, resultMap, tagName, componentContext);
			CacheUseVo               cacheUse     = parseCacheUse(_cacheUse, idWithSqlService);
			CacheCleanVo             cacheClean   = parseCacheClean(_cacheClean, idWithSqlService);
			InternalMongoCommandNode commandNode  = new InternalMongoCommandNode(dsKey, resultKey, null, null, sqlNode, serviceResultType, selectResult.resultMap, cacheUse,
					cacheClean);
			targetContents.add(commandNode);
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

			ForEachNode forEachNode = new MongoForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator);
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

			ForNode forNode = new MongoForNode(sqlNode, index, start, end, open, close, separator);
			targetContents.add(forNode);
		}
	}

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
	//			put("update", new UpdateHandler());
	//			put("delete", new DeleteHandler());
	//			put("insert", new InsertHandler());
	//			put("command", new CommandHandler());
	//			put("call", new CallHandler());
	//		}
	//	};

	//	private interface NodeHandler {
	//	void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents);
	//}
	//
	//private class IfHandler implements NodeHandler {
	//	public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//		String test = nodeToHandle.getStringAttribute("test");
	//		if (null == test) {
	//			throw new XmlParseException("<if> node test == null");
	//		}
	//		List<TangYuanNode> contents = parseDynamicTags(nodeToHandle);
	//		int                size     = contents.size();
	//		IfNode             ifNode   = null;
	//		if (1 == size) {
	//			ifNode = new IfNode(contents.get(0), new LogicalExprParser().parse(test));
	//		} else if (size > 1) {
	//			ifNode = new IfNode(new MixedNode(contents), new LogicalExprParser().parse(test));
	//		} else { // size == 0
	//			throw new XmlParseException("<if> node contents == null");
	//		}
	//		targetContents.add(ifNode);
	//	}
	//}

	//private class ElseIfHandler implements NodeHandler {
	//	public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//		if (0 == targetContents.size()) {
	//			throw new XmlParseException("<elseIf> node is not legal.");
	//		}
	//		TangYuanNode previousNode = targetContents.get(targetContents.size() - 1);
	//		if (!(previousNode instanceof IfNode)) {
	//			throw new XmlParseException("The node before the <elseIf> node must be an <if> node.");
	//		}
	//		String test = nodeToHandle.getStringAttribute("test");
	//		if (null == test) {
	//			throw new XmlParseException("<elseIf> node test == null");
	//		}
	//
	//		List<TangYuanNode> contents = parseDynamicTags(nodeToHandle);
	//		int                size     = contents.size();
	//
	//		IfNode             ifNode   = null;
	//		if (1 == size) {
	//			ifNode = new IfNode(contents.get(0), new LogicalExprParser().parse(test));
	//		} else if (size > 1) {
	//			ifNode = new IfNode(new MixedNode(contents), new LogicalExprParser().parse(test));
	//		} else {
	//			throw new XmlParseException("<elseIf> node contents == null");
	//		}
	//		((IfNode) previousNode).addElseIfNode(ifNode);
	//	}
	//}

	//private class ElseHandler implements NodeHandler {
	//	public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//		if (0 == targetContents.size()) {
	//			throw new XmlParseException("<else> node is not legal.");
	//		}
	//		TangYuanNode previousNode = targetContents.get(targetContents.size() - 1);
	//		if (!(previousNode instanceof IfNode)) {
	//			throw new XmlParseException("<else> node is not legal.");
	//		}
	//		List<TangYuanNode> contents = parseDynamicTags(nodeToHandle);
	//		int                size     = contents.size();
	//		IfNode             ifNode   = null;
	//		if (1 == size) {
	//			ifNode = new IfNode(contents.get(0), null);
	//		} else if (size > 1) {
	//			ifNode = new IfNode(new MixedNode(contents), null);
	//		} else {
	//			throw new XmlParseException("<else> node contents == null");
	//		}
	//		((IfNode) previousNode).addElseNode(ifNode);
	//	}
	//}

	//	private class SetVarHandler implements NodeHandler {
	//	public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//		// <setvar key="{x}" value="100" type="Integer" />
	//		String key    = StringUtils.trim(nodeToHandle.getStringAttribute("key")); // xml v
	//		String _value = StringUtils.trim(nodeToHandle.getStringAttribute("value")); // xml v
	//		String type   = StringUtils.trim(nodeToHandle.getStringAttribute("type")); // xml v
	//		if (!checkVar(key)) {
	//			throw new XmlParseException("<setvar> node key is not legal, should be {xxx}.");
	//		}
	//		key = getRealVal(key);
	//		Object  value    = null;
	//		boolean constant = true;
	//		if (checkVar(_value)) {
	//			constant = false;
	//			value = new NormalParser().parse(getRealVal(_value));
	//		} else {
	//			value = getSetVarValue(_value, type);
	//		}
	//		SetVarNode setVarNode = new SetVarNode(key, value, constant);
	//		targetContents.add(setVarNode);
	//	}
	//}

	//private class LogHandler implements NodeHandler {
	//	public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//		String message = StringUtils.trim(nodeToHandle.getStringAttribute("message")); // xml v
	//		String _level  = StringUtils.trim(nodeToHandle.getStringAttribute("level")); // xml c
	//		int    level   = 3;
	//		if (null != _level) {
	//			level = getLogLevel(_level);
	//		}
	//		LogNode logNode = new LogNode(level, message);
	//		targetContents.add(logNode);
	//	}
	//}

	//private class ReturnHandler implements NodeHandler {
	//	public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//		// Variable result = null;
	//		Object result  = null;
	//		String _result = StringUtils.trim(nodeToHandle.getStringAttribute("value"));
	//		if (null != _result) {
	//			if (checkVar(_result)) {
	//				result = new NormalParser().parse(getRealVal(_result));
	//			} else {
	//				result = parseValue(_result);
	//			}
	//		}
	//
	//		List<XmlNodeWrapper> properties = nodeToHandle.evalNodes("property");
	//		List<PropertyItem>   resultList = buildPropertyItem(properties, "return");
	//
	//		if (null != result && null != resultList) {
	//			throw new XmlParseException("<return> node in the result | property can only choose a way.");
	//		}
	//
	//		ReturnNode returnNode = new ReturnNode(result, resultList, serviceResultType);
	//		targetContents.add(returnNode);
	//	}
	//}

	//private class ThrowHandler implements NodeHandler {
	//	public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//		String test    = StringUtils.trim(nodeToHandle.getStringAttribute("test")); // xml v
	//		String code    = StringUtils.trim(nodeToHandle.getStringAttribute("code")); // xml v
	//		String message = StringUtils.trim(nodeToHandle.getStringAttribute("message"));
	//		String i18n    = StringUtils.trim(nodeToHandle.getStringAttribute("i18n"));
	//		if (null == test || null == code) {
	//			throw new XmlParseException("In the Exception node, the test, code attribute can not be empty.");
	//		}
	//		targetContents.add(new ExceptionNode(new LogicalExprParser().parse(test), Integer.parseInt(code), message, i18n));
	//	}
	//}

	//private class CallHandler implements NodeHandler {
	//	public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//		String serviceId = StringUtils.trim(nodeToHandle.getStringAttribute("service"));
	//		if (null == serviceId) {
	//			throw new XmlParseException("The service attribute in the call node can not be empty");
	//		}
	//
	//		// fix: 新增变量调用功能
	//		Object service = serviceId;
	//		if (checkVar(serviceId)) {
	//			service = new NormalParser().parse(getRealVal(serviceId));
	//		}
	//
	//		String   resultKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("resultKey")));
	//		String   _mode     = StringUtils.trim(nodeToHandle.getStringAttribute("mode"));// xml v
	//
	//		CallMode mode      = null;// 增加新的默认模式
	//		if (null != _mode) {
	//			mode = getCallMode(_mode);
	//		}
	//
	//		// String exResultKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("exResultKey")));
	//
	//		String               codeKey    = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("codeKey")));
	//		String               messageKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("messageKey")));
	//
	//		List<XmlNodeWrapper> properties = nodeToHandle.evalNodes("property");
	//		List<PropertyItem>   itemList   = buildPropertyItem(properties, "call");
	//
	//		// service id可以放在运行期间检查
	//		// targetContents.add(new CallNode(service, resultKey, mode, itemList, exResultKey));
	//		targetContents.add(new CallNode(service, resultKey, mode, itemList, codeKey, messageKey));
	//	}
	//}

	//	private void buildRefNode(List<XmlNodeWrapper> contexts) {
	//		for (XmlNodeWrapper context : contexts) {
	//			String id     = StringUtils.trim(context.getStringAttribute("id")); // xml V
	//			String fullId = getFullId(id);
	//			if (null == this.context.getXmlContext().getIntegralRefMap().get(fullId)) {
	//				TangYuanNode sqlNode = parseNode(context, false);
	//				if (null != sqlNode) {
	//					this.context.getXmlContext().getIntegralRefMap().put(fullId, sqlNode);
	//					log.info("add <sql> node: " + fullId);
	//				}
	//			} else {
	//				throw new XmlParseException("Duplicate <sql> nodes: " + id);
	//			}
	//		}
	//	}
	//	// 扫描段
	//	private void buildSegmentNode(List<XmlNodeWrapper> contexts) {
	//		for (XmlNodeWrapper context : contexts) {
	//			String id     = StringUtils.trim(context.getStringAttribute("id")); // xml V
	//			String fullId = getFullId(id);
	//			if (null == this.context.getXmlContext().getIntegralRefMap().get(fullId)) {
	//				TangYuanNode sqlNode = new SegmentNode(context);
	//				if (null != sqlNode) {
	//					this.context.getXmlContext().getIntegralRefMap().put(fullId, sqlNode);
	//					log.info("add <segment> node: " + fullId);
	//				}
	//			} else {
	//				throw new XmlParseException("Duplicate <segment> nodes: " + id);
	//			}
	//		}
	//	}

	//	private Log             log                 = LogFactory.getLog(getClass());
	//	private XmlNodeWrapper  root                = null;
	//	private XmlMongoContext context             = null;
	//	private String          dsKeyWithSqlService = null;
	//	private Class<?>        serviceResultType   = null;
	//	@Override
	//	public Log getLog() {
	//		return this.log;
	//	}
	//	@Override
	//	public void clean() {
	//		super.clean();
	//	}
	//	@Override
	//	public void setContext(XmlNodeWrapper root, XmlContext context) {
	//		this.context = (XmlMongoContext) context;
	//		this.root = root;
	//		this.ns = this.root.getStringAttribute("ns", "");
	//		//  需要增加版本号
	//		if (this.ns.length() > 0) {
	//			this.context.getXmlContext().checkNs(this.ns);
	//		}
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

	//	/**
	//	 * 解析: ID:xxx; key:xxx; time:1000; ignore:a,b <br />
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

	//	/**
	//	 * 解析: ID:xxx; key:xxx; ignore=a,b <br />
	//	 * 解析: ID:xxx; key:xxx;
	//	 */
	//	private CacheCleanVo parseCacheClean(String cacheUse, String service) {
	//		CacheCleanVo cacheCleanVo = null;
	//		String[]     array        = cacheUse.split(";");
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
	//			cacheCleanVo = new CacheCleanVo(cache, key, service);
	//		}
	//		return cacheCleanVo;
	//	}
	//	protected String getFullId(String id) {
	//		return TangYuanUtil.getQualifiedName(this.ns, id, null, TangYuanContainer.getInstance().getNsSeparator());
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
	//	List<TangYuanNode> contents = new ArrayList<TangYuanNode>();
	//	NodeList           children = node.getNode().getChildNodes();
	//	for (int i = 0; i < children.getLength(); i++) {
	//		XmlNodeWrapper child = node.newXMlNode(children.item(i));
	//		if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
	//			String data = child.getStringBody("");
	//			if (isEmpty(data)) {
	//				continue;
	//			}
	//			// 使用新的sqlText节点
	//			contents.add(new MongoTextNode(data));
	//			// log.info("-----------data:" + data);
	//		} else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) {
	//			String      nodeName = child.getNode().getNodeName();
	//			// log.info("-----------name:" + nodeName);
	//			NodeHandler handler  = nodeHandlers.get(nodeName);
	//			if (handler == null) {
	//				throw new XmlParseException("Unknown element <" + nodeName + "> in SQL statement.");
	//			}
	//			handler.handleNode(child, contents);
	//		}
	//	}
	//	return contents;
	//}
	// class SelectResult {
	// Class<?> resultType;
	// MappingVo resultMap;
	//
	// SelectResult(Class<?> resultType, MappingVo resultMap) {
	// this.resultType = resultType;
	// this.resultMap = resultMap;
	// }
	// }

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
	//			resultMap = this.context.getMappingVoMap().get(_resultMap);
	//			if (null == resultMap) {
	//				throw new XmlParseException("Non-existent resultMap: " + _resultMap);
	//			}
	//			// 检测是否冲突
	//			if (null != resultMap.getBeanClass() && resultType != resultMap.getBeanClass()) {
	//				throw new XmlParseException("resultMap[" + resultMap.getBeanClass() + "] and resultType[" + resultType + "]类型冲突");
	//			}
	//		} else if (null == _resultType && null != _resultMap) {
	//			resultMap = this.context.getMappingVoMap().get(_resultMap);
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
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
	//				existingService(id);
	//
	//				String       _resultType  = StringUtils.trim(context.getStringAttribute("resultType"));
	//				String       _resultMap   = StringUtils.trim(context.getStringAttribute("resultMap"));
	//				SelectResult selectResult = parseSelectResult(_resultType, _resultMap);
	//
	//				String       _fetchSize   = StringUtils.trim(context.getStringAttribute("fetchSize")); // xml
	//				// validation
	//				Integer      fetchSize    = null;
	//				if (null != _fetchSize) {
	//					fetchSize = Integer.valueOf(_fetchSize);
	//				}
	//				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				// ID:xxx; key:xxx; time=1000; ignore=a,b
	//				String     _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//				CacheUseVo cacheUse  = null;
	//				if (null != _cacheUse && _cacheUse.length() > 0) {
	//					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//				}
	//
	//				MongoSelectSetNode selectSetNode = new MongoSelectSetNode(id, ns, getFullId(id), selectResult.resultType, selectResult.resultMap, dsKey, fetchSize, sqlNode,
	//						cacheUse);
	//
	//				list.add(selectSetNode);
	//			}
	//		}
	//		return list;
	//	}

	//	private List<AbstractServiceNode> buildSelectOneNodes(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			TangYuanNode sqlNode = parseNode(context, false);
	//			if (null != sqlNode) {
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
	//				existingService(id);
	//
	//				String       _resultType  = StringUtils.trim(context.getStringAttribute("resultType"));
	//				String       _resultMap   = StringUtils.trim(context.getStringAttribute("resultMap"));
	//				SelectResult selectResult = parseSelectResult(_resultType, _resultMap);
	//
	//				String       dsKey        = StringUtils.trim(context.getStringAttribute("dsKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				String     _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//				CacheUseVo cacheUse  = null;
	//				if (null != _cacheUse && _cacheUse.length() > 0) {
	//					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//				}
	//
	//				MongoSelectOneNode selectOneNode = new MongoSelectOneNode(id, ns, getFullId(id), selectResult.resultType, selectResult.resultMap, dsKey, sqlNode, cacheUse);
	//
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
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
	//				existingService(id);
	//
	//				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				String     _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//				CacheUseVo cacheUse  = null;
	//				if (null != _cacheUse && _cacheUse.length() > 0) {
	//					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//				}
	//
	//				MongoSelectVarNode selectVarNode = new MongoSelectVarNode(id, ns, getFullId(id), dsKey, sqlNode, cacheUse);
	//				list.add(selectVarNode);
	//			}
	//		}
	//		return list;
	//	}

	//	private List<AbstractServiceNode> buildInsertNodes(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			TangYuanNode sqlNode = parseNode(context, false);
	//			if (null != sqlNode) {
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
	//				existingService(id);
	//
	//				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				String       _cacheClean = StringUtils.trim(context.getStringAttribute("cacheClean"));
	//				CacheCleanVo cacheClean  = null;
	//				if (null != _cacheClean && _cacheClean.length() > 0) {
	//					cacheClean = parseCacheClean(_cacheClean, getFullId(id));
	//				}
	//
	//				MongoInsertNode insertNode = new MongoInsertNode(id, ns, getFullId(id), dsKey, sqlNode, cacheClean);
	//
	//				list.add(insertNode);
	//			}
	//		}
	//		return list;
	//	}

	//	private List<AbstractServiceNode> buildUpdateNodes(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			TangYuanNode sqlNode = parseNode(context, false);
	//			if (null != sqlNode) {
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
	//																				// validation
	//				existingService(id);
	//
	//				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				String       _cacheClean = StringUtils.trim(context.getStringAttribute("cacheClean"));
	//				CacheCleanVo cacheClean  = null;
	//				if (null != _cacheClean && _cacheClean.length() > 0) {
	//					cacheClean = parseCacheClean(_cacheClean, getFullId(id));
	//				}
	//
	//				MongoUpdateNode updateNode = new MongoUpdateNode(id, ns, getFullId(id), dsKey, sqlNode, cacheClean);
	//				list.add(updateNode);
	//			}
	//		}
	//		return list;
	//	}

	//	private List<AbstractServiceNode> buildDeleteNodes(List<XmlNodeWrapper> contexts) {
	//	List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//	for (XmlNodeWrapper context : contexts) {
	//		TangYuanNode sqlNode = parseNode(context, false);
	//		if (null != sqlNode) {
	//			String id = StringUtils.trim(context.getStringAttribute("id")); // xml
	//																			// validation
	//			existingService(id);
	//
	//			String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
	//			dsKey = checkDsKey(dsKey, id);
	//
	//			String       _cacheClean = StringUtils.trim(context.getStringAttribute("cacheClean"));
	//			CacheCleanVo cacheClean  = null;
	//			if (null != _cacheClean && _cacheClean.length() > 0) {
	//				cacheClean = parseCacheClean(_cacheClean, getFullId(id));
	//			}
	//
	//			MongoDeleteNode deleteNode = new MongoDeleteNode(id, ns, getFullId(id), dsKey, sqlNode, cacheClean);
	//			list.add(deleteNode);
	//		}
	//	}
	//	return list;
	//}

	//	private List<AbstractServiceNode> buildCommandNodes(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//
	//			TangYuanNode sqlNode = parseNode(context, false);
	//			if (null == sqlNode) {
	//				continue;
	//			}
	//
	//			String id = StringUtils.trim(context.getStringAttribute("id")); // xml
	//			existingService(id);
	//
	//			String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
	//			dsKey = checkDsKey(dsKey, id);
	//
	//			String       _resultType  = StringUtils.trim(context.getStringAttribute("resultType"));
	//			String       _resultMap   = StringUtils.trim(context.getStringAttribute("resultMap"));
	//			SelectResult selectResult = parseSelectResult(_resultType, _resultMap);
	//
	//			String       _cacheUse    = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//			CacheUseVo   cacheUse     = null;
	//			if (null != _cacheUse && _cacheUse.length() > 0) {
	//				cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//			}
	//
	//			String       _cacheClean = StringUtils.trim(context.getStringAttribute("cacheClean"));
	//			CacheCleanVo cacheClean  = null;
	//			if (null != _cacheClean && _cacheClean.length() > 0) {
	//				cacheClean = parseCacheClean(_cacheClean, getFullId(id));
	//			}
	//
	//			MongoCommandNode commandNode = new MongoCommandNode(id, ns, getFullId(id), dsKey, sqlNode, cacheUse, cacheClean, selectResult.resultType, selectResult.resultMap);
	//			list.add(commandNode);
	//		}
	//		return list;
	//	}

	//	private List<AbstractServiceNode> buildSqlServiceNodes(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			String id = StringUtils.trim(context.getStringAttribute("id")); // xml
	//			existingService(id);
	//
	//			String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
	//			if (null != dsKey && !MongoComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
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
	//				MongoServiceNode serviceNode = new MongoServiceNode(id, this.ns, getFullId(id), dsKey, sqlNode, cacheUse, cacheClean, this.serviceResultType);
	//				list.add(serviceNode);
	//			}
	//		}
	//		return list;
	//	}
	//	private class IncludeHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			String       refKey  = nodeToHandle.getStringAttribute("ref"); // xml V
	//			TangYuanNode refNode = context.getXmlContext().getIntegralRefMap().get(refKey);
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
	//
	//			if (null == sqlNode) {
	//				if (null == index) {
	//					index = "i";
	//				}
	//				sqlNode = new MongoTextNode("#{" + collection + "[" + index + "]}");
	//			}
	//
	//			ForEachNode forEachNode = new MongoForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator);
	//			targetContents.add(forEachNode);
	//		}
	//	}
	//	private String checkDsKey(String dsKey, String service) {
	//		if (null == dsKey) {
	//			dsKey = context.getDefaultDataSource();
	//			if (null == dsKey) {
	//				throw new XmlParseException("service[" + service + "] uses an invalid dsKey: " + dsKey);
	//			}
	//		} else {
	//			if (!MongoComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
	//				throw new XmlParseException("service[" + service + "] uses an invalid dsKey: " + dsKey);
	//			}
	//		}
	//		return dsKey;
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
	//				InternalMongoSelectSetNode selectSetNode = new InternalMongoSelectSetNode(dsKey, resultKey, sqlNode, serviceResultType, fetchSize, cacheUse);
	//				targetContents.add(selectSetNode);
	//			}
	//		}
	//	}

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
	//				InternalMongoSelectOneNode selectOneNode = new InternalMongoSelectOneNode(dsKey, resultKey, sqlNode, serviceResultType, cacheUse);
	//				targetContents.add(selectOneNode);
	//			}
	//		}
	//	}

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
	//				InternalMongoSelectVarNode selectVarNode = new InternalMongoSelectVarNode(dsKey, resultKey, sqlNode, cacheUse);
	//				targetContents.add(selectVarNode);
	//			}
	//		}
	//	}
	//	private class DeleteHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
	//			if (null != sqlNode) {
	//				String dsKey = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
	//				if (null == dsKey) {
	//					dsKey = dsKeyWithSqlService;
	//				} else {
	//					checkInnerDsKey(dsKey, "delete");
	//				}
	//				String resultKey = StringUtils.trim(nodeToHandle.getStringAttribute("rowCount"));
	//				if (null != resultKey) {
	//					if (!checkVar(resultKey)) {
	//						throw new XmlParseException("<delete> rowCount is not legal, should be {xxx}.");
	//					}
	//					resultKey = getRealVal(resultKey);
	//				}
	//
	//				String       _cacheClean = StringUtils.trim(nodeToHandle.getStringAttribute("cacheClean"));
	//				CacheCleanVo cacheClean  = null;
	//				if (null != _cacheClean && _cacheClean.length() > 0) {
	//					cacheClean = parseCacheClean(_cacheClean, "");
	//				}
	//
	//				InternalMongoDeleteNode deleteNode = new InternalMongoDeleteNode(dsKey, resultKey, sqlNode, cacheClean);
	//				targetContents.add(deleteNode);
	//			}
	//		}
	//	}
	//	private class InsertHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
	//			if (null != sqlNode) {
	//				String dsKey = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
	//				if (null == dsKey) {
	//					dsKey = dsKeyWithSqlService;
	//				} else {
	//					checkInnerDsKey(dsKey, "insert");
	//				}
	//
	//				String resultKey = StringUtils.trim(nodeToHandle.getStringAttribute("resultKey"));
	//				if (null != resultKey) {
	//					if (!checkVar(resultKey)) {
	//						throw new XmlParseException("<insert> resultKey is not legal, should be {xxx}");
	//					}
	//					resultKey = getRealVal(resultKey);
	//				}
	//
	//				String       _cacheClean = StringUtils.trim(nodeToHandle.getStringAttribute("cacheClean"));
	//				CacheCleanVo cacheClean  = null;
	//				if (null != _cacheClean && _cacheClean.length() > 0) {
	//					cacheClean = parseCacheClean(_cacheClean, "");
	//				}
	//
	//				InternalMongoInsertNode insertNode = new InternalMongoInsertNode(dsKey, resultKey, sqlNode, cacheClean);
	//
	//				targetContents.add(insertNode);
	//			}
	//		}
	//	}
	//	private class CommandHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
	//
	//			String       dsKey   = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
	//			if (null == dsKey) {
	//				dsKey = dsKeyWithSqlService;
	//			} else {
	//				checkInnerDsKey(dsKey, "command");
	//			}
	//
	//			String rowCount = StringUtils.trim(nodeToHandle.getStringAttribute("rowCount"));
	//			if (null != rowCount) {
	//				if (!checkVar(rowCount)) {
	//					throw new XmlParseException("<command> rowCount is not legal, should be {xxx}.");
	//				}
	//				rowCount = getRealVal(rowCount);
	//			}
	//
	//			String resultKey = StringUtils.trim(nodeToHandle.getStringAttribute("resultKey"));
	//			if (null != resultKey) {
	//				if (!checkVar(resultKey)) {
	//					throw new XmlParseException("<command> resultKey is not legal, should be {xxx}");
	//				}
	//				resultKey = getRealVal(resultKey);
	//			}
	//
	//			if (null == resultKey) {
	//				resultKey = rowCount;
	//			}
	//
	//			String     _cacheUse = StringUtils.trim(nodeToHandle.getStringAttribute("cacheUse"));
	//			CacheUseVo cacheUse  = null;
	//			if (null != _cacheUse && _cacheUse.length() > 0) {
	//				cacheUse = parseCacheUse(_cacheUse, "");
	//			}
	//
	//			String       _cacheClean = StringUtils.trim(nodeToHandle.getStringAttribute("cacheClean"));
	//			CacheCleanVo cacheClean  = null;
	//			if (null != _cacheClean && _cacheClean.length() > 0) {
	//				cacheClean = parseCacheClean(_cacheClean, "");
	//			}
	//
	//			InternalMongoCommandNode commandNode = new InternalMongoCommandNode(dsKey, resultKey, sqlNode, serviceResultType, cacheUse, cacheClean);
	//			targetContents.add(commandNode);
	//		}
	//	}
	//	private class UpdateHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
	//			if (null != sqlNode) {
	//				String dsKey = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
	//				if (null == dsKey) {
	//					dsKey = dsKeyWithSqlService;
	//				} else {
	//					checkInnerDsKey(dsKey, "update");
	//				}
	//				String resultKey = StringUtils.trim(nodeToHandle.getStringAttribute("rowCount"));
	//				if (null != resultKey) {
	//					if (!checkVar(resultKey)) {
	//						throw new XmlParseException("<update> rowCount is not legal, should be {xxx}.");
	//					}
	//					resultKey = getRealVal(resultKey);
	//				}
	//
	//				String       _cacheClean = StringUtils.trim(nodeToHandle.getStringAttribute("cacheClean"));
	//				CacheCleanVo cacheClean  = null;
	//				if (null != _cacheClean && _cacheClean.length() > 0) {
	//					cacheClean = parseCacheClean(_cacheClean, "");
	//				}
	//
	//				InternalMongoUpdateNode updateNode = new InternalMongoUpdateNode(dsKey, resultKey, sqlNode, cacheClean);
	//				targetContents.add(updateNode);
	//			}
	//		}
	//	}

}
