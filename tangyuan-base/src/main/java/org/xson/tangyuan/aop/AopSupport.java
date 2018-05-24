package org.xson.tangyuan.aop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xson.tangyuan.aop.AopVo.PointCut;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

/**
 * AOP入口类
 */
public class AopSupport {

	private static AopSupport			instance			= new AopSupport();

	private Map<String, Integer>		execMap				= null;

	private List<AopVo>					beforeList			= null;
	private List<AopVo>					afterExtendList		= null;
	private List<AopVo>					afterList			= null;

	private Map<String, ServiceAopVo>	remoteServiceAopMap	= null;
	private Map<String, ServiceAopVo>	serviceAopMap		= null;

	private AopSupport() {
	}

	public static AopSupport getInstance() {
		return instance;
	}

	public void bind(String service, ServiceAopVo serviceAopVo) {
		if (null == serviceAopMap) {
			serviceAopMap = new HashMap<String, ServiceAopVo>();
		}
		serviceAopMap.put(service, serviceAopVo);
	}

	public void setAopInfo(Map<String, Integer> execMap, List<AopVo> beforeList, List<AopVo> afterExtendList, List<AopVo> afterList) {
		this.execMap = execMap;
		this.beforeList = beforeList;
		this.afterExtendList = afterExtendList;
		this.afterList = afterList;
		this.remoteServiceAopMap = new ConcurrentHashMap<String, ServiceAopVo>();
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

	public void execAfter(AbstractServiceNode service, Object arg, Object result, Throwable ex, boolean extend) {
		if (null == serviceAopMap) {
			return;
		}
		if (extend) {
			if (!service.checkAspect(PointCut.AFTER_EXTEND)) {
				return;
			}
		} else {
			if (!service.checkAspect(PointCut.AFTER)) {
				return;
			}
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
		aopVo.execAfter(service.getServiceKey(), arg, result, ex, extend);
	}

	/** 是否是拦截方 */
	public boolean isInterceptor(String serviceURL) {
		return execMap.containsKey(serviceURL);
	}

	/** 检查并设置被拦截方 */
	public void checkAndsetIntercepted(String serviceURL, AbstractServiceNode serviceNode) {

		String service = serviceURL;

		List<AopVo> tempBeforeList = new ArrayList<AopVo>();
		List<AopVo> tempAfterExtendList = new ArrayList<AopVo>();
		List<AopVo> tempAfterList = new ArrayList<AopVo>();

		int count = 0;

		for (AopVo aVo : this.beforeList) {
			if (aVo.match(service)) {
				tempBeforeList.add(aVo);
			}
		}
		for (AopVo aVo : this.afterExtendList) {
			if (aVo.match(service)) {
				tempAfterExtendList.add(aVo);
			}
		}
		for (AopVo aVo : this.afterList) {
			if (aVo.match(service)) {
				tempAfterList.add(aVo);
			}
		}

		if (tempBeforeList.size() > 0) {
			serviceNode.setAspect(PointCut.BEFORE);
			count++;
		}
		if (tempAfterExtendList.size() > 0) {
			serviceNode.setAspect(PointCut.AFTER_EXTEND);
			count++;
		}
		if (tempAfterList.size() > 0) {
			serviceNode.setAspect(PointCut.AFTER);
			count++;
		}

		if (0 == count) {
			return;
		}

		ServiceAopVo serviceAopVo = new ServiceAopVo(service, tempBeforeList, tempAfterExtendList, tempAfterList);

		remoteServiceAopMap.put(service, serviceAopVo);
	}
}
