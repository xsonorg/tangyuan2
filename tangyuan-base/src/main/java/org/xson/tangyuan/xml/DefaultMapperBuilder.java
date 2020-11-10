package org.xson.tangyuan.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.mapping.ColumnValueHandler;
import org.xson.tangyuan.mapping.ColumnVo;
import org.xson.tangyuan.mapping.MappingHandler;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.util.ClassUtils;

/**
 * Mapper解析基类
 */
public class DefaultMapperBuilder extends DefaultXmlComponentBuilder {

	protected Map<String, MappingHandler>     mappingClassMap       = new HashMap<String, MappingHandler>();
	protected Map<String, ColumnValueHandler> columnHandlerClassMap = new HashMap<String, ColumnValueHandler>();
	protected Map<String, MappingVo>          mappingVoMap          = new HashMap<String, MappingVo>();

	@Override
	protected void clean() {
		super.clean();
		this.mappingClassMap = null;
		this.columnHandlerClassMap = null;
		this.mappingVoMap = null;
	}

	protected void buildMappingClassNode(List<XmlNodeWrapper> contexts) throws Throwable {
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

	protected void buildColumnHandlerClassNode(List<XmlNodeWrapper> contexts) throws Throwable {
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

	protected void buildResultMapNode(List<XmlNodeWrapper> contexts) {
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
				String             cvhId    = getStringFromAttr(propertyNode, "columnHandler");
				ColumnValueHandler cvh      = null;
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
	}

}
