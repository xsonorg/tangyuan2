package org.xson.tangyuan.web.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.util.RestUtil;
import org.xson.tangyuan.web.xml.vo.ControllerVo;
import org.xson.tangyuan.web.xml.vo.InterceptVo;
import org.xson.tangyuan.web.xml.vo.MethodObject;
import org.xson.tangyuan.web.xml.vo.RESTControllerVo;

/**
 * 解析过程中的上下文
 */
public class XMLWebContext {

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

	private Map<String, Integer>		restControllerFlagMap	= new HashMap<String, Integer>();
	private List<RESTControllerVo>		restControllerList		= new ArrayList<RESTControllerVo>();

	private Map<String, DataConverter>	converterIdMap			= new HashMap<String, DataConverter>();
	private Map<String, DataConverter>	converterClassMap		= new HashMap<String, DataConverter>();

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
		this.restControllerFlagMap.put(RestUtil.getRestKey(cVo.getRequestType(), cVo.getRestURIVo().getPath()), 0);
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

	public void clear() {
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
	}
}
