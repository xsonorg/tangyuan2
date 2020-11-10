package org.xson.tangyuan.manager.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.util.PatternMatchUtils;

public class ServiceManagerVo {

	/**
	 * 固定的被挂起的服务集合
	 */
	private Map<String, Integer> serviceMap  = null;

	/**
	 * 模糊的被挂起的服务集合
	 */
	private List<String>         serviceList = null;

	public void init(XCO data) {
		if (null == data) {
			return;
		}
		// TODO suspendedServices可以短一些
		List<String> list = data.getStringListValue("precise");
		if (CollectionUtils.isEmpty(list)) {
			this.serviceMap = new HashMap<String, Integer>();
			for (String s : list) {
				this.serviceMap.put(s, 1);
			}
		}

		list = data.getStringListValue("fuzzy");
		if (CollectionUtils.isEmpty(list)) {
			this.serviceList = list;
		}
	}

	public boolean isAccessed(String service) {
		if (null != this.serviceMap) {
			if (this.serviceMap.containsKey(service)) {
				return false;
			}
		}
		if (null != this.serviceList) {
			// pattern: /a/*
			for (String pattern : this.serviceList) {
				if (PatternMatchUtils.simpleMatch(pattern, service)) {
					return false;
				}
			}
		}
		return true;
	}
}
