package org.xson.tangyuan.web.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.ResponseHandler;
import org.xson.tangyuan.web.WebComponent;
import org.xson.tangyuan.web.convert.DataConverterVo;
import org.xson.tangyuan.web.convert.Form2XCOByRuleConverter;
import org.xson.tangyuan.web.convert.Form2XCOConverter;
import org.xson.tangyuan.web.convert.JSON2XCOByRuleConverter;
import org.xson.tangyuan.web.convert.JSON2XCOConverter;
import org.xson.tangyuan.web.convert.JSONDataConverter;
import org.xson.tangyuan.web.convert.MixedDataConverter;
import org.xson.tangyuan.web.convert.NothingConverter;
import org.xson.tangyuan.web.convert.URL2JSONConverter;
import org.xson.tangyuan.web.convert.URL2XCOByRuleConverter;
import org.xson.tangyuan.web.convert.URL2XCOConverter;
import org.xson.tangyuan.web.convert.XCODataConverter;
import org.xson.tangyuan.web.convert.XCOXSONDataConverter;
import org.xson.tangyuan.web.handler.ResponseConvertVo;
import org.xson.tangyuan.web.xml.vo.InterceptVo;
import org.xson.tangyuan.web.xml.vo.InterceptVo.InterceptType;
import org.xson.tangyuan.web.xml.vo.MethodObject;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlWebComponentBuilder extends DefaultXmlComponentBuilder {

	private XmlWebContext componentContext = null;

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		this.componentContext = (XmlWebContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "web-component", true);
		this.configurationElement();
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();
		this.componentContext = null;
	}

	private void configurationElement() throws Throwable {
		// 解析配置项目
		buildConfigNode(this.root.evalNodes("config-property"), WebComponent.getInstance());

		buildBeanNode(this.root.evalNodes("bean"));

		registerSystemConverter();
		buildConverterNode(this.root.evalNodes("converter"));
		buildConverterGroupNode(this.root.evalNodes("converter-group"));

		buildDataConverterNode(this.root.evalNodes("data-converte"));

		buildInterceptNode(this.root.evalNodes("assembly"), InterceptType.ASSEMBLY, "assembly");
		buildInterceptNode(this.root.evalNodes("before"), InterceptType.BEFORE, "before");
		buildInterceptNode(this.root.evalNodes("after"), InterceptType.AFTER, "after");
		// 增加响应转换器
		buildResponseConvertNode(this.root.evalNodes("response"));
		// 解析plugin
		buildPluginNode(this.root.evalNodes("plugin"));
	}

	private void buildBeanNode(List<XmlNodeWrapper> contexts) throws Throwable {
		// <bean id="" class="" />
		String tagName = "bean";
		for (XmlNodeWrapper xNode : contexts) {
			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String className = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));

			if (this.componentContext.getBeanIdMap().containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			Object bean = this.componentContext.getBeanClassMap().get(className);
			if (null == bean) {
				bean = getInstanceForName(className, null, null);
				this.componentContext.getBeanClassMap().put(className, bean);
			}
			this.componentContext.getBeanIdMap().put(id, bean);

			log.info(lang("add.tag.class"), tagName, className);
		}
	}

	private void registerSystemConverter() {
		// 添加系统自带转换器
		this.componentContext.getConverterIdMap().put("@no", NothingConverter.instance);
		// xco
		this.componentContext.getConverterIdMap().put("@xco", XCODataConverter.instance);
		this.componentContext.getConverterIdMap().put("@xco-xson", XCOXSONDataConverter.instance);
		// xco:form
		this.componentContext.getConverterIdMap().put("@form2xco", Form2XCOConverter.instance);
		this.componentContext.getConverterIdMap().put("@form2xcoByRule", Form2XCOByRuleConverter.instance);
		// xco:url
		this.componentContext.getConverterIdMap().put("@url2xco", URL2XCOConverter.instance);
		this.componentContext.getConverterIdMap().put("@url2xcoByRule", URL2XCOByRuleConverter.instance);
		// xco:json
		this.componentContext.getConverterIdMap().put("@json2xco", JSON2XCOConverter.instance);
		this.componentContext.getConverterIdMap().put("@json2xcoByRule", JSON2XCOByRuleConverter.instance);
		// json
		this.componentContext.getConverterIdMap().put("@json", JSONDataConverter.instance);
		this.componentContext.getConverterIdMap().put("@url2json", URL2JSONConverter.instance);

		String tagName = "converter";
		log.info(lang("add.tag", tagName, "@no"));
		log.info(lang("add.tag", tagName, "@xco"));
		log.info(lang("add.tag", tagName, "@xco-xson"));
		log.info(lang("add.tag", tagName, "@form2xco"));
		log.info(lang("add.tag", tagName, "@form2xcoByRule"));
		log.info(lang("add.tag", tagName, "@url2xco"));
		log.info(lang("add.tag", tagName, "@url2xcoByRule"));
		log.info(lang("add.tag", tagName, "@json2xco"));
		log.info(lang("add.tag", tagName, "@json2xcoByRule"));
		log.info(lang("add.tag", tagName, "@json"));
		log.info(lang("add.tag", tagName, "@url2json"));

		// this.componentContext.getConverterIdMap().put("@rule", RuleDataConverter.instance);
		// this.componentContext.getConverterIdMap().put("@xco_rest_uri", XCORESTURIDataConverter.instance);
		// this.componentContext.getConverterIdMap().put("@xco_rest_uri_body", XCORESTURIBodyDataConverter.instance);
		// this.componentContext.getConverterIdMap().put("@json_rest_uri", JSONRESTURIDataConverter.instance);
		// this.componentContext.getConverterIdMap().put("@json_rest_uri_body", JSONRESTURIBodyDataConverter.instance);
	}

	private void buildConverterNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "converter";
		for (XmlNodeWrapper xNode : contexts) {
			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String className = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));

			if (this.componentContext.getConverterIdMap().containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			DataConverter bean = this.componentContext.getConverterClassMap().get(className);
			if (null == bean) {
				bean = getInstanceForName(className, DataConverter.class, lang("xml.class.impl.interface", className, DataConverter.class.getName()));
				this.componentContext.getConverterClassMap().put(className, bean);
			}
			this.componentContext.getConverterIdMap().put(id, bean);
			// log.info(lang("add.tag.class"), tagName, className);
			log.info(lang("add.tag", tagName, id));
		}
	}

	private void buildConverterGroupNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "converter-group";
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			if (this.componentContext.getConverterIdMap().containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			List<DataConverter> converters = new ArrayList<DataConverter>();
			List<XmlNodeWrapper> converterRefList = xNode.evalNodes("converter");
			for (XmlNodeWrapper converterNode : converterRefList) {
				String ref = getStringFromAttr(converterNode, "ref", lang("xml.tag.attribute.empty", "ref", tagName + ".converter", this.resource));
				DataConverter converter = this.componentContext.getConverterIdMap().get(ref);
				if (null == converter) {
					throw new XmlParseException(
							lang("xml.tag.attribute.reference.id.invalid", ref, id, "ref", tagName + ".converter", this.resource));
				}
				converters.add(converter);
			}

			this.componentContext.getConverterIdMap().put(id, new MixedDataConverter(converters));
			log.info(lang("add.tag", tagName, id));
		}
	}

	private void buildInterceptNode(List<XmlNodeWrapper> contexts, InterceptType type, String tagName) throws Throwable {

		List<InterceptVo> interceptList = null;
		Map<String, String> callMap = null;

		if (InterceptType.ASSEMBLY == type) {
			interceptList = this.componentContext.getAssemblyList();
			callMap = this.componentContext.getAssemblyMap();
		} else if (InterceptType.BEFORE == type) {
			interceptList = this.componentContext.getBeforeList();
			callMap = this.componentContext.getBeforeMap();
		} else if (InterceptType.AFTER == type) {
			interceptList = this.componentContext.getAfterList();
			callMap = this.componentContext.getAfterMap();
		}

		for (XmlNodeWrapper xNode : contexts) {

			String call = getStringFromAttr(xNode, "call", lang("xml.tag.attribute.empty", "call", tagName, this.resource));
			int order = getIntFromAttr(xNode, "order", WebComponent.getInstance().getOrder());

			if (callMap.containsKey(call)) {
				throw new XmlParseException(lang("xml.tag.repeated", call, tagName, this.resource));
			}

			// MethodObject mo = getMethodObject(call);
			MethodObject mo = this.componentContext.getMethodObject(call);

			List<String> includeList = getBodyList(xNode.evalNodes("include"));
			List<String> excludeList = getBodyList(xNode.evalNodes("exclude"));
			if (null == includeList && null == excludeList) {
				// throw new XmlParseException("Intercept missing <include|exclude>: " + call);
				throw new XmlParseException(lang("xml.tag.sub.miss", "include|exclude", tagName, this.resource));
			}

			InterceptVo baVo = new InterceptVo(mo, order, includeList, excludeList);

			interceptList.add(baVo);
			callMap.put(call, call);

			// log.info("Add <assembly> :" + call);
			log.info(lang("add.tag", tagName, call));
		}
	}

	private void buildDataConverterNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "data-converte";

		for (XmlNodeWrapper xNode : contexts) {

			String converterId = getStringFromAttr(xNode, "converter", lang("xml.tag.attribute.empty", "bean", tagName, this.resource));
			DataConverter converter = this.componentContext.getConverterIdMap().get(converterId);
			if (null == converter) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", converterId, "converter", tagName, this.resource));
			}

			List<String> includeList = getBodyList(xNode.evalNodes("include"));
			List<String> excludeList = getBodyList(xNode.evalNodes("exclude"));
			if (null == includeList && null == excludeList) {
				throw new XmlParseException(lang("xml.tag.sub.miss", "include|exclude", tagName, this.resource));
			}

			DataConverterVo vo = new DataConverterVo(converter, includeList, excludeList);
			this.componentContext.addDataConvert(vo);

			log.info(lang("add.tag", tagName, converterId));
		}

	}

	private void buildResponseConvertNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "response";

		for (XmlNodeWrapper xNode : contexts) {

			String bean = getStringFromAttr(xNode, "bean", lang("xml.tag.attribute.empty", "bean", tagName, this.resource));
			Object beanInstance = this.componentContext.getBeanIdMap().get(bean);

			if (null == beanInstance) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", bean, "bean", tagName, this.resource));
			}
			if (!(beanInstance instanceof ResponseHandler)) {
				throw new XmlParseException(lang("xml.class.impl.interface", beanInstance.getClass().getName(), ResponseHandler.class.getName()));
			}

			ResponseHandler handler = (ResponseHandler) beanInstance;

			List<String> includeList = getBodyList(xNode.evalNodes("include"));
			List<String> excludeList = getBodyList(xNode.evalNodes("exclude"));
			if (null == includeList && null == excludeList) {
				throw new XmlParseException(lang("xml.tag.sub.miss", "include|exclude", tagName, this.resource));
			}

			ResponseConvertVo rcVo = new ResponseConvertVo(handler, includeList, excludeList);
			this.componentContext.addResponseConvert(rcVo);

			// log
			log.info(lang("add.tag", tagName, bean));
		}
	}

	private List<String> getBodyList(List<XmlNodeWrapper> contexts) {
		List<String> bodyList = new ArrayList<String>();
		for (XmlNodeWrapper xNode : contexts) {
			String body = StringUtils.trimEmpty(xNode.getStringBody());
			if (null != body) {
				bodyList.add(body);
			}
		}
		if (bodyList.size() == 0) {
			bodyList = null;
		}
		return bodyList;
	}

	private void buildPluginNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		String tagName = "plugin";
		List<XmlWebPluginBuilder> pluginBuilders = new ArrayList<XmlWebPluginBuilder>();
		// 扫描所有的plugin
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String resource = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
			XmlWebPluginBuilder builder = new XmlWebPluginBuilder();
			builder.setContext(resource, this.componentContext);
			// first
			builder.parseRef();
			pluginBuilders.add(builder);
		}
		// 注册所有的服务
		for (int i = 0; i < size; i++) {
			pluginBuilders.get(i).parseService();
			pluginBuilders.get(i).clean();
		}
	}

}
