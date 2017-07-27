package org.xson.tangyuan.aop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xson.tangyuan.aop.AspectVo.PointCut;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

/**
 * AOP入口类
 */
public class AopSupport {

	private static AopSupport			instance			= new AopSupport();

	private Map<String, Integer>		execMap				= null;
	private List<AspectVo>				beforeCheckList		= null;
	private List<AspectVo>				beforeJoinList		= null;
	private List<AspectVo>				afterJoinList		= null;
	private List<AspectVo>				beforeAloneList		= null;
	private List<AspectVo>				afterAloneList		= null;
	private Map<String, ServiceAopVo>	remoteServiceAopMap	= null;

	private Map<String, ServiceAopVo>	serviceAopMap		= null;

	private AopSupport() {
	}

	public static AopSupport getInstance() {
		return instance;
	}

	public void bind(String service, ServiceAopVo aopVo) {
		if (null == serviceAopMap) {
			serviceAopMap = new HashMap<String, ServiceAopVo>();
		}
		serviceAopMap.put(service, aopVo);
	}

	public void setAopInfo(Map<String, Integer> execMap, List<AspectVo> beforeCheckList, List<AspectVo> beforeJoinList, List<AspectVo> afterJoinList,
			List<AspectVo> beforeAloneList, List<AspectVo> afterAloneList) {
		this.execMap = execMap;
		this.beforeCheckList = beforeCheckList;
		this.beforeJoinList = beforeJoinList;
		this.afterJoinList = afterJoinList;
		this.beforeAloneList = beforeAloneList;
		this.afterAloneList = afterAloneList;
		this.remoteServiceAopMap = new ConcurrentHashMap<String, ServiceAopVo>();
	}

	public void execBefore(AbstractServiceNode service, Object arg, PointCut pointCut) {
		if (null == serviceAopMap) {
			return;
		}
		if (!service.checkAspect(pointCut)) {
			return;
		}

		// ServiceAopVo aopVo = serviceAopMap.get(service.getServiceKey());

		ServiceAopVo aopVo = null;
		if (TangYuanServiceType.PRCPROXY == service.getServiceType()) {
			aopVo = remoteServiceAopMap.get(service.getServiceKey());
		} else {
			aopVo = serviceAopMap.get(service.getServiceKey());
		}
		if (null == aopVo) {
			return;
		}
		aopVo.execBefore(service.getServiceKey(), arg, pointCut);
	}

	public void execAfter(AbstractServiceNode service, Object arg, Object result, Throwable ex, PointCut pointCut) {
		if (null == serviceAopMap) {
			return;
		}
		if (!service.checkAspect(pointCut)) {
			return;
		}

		// ServiceAopVo aopVo = serviceAopMap.get(service.getServiceKey());

		ServiceAopVo aopVo = null;
		if (TangYuanServiceType.PRCPROXY == service.getServiceType()) {
			aopVo = remoteServiceAopMap.get(service.getServiceKey());
		} else {
			aopVo = serviceAopMap.get(service.getServiceKey());
		}
		if (null == aopVo) {
			return;
		}
		aopVo.execAfter(service.getServiceKey(), arg, result, ex, pointCut);
	}

	/** 是否是拦截方 */
	public boolean isInterceptor(String serviceURL) {
		return execMap.containsKey(serviceURL);
	}

	/** 检查并设置被拦截方 */
	public void checkAndsetIntercepted(String serviceURL, AbstractServiceNode serviceNode) {

		String service = serviceURL;

		List<AspectVo> tempBeforeCheckList = new ArrayList<AspectVo>();
		List<AspectVo> tempBeforeJoinList = new ArrayList<AspectVo>();
		List<AspectVo> tempAfterJoinList = new ArrayList<AspectVo>();
		List<AspectVo> tempBeforeAloneList = new ArrayList<AspectVo>();
		List<AspectVo> tempAfterAloneList = new ArrayList<AspectVo>();

		int count = 0;

		for (AspectVo aVo : this.beforeCheckList) {
			if (aVo.match(service)) {
				tempBeforeCheckList.add(aVo);
			}
		}
		for (AspectVo aVo : this.beforeJoinList) {
			if (aVo.match(service)) {
				tempBeforeJoinList.add(aVo);
			}
		}
		for (AspectVo aVo : this.afterJoinList) {
			if (aVo.match(service)) {
				tempAfterJoinList.add(aVo);
			}
		}
		for (AspectVo aVo : this.beforeAloneList) {
			if (aVo.match(service)) {
				tempBeforeAloneList.add(aVo);
			}
		}
		for (AspectVo aVo : this.afterAloneList) {
			if (aVo.match(service)) {
				tempAfterAloneList.add(aVo);
			}
		}

		if (tempBeforeCheckList.size() > 0) {
			serviceNode.setAspect(PointCut.BEFORE_CHECK);
			count++;
		}
		if (tempBeforeJoinList.size() > 0) {
			serviceNode.setAspect(PointCut.BEFORE_JOIN);
			count++;
		}
		if (tempAfterJoinList.size() > 0) {
			serviceNode.setAspect(PointCut.AFTER_JOIN);
			count++;
		}
		if (tempBeforeAloneList.size() > 0) {
			serviceNode.setAspect(PointCut.BEFORE_ALONE);
			count++;
		}
		if (tempAfterAloneList.size() > 0) {
			serviceNode.setAspect(PointCut.AFTER_ALONE);
			count++;
		}

		if (0 == count) {
			return;
		}

		ServiceAopVo aopVo = new ServiceAopVo(service, tempBeforeCheckList, tempBeforeJoinList, tempAfterJoinList, tempBeforeAloneList,
				tempAfterAloneList);

		remoteServiceAopMap.put(service, aopVo);
	}
}
