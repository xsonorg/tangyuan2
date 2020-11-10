package org.xson.tangyuan.aop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xson.tangyuan.aop.service.vo.AopVo;
import org.xson.tangyuan.aop.service.vo.AopVo.PointCut;
import org.xson.tangyuan.aop.service.vo.ServiceAopVo;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

/**
 * AOP入口类
 */
public class AopImpl implements Aop {

	private Map<String, Integer>      execMap             = null;
	private List<AopVo>               beforeList          = null;
	private List<AopVo>               afterList           = null;

	private Map<String, ServiceAopVo> remoteServiceAopMap = null;
	private Map<String, ServiceAopVo> serviceAopMap       = null;

	public AopImpl(Map<String, Integer> execMap, List<AopVo> beforeList, List<AopVo> afterList) {
		this.execMap = execMap;
		this.beforeList = beforeList;
		this.afterList = afterList;
		this.remoteServiceAopMap = new ConcurrentHashMap<String, ServiceAopVo>();
	}

	public void bind(String service, ServiceAopVo serviceAopVo) {
		if (null == serviceAopMap) {
			serviceAopMap = new HashMap<String, ServiceAopVo>();
		}
		serviceAopMap.put(service, serviceAopVo);
	}

	/**
	 * 动态绑定，用户远程代理服务
	 * 
	 * @param service
	 * @param serviceAopVo
	 */
	public void bindDynamic(AbstractServiceNode serviceNode) {
		String service = serviceNode.getServiceKey();
		if (execMap.containsKey(service)) {
			return;
		}
		checkAndsetIntercepted(service, serviceNode);
	}

	public void execBefore(AbstractServiceNode service, Object arg) throws Throwable {
		if (null == serviceAopMap) {
			return;
		}
		if (!service.checkAspect(PointCut.BEFORE)) {
			return;
		}
		ServiceAopVo aopVo = null;
		if (TangYuanServiceType.PRCPROXY == service.getServiceType()) {
			aopVo = remoteServiceAopMap.get(service.getServiceKey());
		} else {
			aopVo = serviceAopMap.get(service.getServiceKey());
		}
		if (null == aopVo) {
			return;
		}
		aopVo.execBefore(service.getServiceKey(), arg);
	}

	public void execAfter(AbstractServiceNode service, Object arg, Object result, Throwable ex) throws Throwable {
		if (null == serviceAopMap) {
			return;
		}
		if (!service.checkAspect(PointCut.AFTER)) {
			return;
		}
		ServiceAopVo aopVo = null;
		if (TangYuanServiceType.PRCPROXY == service.getServiceType()) {
			aopVo = remoteServiceAopMap.get(service.getServiceKey());
		} else {
			aopVo = serviceAopMap.get(service.getServiceKey());
		}
		if (null == aopVo) {
			return;
		}
		aopVo.execAfter(service.getServiceKey(), arg, result, ex);
	}

	/** 检查并设置被拦截方 */
	private void checkAndsetIntercepted(String service, AbstractServiceNode serviceNode) {

		List<AopVo> tempBeforeList = new ArrayList<AopVo>();
		//		List<AopVo> tempAfterExtendList = new ArrayList<AopVo>();
		List<AopVo> tempAfterList  = new ArrayList<AopVo>();

		int         count          = 0;
		// TODO url
		for (AopVo aVo : this.beforeList) {
			//			if (aVo.match(service)) {
			//				tempBeforeList.add(aVo);
			//			}
			if (aVo.match(serviceNode)) {
				tempBeforeList.add(aVo);
			}
		}
		//		for (AopVo aVo : this.afterExtendList) {
		//			if (aVo.match(service)) {
		//				tempAfterExtendList.add(aVo);
		//			}
		//		}
		for (AopVo aVo : this.afterList) {
			//			if (aVo.match(service)) {
			//				tempAfterList.add(aVo);
			//			}
			if (aVo.match(serviceNode)) {
				tempAfterList.add(aVo);
			}
		}

		if (tempBeforeList.size() > 0) {
			serviceNode.setAspect(PointCut.BEFORE);
			count++;
		}
		//		if (tempAfterExtendList.size() > 0) {
		//			serviceNode.setAspect(PointCut.AFTER_EXTEND);
		//			count++;
		//		}
		if (tempAfterList.size() > 0) {
			serviceNode.setAspect(PointCut.AFTER);
			count++;
		}

		if (0 == count) {
			return;
		}

		// ServiceAopVo serviceAopVo = new ServiceAopVo(service, tempBeforeList, tempAfterExtendList, tempAfterList);
		ServiceAopVo serviceAopVo = new ServiceAopVo(service, tempBeforeList, tempAfterList);
		remoteServiceAopMap.put(service, serviceAopVo);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//	private List<AopVo>               afterExtendList     = null;
	//	public Aop(Map<String, Integer> execMap, List<AopVo> beforeList, List<AopVo> afterExtendList, List<AopVo> afterList) {
	//		this.execMap = execMap;
	//		this.beforeList = beforeList;
	//		this.afterExtendList = afterExtendList;
	//		this.afterList = afterList;
	//		this.remoteServiceAopMap = new ConcurrentHashMap<String, ServiceAopVo>();
	//	}

	//	private static Aop			instance			= new Aop();
	//	public static Aop getInstance() {
	//		return instance;
	//	}
	//	private Aop() {}
	//	public void setAopInfo(Map<String, Integer> execMap, List<AopVo> beforeList, List<AopVo> afterExtendList, List<AopVo> afterList) {
	//		this.execMap = execMap;
	//		this.beforeList = beforeList;
	//		this.afterExtendList = afterExtendList;
	//		this.afterList = afterList;
	//		this.remoteServiceAopMap = new ConcurrentHashMap<String, ServiceAopVo>();
	//	}

	//	/** 是否是拦截方 */
	//	private boolean isInterceptor(String serviceURL) {
	//		return execMap.containsKey(serviceURL);
	//	}

	//	public void execAfter(ActuatorContext parent, AbstractServiceNode service, Object arg, Object result, Throwable ex, boolean extend) {
	//		if (null == serviceAopMap) {
	//			return;
	//		}
	//		if (extend) {
	//			if (!service.checkAspect(PointCut.AFTER_EXTEND)) {
	//				return;
	//			}
	//		} else {
	//			if (!service.checkAspect(PointCut.AFTER)) {
	//				return;
	//			}
	//		}
	//		ServiceAopVo aopVo = null;
	//		if (TangYuanServiceType.PRCPROXY == service.getServiceType()) {
	//			aopVo = remoteServiceAopMap.get(service.getServiceKey());
	//		} else {
	//			aopVo = serviceAopMap.get(service.getServiceKey());
	//		}
	//		if (null == aopVo) {
	//			return;
	//		}
	//		aopVo.execAfter(parent, service.getServiceKey(), arg, result, ex, extend);
	//	}

	//	private void checkAndsetIntercepted(String serviceURL, AbstractServiceNode serviceNode) {
}
