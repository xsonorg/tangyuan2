package org.xson.tangyuan.web.xml;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.web.ControllerVo;
import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.convert.DataConverterVo;
import org.xson.tangyuan.web.handler.ResponseConvertVo;
import org.xson.tangyuan.web.rest.RESTControllerVo;
import org.xson.tangyuan.web.util.WebUtil;
import org.xson.tangyuan.web.xml.vo.InterceptVo;
import org.xson.tangyuan.web.xml.vo.MethodObject;
import org.xson.tangyuan.xml.DefaultXmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlParseException;

/**
 * 解析过程中的上下文
 */
public class XmlWebContext extends DefaultXmlContext {

	private XmlGlobalContext			xmlContext				= null;

	private Map<String, ControllerVo>	controllerMap			= new HashMap<String, ControllerVo>();

	private Map<String, Object>			beanIdMap				= new HashMap<String, Object>();
	private Map<String, Object>			beanClassMap			= new HashMap<String, Object>();

	private Map<String, MethodObject>	moMap					= new HashMap<String, MethodObject>();

	private List<InterceptVo>			beforeList				= new ArrayList<InterceptVo>();
	private List<InterceptVo>			afterList				= new ArrayList<InterceptVo>();
	private List<InterceptVo>			assemblyList			= new ArrayList<InterceptVo>();

	private Map<String, String>			beforeMap				= new HashMap<String, String>();
	private Map<String, String>			afterMap				= new HashMap<String, String>();
	private Map<String, String>			assemblyMap				= new HashMap<String, String>();

	private List<ResponseConvertVo>		responseConvertList		= new ArrayList<ResponseConvertVo>();

	private Map<String, DataConverter>	converterIdMap			= new HashMap<String, DataConverter>();
	private Map<String, DataConverter>	converterClassMap		= new HashMap<String, DataConverter>();
	private List<DataConverterVo>		dataConvertList			= new ArrayList<DataConverterVo>();

	private Map<String, Integer>		restControllerFlagMap	= new HashMap<String, Integer>();
	private List<RESTControllerVo>		restControllerList		= new ArrayList<RESTControllerVo>();

	// private Map<String, String> responseHandlerMap = new HashMap<String, String>();

	@Override
	public void clean() {

		this.xmlContext = null;

		// this.domainMap.clear();
		this.beanIdMap.clear();
		this.beanClassMap.clear();
		this.moMap.clear();
		this.beforeList.clear();
		this.afterList.clear();
		this.beforeMap.clear();
		this.afterMap.clear();
		this.assemblyList.clear();
		this.restControllerFlagMap.clear();
		this.restControllerList.clear();

		this.converterIdMap.clear();
		this.converterClassMap.clear();

		this.responseConvertList.clear();

		// this.domainMap = null;
		this.beanIdMap = null;
		this.beanClassMap = null;
		this.moMap = null;
		this.beforeList = null;
		this.afterList = null;
		this.beforeMap = null;
		this.afterMap = null;
		this.assemblyMap = null;
		this.controllerMap = null;
		this.restControllerFlagMap = null;
		this.restControllerList = null;
		this.converterIdMap = null;
		this.converterClassMap = null;

		this.responseConvertList = null;

		this.dataConvertList.clear();
		this.dataConvertList = null;
	}

	public XmlGlobalContext getXmlContext() {
		return xmlContext;
	}

	public void setXmlContext(XmlGlobalContext xmlContext) {
		this.xmlContext = xmlContext;
	}
	///

	public Map<String, Object> getBeanIdMap() {
		return beanIdMap;
	}

	public Map<String, Object> getBeanClassMap() {
		return beanClassMap;
	}

	public Map<String, ControllerVo> getControllerMap() {
		return controllerMap;
	}

	public boolean existRestURI(String key) {
		return restControllerFlagMap.containsKey(key);
	}

	public void addRestController(RESTControllerVo cVo) {
		this.restControllerList.add(cVo);
		this.restControllerFlagMap.put(WebUtil.getRestKey(cVo.getRequestType(), cVo.getRestURIVo().getPath()), 0);
	}

	public void addResponseConvert(ResponseConvertVo rcVo) {
		this.responseConvertList.add(rcVo);
	}

	public void addDataConvert(DataConverterVo vo) {
		this.dataConvertList.add(vo);
	}

	public List<InterceptVo> getBeforeList() {
		return beforeList;
	}

	public List<InterceptVo> getAfterList() {
		return afterList;
	}

	public List<InterceptVo> getAssemblyList() {
		return assemblyList;
	}

	public Map<String, String> getBeforeMap() {
		return beforeMap;
	}

	public Map<String, String> getAfterMap() {
		return afterMap;
	}

	public Map<String, String> getAssemblyMap() {
		return assemblyMap;
	}

	public Map<String, MethodObject> getMoMap() {
		return moMap;
	}

	public List<RESTControllerVo> getRestControllerList() {
		return restControllerList;
	}

	public Map<String, DataConverter> getConverterIdMap() {
		return converterIdMap;
	}

	public Map<String, DataConverter> getConverterClassMap() {
		return converterClassMap;
	}

	public List<ResponseConvertVo> getResponseConvertList() {
		return responseConvertList;
	}

	public List<DataConverterVo> getDataConvertList() {
		return dataConvertList;
	}

	public MethodObject getMethodObject(String str) throws Exception {
		MethodObject mo = null;
		// a.b
		if (str.startsWith("{")) {
			String[] array = str.split("\\.");
			if (array.length != 2) {
				throw new XmlParseException(TangYuanLang.get("web.bean.expr.invalid", str));
			}

			String beanId = array[0].substring(1, array[0].length() - 1);
			String methodName = array[1];
			Object bean = this.getBeanIdMap().get(beanId);

			if (null == bean) {
				throw new XmlParseException(TangYuanLang.get("web.bean.reference.invalid", beanId));
			}
			String moKey = bean.getClass().getName() + "." + methodName;
			mo = this.getMoMap().get(moKey);
			if (null == mo) {
				Method method = bean.getClass().getMethod(methodName, RequestContext.class);
				// 如果方法不存在或者参数类型不匹配，这里会抛出异常
				method.setAccessible(true);
				mo = new MethodObject(method, bean);
				this.getMoMap().put(moKey, mo);
			}
		} else {
			mo = this.getMoMap().get(str);
			if (null != mo) {
				return mo;
			}
			int pos = str.lastIndexOf(".");
			String className = str.substring(1, pos);
			String methodName = str.substring(pos + 1, str.length());

			Object instance = this.getBeanClassMap().get(className);
			Class<?> clazz = null;
			if (null == instance) {
				// only JDK
				// clazz = Class.forName(className);
				// instance = clazz.newInstance();

				// AUTO[CGLIB/JDK]
				clazz = ClassUtils.forName(className);
				instance = TangYuanUtil.newInstance(clazz);

				this.getBeanClassMap().put(className, instance);
			} else {
				clazz = instance.getClass();
			}
			Method method = clazz.getMethod(methodName, RequestContext.class);
			// 如果方法不存在或者参数类型不匹配，这里会抛出异常
			method.setAccessible(true);
			mo = new MethodObject(method, instance);
			this.getMoMap().put(str, mo);
		}
		return mo;
	}

}
