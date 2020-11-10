package org.xson.tangyuan.mongo.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.mapping.ColumnValueHandler;
import org.xson.tangyuan.mapping.ColumnVo;
import org.xson.tangyuan.mapping.MappingHandler;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlMongoMapperBuilder2 extends DefaultXmlComponentBuilder {

	private XmlMongoContext                 componentContext      = null;
	private Map<String, MappingHandler>     mappingClassMap       = new HashMap<String, MappingHandler>();
	private Map<String, ColumnValueHandler> columnHandlerClassMap = new HashMap<String, ColumnValueHandler>();
	private Map<String, MappingVo>          mappingVoMap          = new HashMap<String, MappingVo>();

	@Override
	protected void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing.type", "mapper", resource));
		this.componentContext = (XmlMongoContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "mapper", true);
		this.configurationElement();
		this.componentContext.setMappingVoMap(mappingVoMap);
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();
		this.mappingClassMap = null;
		this.columnHandlerClassMap = null;
		this.mappingVoMap = null;
	}

	private void configurationElement() throws Throwable {
		buildMappingClassNode(this.root.evalNodes("mappingClass"));
		buildColumnHandlerClassNode(this.root.evalNodes("columnHandlerClass"));
		buildResultMapNode(this.root.evalNodes("resultMap"));
	}

	private void buildMappingClassNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int    size    = contexts.size();
		String tagName = "mappingClass";
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode     = contexts.get(i);
			String         id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String         className = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));
			MappingHandler handler   = getInstanceForName(className, MappingHandler.class, lang("xml.class.impl.interface", className, MappingHandler.class.getName()));
			if (mappingClassMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			mappingClassMap.put(id, handler);
			log.info(lang("add.tag.class"), tagName, className);
		}
	}

	private void buildColumnHandlerClassNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int    size    = contexts.size();
		String tagName = "columnHandlerClass";
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper     xNode     = contexts.get(i);
			String             id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String             className = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));
			ColumnValueHandler handler   = getInstanceForName(className, ColumnValueHandler.class, lang("xml.class.impl.interface", className, ColumnValueHandler.class.getName()));
			if (columnHandlerClassMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			columnHandlerClassMap.put(id, handler);
			log.info(lang("add.tag.class"), tagName, className);
		}
	}

	private void buildResultMapNode(List<XmlNodeWrapper> contexts) {
		int    size    = contexts.size();
		String tagName = "resultMap";
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode         = contexts.get(i);

			String         id            = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String         type          = getStringFromAttr(xNode, "type");
			String         rule          = getStringFromAttr(xNode, "rule");
			String         columnHandler = getStringFromAttr(xNode, "columnHandler");

			if (mappingVoMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			Class<?> beanClass = null;
			if (null == type) {
				// beanClass = Map.class;// 这里需要看resultType
			} else if (!"*".equals(type)) {
				beanClass = ClassUtils.forName(type);
			}

			//			MappingHandler        handler    = mappingClassMap.get(rule);

			MappingHandler     handler             = null;
			ColumnValueHandler gColumnValueHandler = null;
			if (null != rule) {
				handler = mappingClassMap.get(rule);
				if (null == handler) {
					throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", rule, "rule", tagName, this.resource));
				}
			}
			if (null != columnHandler) {
				gColumnValueHandler = this.columnHandlerClassMap.get(columnHandler);
				if (null == gColumnValueHandler) {
					throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", columnHandler, "columnHandler", tagName, this.resource));
				}
			}

			Map<String, ColumnVo> columnMap  = new HashMap<String, ColumnVo>();
			List<XmlNodeWrapper>  properties = xNode.evalNodes("result");
			for (XmlNodeWrapper propertyNode : properties) {
				String             column   = getStringFromAttr(propertyNode, "column", lang("xml.tag.attribute.empty", "column", tagName + ".property", this.resource));
				String             property = getStringFromAttr(propertyNode, "property");

				//				String             cvhId    = getStringFromAttr(propertyNode, "columnHandler");
				//				ColumnValueHandler cvh      = this.columnHandlerClassMap.get(cvhId);

				ColumnValueHandler cvh      = null;
				String             cvhId    = getStringFromAttr(propertyNode, "columnHandler");
				if (null != cvhId) {
					cvh = this.columnHandlerClassMap.get(cvhId);
					if (null == cvh) {
						throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", columnHandler, "columnHandler", tagName + ".result", this.resource));
					}
				}

				ColumnVo columnVo = new ColumnVo(property, cvh);
				columnMap.put(column, columnVo);
			}

			if (null == handler && null == gColumnValueHandler && 0 == columnMap.size()) {
				throw new XmlParseException(lang("xml.tag.attribute.empty", "rule && columnHandler && result", tagName, this.resource));
			}

			MappingVo mVo = new MappingVo(id, type, beanClass, handler, gColumnValueHandler, columnMap);
			mappingVoMap.put(id, mVo);
			log.info(lang("add.tag.class"), tagName, id);
		}

		//			Map<String, String>  columnMap  = new HashMap<String, String>();
		//			List<XmlNodeWrapper> properties = xNode.evalNodes("result");
		//			for (XmlNodeWrapper propertyNode : properties) {
		//				String column   = getStringFromAttr(propertyNode, "column", lang("xml.tag.attribute.empty", "column", tagName + ".property", this.resource));
		//				String property = getStringFromAttr(propertyNode, "property", lang("xml.tag.attribute.empty", "property", tagName + ".property", this.resource));
		//				columnMap.put(column, property);
		//			}
	}

	//	private Log							log				= LogFactory.getLog(getClass());
	//	private XPathParser					xPathParser		= null;
	//	private Map<String, MappingHandler>	mappingClassMap	= new HashMap<String, MappingHandler>();
	//	private Map<String, MappingVo>		mappingVoMap	= new HashMap<String, MappingVo>();

	//	public void parse(XmlMongoContext context) throws Throwable {
	//		configurationElement(xPathParser.evalNode("/mapper"));
	//		context.setMappingVoMap(mappingVoMap);
	//		mappingClassMap = null;
	//		mappingVoMap = null;
	//	}
	//	private void configurationElement(XmlNodeWrapper context) throws Throwable {
	//		buildMappingClassNodes(context.evalNodes("mappingClass"));
	//		buildResultMapNodes(context.evalNodes("resultMap"));
	//	}
	//	public XmlMongoMapperBuilder(InputStream inputStream) {
	//		this.xPathParser = new XPathParser(inputStream);
	//	}
	//	private void buildMappingClassNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		int size = contexts.size();
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode = contexts.get(i);
	//			String         id    = StringUtils.trim(xNode.getStringAttribute("id"));
	//			if (null != mappingClassMap.get(id)) {
	//				throw new XmlParseException("Duplicate mappingClass id: " + id);
	//			}
	//			String   className    = StringUtils.trim(xNode.getStringAttribute("class"));
	//			Class<?> handlerClass = ClassUtils.forName(className);
	//			if (!MappingHandler.class.isAssignableFrom(handlerClass)) {
	//				throw new XmlParseException("mapping class not implement the MappingHandler interface: " + className);
	//			}
	//			MappingHandler handler = (MappingHandler) handlerClass.newInstance();
	//			mappingClassMap.put(id, handler);
	//			log.info("add mapping handler: " + className);
	//		}
	//	}

	//	private void buildResultMapNodes(List<XmlNodeWrapper> contexts) {
	//		int size = contexts.size();
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode = contexts.get(i);
	//			String         id    = StringUtils.trim(xNode.getStringAttribute("id"));// xmlV
	//			if (mappingVoMap.containsKey(id)) {
	//				throw new XmlParseException("Duplicate mapping:" + id);
	//			}
	//			String   type      = StringUtils.trim(xNode.getStringAttribute("type"));
	//			Class<?> beanClass = null;
	//			if (null == type) {
	//				// beanClass = Map.class;// 这里需要看resultType
	//			} else if (!"*".equals(type)) {
	//				beanClass = ClassUtils.forName(type);
	//			}
	//			String               rule       = StringUtils.trim(xNode.getStringAttribute("rule"));
	//			MappingHandler       handler    = mappingClassMap.get(rule);
	//			Map<String, String>  columnMap  = new HashMap<String, String>();
	//			List<XmlNodeWrapper> properties = xNode.evalNodes("result");
	//			for (XmlNodeWrapper propertyNode : properties) {
	//				columnMap.put(StringUtils.trim(propertyNode.getStringAttribute("column")), StringUtils.trim(propertyNode.getStringAttribute("property")));
	//			}
	//			if (null == handler && 0 == columnMap.size()) {
	//				throw new XmlParseException("<resultMap> rule and result can not be empty: " + id);
	//			}
	//			MappingVo mVo = new MappingVo(id, type, beanClass, handler, columnMap);
	//			mappingVoMap.put(id, mVo);
	//			log.info("add resultMap: " + id);
	//		}
	//	}
}
